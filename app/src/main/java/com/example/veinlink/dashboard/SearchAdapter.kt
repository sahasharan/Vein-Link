
package com.example.veinlink.dashboard

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.veinlink.R
import com.example.veinlink.models.Donor
import com.example.veinlink.network.ApiService
import com.example.veinlink.util.LocationProvider
import com.example.veinlink.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import java.util.concurrent.Executors
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SearchAdapter(
    private var donorList: List<Donor>
) : RecyclerView.Adapter<SearchAdapter.DonorViewHolder>() {

    private val TAG = "SearchAdapter"

    class DonorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val donorName: TextView = itemView.findViewById(R.id.donorName)
        val donorBlood: TextView = itemView.findViewById(R.id.donorBlood)
        val buttonSendRequest: Button = itemView.findViewById(R.id.buttonSendRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donor, parent, false)
        return DonorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donorList[position]
        holder.donorName.text = donor.name
        holder.donorBlood.text = "Blood Group: ${donor.bloodGroup}"

        holder.buttonSendRequest.setOnClickListener {
            if (donor.email.isNotBlank()) {
                showRequestDialog(holder.itemView.context, donor)
            } else {
                Toast.makeText(holder.itemView.context, "Donor email is not available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRequestDialog(context: Context, donor: Donor) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Request to ${donor.name}")

        val input = EditText(context)
        input.hint = "Write your message here..."
        builder.setView(input)

        builder.setPositiveButton("Send") { dialog, _ ->
            val message = input.text.toString()
            if (message.isNotBlank()) {
                // Fetch location and send email
                val locationProvider = LocationProvider(context)
                CoroutineScope(Dispatchers.Main).launch {
                    val location = locationProvider.getCurrentLocation()
                    sendRequestInBackground(context, donor, message, location)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please write a message.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun sendRequestInBackground(context: Context, donor: Donor, customMessage: String, location: Location?) {
        // Send email in a background thread
        Executors.newSingleThreadExecutor().execute {
            sendRequestEmail(context, donor, customMessage, location)
        }

        // Simulate sending a push notification
        ApiService.sendPushNotification(
            token = donor.fcmToken,
            title = "Urgent Blood Request",
            message = "A user needs your help! Blood group: ${donor.bloodGroup}"
        )
    }

    private fun sendRequestEmail(context: Context, donor: Donor, customMessage: String, location: Location?) {
        val fromEmail = "abcd20050625@gmail.com"
        val password = "qkvm lzhw uobx orcc"

        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.auth", "true")
            put("mail.smtp.port", "465")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(fromEmail, password)
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(fromEmail, "VeinLink"))
                addRecipient(Message.RecipientType.TO, InternetAddress(donor.email))
                subject = "Urgent Blood Request via VeinLink"

                val locationInfo = if (location != null) {
                    "<p>The requester\'s approximate location is: <a href=\"http://maps.google.com/maps?q=${location.latitude},${location.longitude}\">View on Map</a></p>"
                } else {
                    "<p>Location not available.</p>"
                }

                setContent("""
                    <p>Dear ${donor.name},</p>
                    <p>A user on VeinLink requires your assistance. They have sent the following message:</p>
                    <hr>
                    <p><i>"$customMessage"</i></p>
                    <hr>
                    $locationInfo
                    <p>Please coordinate with them if you are available to donate. Your quick response could save a life.</p>
                    <p>Thank you,<br>The VeinLink Team</p>
                """, "text/html; charset=utf-8")
            }
            Transport.send(message)
            Log.d(TAG, "Request email sent successfully to ${donor.email}")

            // Show a notification to the sender
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showRequestSentNotification()

        } catch (e: Exception) {
            Log.e(TAG, "Error sending request email", e)
        }
    }

    override fun getItemCount(): Int = donorList.size

    fun setDonors(newDonors: List<Donor>) {
        donorList = newDonors
        notifyDataSetChanged()
    }
}
