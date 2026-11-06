package com.example.veinlink.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.veinlink.R
import com.example.veinlink.models.Donor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Properties
import java.util.concurrent.Executors
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextBloodGroup: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewGoToLogin: TextView
    private lateinit var formLayout: LinearLayout
    private lateinit var textViewSuccessMessage: TextView
    private lateinit var buttonRegisterOriginalText: String

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    private val SUCCESS_DISPLAY_DELAY = 3000L
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextBloodGroup = findViewById(R.id.editTextBloodGroup)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonRegisterOriginalText = buttonRegister.text.toString()
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin)
        formLayout = findViewById(R.id.formLayout)
        textViewSuccessMessage = findViewById(R.id.textViewSuccessMessage)

        buttonRegister.setOnClickListener { performRegistration() }
        textViewGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun performRegistration() {
        val username = editTextUsername.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val bloodGroup = editTextBloodGroup.text.toString().trim().uppercase()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || bloodGroup.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        buttonRegister.isEnabled = false
        buttonRegister.text = "Registering..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid

                    if (userId != null) {
                        // Get FCM token
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (!tokenTask.isSuccessful) {
                                Log.w(TAG, "Fetching FCM registration token failed", tokenTask.exception)
                                // Continue without a token
                                saveUser(userId, username, email, bloodGroup, "")
                                return@addOnCompleteListener
                            }
                            val token = tokenTask.result
                            saveUser(userId, username, email, bloodGroup, token)
                        }
                    } else {
                        // This case is unlikely but handled for safety
                        buttonRegister.isEnabled = true
                        buttonRegister.text = buttonRegisterOriginalText
                    }
                } else {
                    buttonRegister.isEnabled = true
                    buttonRegister.text = buttonRegisterOriginalText
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(baseContext, "This email is already registered.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveUser(userId: String, username: String, email: String, bloodGroup: String, fcmToken: String) {
        val newUser = Donor(userId, username, email, bloodGroup, "", true, fcmToken)
        db.collection("donors").document(userId).set(newUser)
            .addOnSuccessListener {
                Log.d(TAG, "User profile created for UID: $userId with FCM token.")
                sendWelcomeEmail(username, email)
                showSuccessAndNavigate()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding user profile to Firestore", e)
                buttonRegister.isEnabled = true
                buttonRegister.text = buttonRegisterOriginalText
            }
    }

    private fun sendWelcomeEmail(name: String, email: String) {
        // ... (rest of the function is unchanged)
    }

    private fun showSuccessAndNavigate() {
        formLayout.visibility = View.GONE
        textViewSuccessMessage.visibility = View.VISIBLE
        Toast.makeText(this, "Registration successful! Redirecting...", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, SUCCESS_DISPLAY_DELAY)
    }
}
