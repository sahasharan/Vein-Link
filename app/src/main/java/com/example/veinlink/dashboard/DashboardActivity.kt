
package com.example.veinlink.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.veinlink.R
import com.example.veinlink.models.Donor
import com.example.veinlink.network.ApiService
import com.example.veinlink.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

class DashboardActivity : AppCompatActivity() {

    private lateinit var donorRecyclerView: RecyclerView
    private lateinit var donorAdapter: SearchAdapter
    private lateinit var searchBarEditText: EditText
    private lateinit var textViewNoResults: TextView
    private lateinit var progressBar: ProgressBar

    private var searchJob: Job? = null
    private val TAG = "DashboardActivity"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Create notification channel
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        searchBarEditText = findViewById(R.id.searchBarEditText)
        donorRecyclerView = findViewById(R.id.donorRecyclerView)
        textViewNoResults = findViewById(R.id.textViewNoResults)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        setupSearch()

        // Start with a default search to show all donors initially
        fetchDonors("")
    }

    override fun onResume() {
        super.onResume()
        // Re-check location status when the user returns to the app from settings
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permission is granted, now check if location is enabled
            if (!isLocationEnabled()) {
                showEnableLocationDialog()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Location")
            .setMessage("Location services are disabled. Please enable them to share your location with donors.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission was granted, now check if location is enabled
                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                }
            } else {
                Toast.makeText(this, "Location permission is required to share your location with donors.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRecyclerView() {
        donorRecyclerView.layoutManager = LinearLayoutManager(this)
        donorAdapter = SearchAdapter(emptyList())
        donorRecyclerView.adapter = donorAdapter
    }

    private fun setupSearch() {
        searchBarEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                fetchDonors(query)
            }
        })
    }

    private fun fetchDonors(query: String) {
        Log.d(TAG, "Starting fetch for query: '$query'")

        // Cancel any previous search coroutine to avoid multiple listeners
        searchJob?.cancel()

        progressBar.visibility = View.VISIBLE
        donorRecyclerView.visibility = View.GONE
        textViewNoResults.visibility = View.GONE

        searchJob = lifecycleScope.launch {
            try {
                val uppercaseQuery = query.uppercase()

                ApiService.searchDonors(uppercaseQuery).collect { donors ->
                    progressBar.visibility = View.GONE
                    Log.d(TAG, "Firestore emitted ${donors.size} donors for query: '$uppercaseQuery'")

                    donorAdapter.setDonors(donors)

                    if (donors.isEmpty()) {
                        textViewNoResults.text = "No donors found."
                        textViewNoResults.visibility = View.VISIBLE
                        donorRecyclerView.visibility = View.GONE
                    } else {
                        textViewNoResults.visibility = View.GONE
                        donorRecyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                // If the exception is a CancellationException, it's expected, so we ignore it.
                if (e is CancellationException) {
                    Log.d(TAG, "Search coroutine was cancelled. This is expected.")
                    // Don't show an error toast for cancellation.
                } else {
                    // For any other real error, we show the message.
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Error collecting Firestore flow", e)
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    textViewNoResults.text = "Error loading data. Please check Firestore rules."
                    textViewNoResults.visibility = View.VISIBLE
                    donorRecyclerView.visibility = View.GONE
                }
            }
        }
    }
}
