package com.example.veinlink.network

import android.util.Log
import com.example.veinlink.models.Donor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object ApiService {

    private const val TAG = "ApiService"
    private val db: FirebaseFirestore = Firebase.firestore

    private const val DONORS_COLLECTION = "donors"

    fun searchDonors(query: String): Flow<List<Donor>> = callbackFlow {
        Log.d(TAG, "Starting Firestore real-time search for query: '$query'")

        var queryRef = db.collection(DONORS_COLLECTION).limit(50)

        if (query.isNotBlank()) {
            queryRef = queryRef.whereGreaterThanOrEqualTo("bloodGroup", query)
                               .whereLessThanOrEqualTo("bloodGroup", query + "\uf8ff")
        }

        val snapshotListener = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Firestore listener failed for query: '$query'", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.d(TAG, "Received ${snapshot.size()} documents for query: '$query'")
                val donors = snapshot.toObjects(Donor::class.java)
                trySend(donors).isSuccess
            } else {
                Log.d(TAG, "Received null snapshot for query: '$query'")
            }
        }

        awaitClose { 
            Log.d(TAG, "Closing Firestore listener for query: '$query'")
            snapshotListener.remove()
        }
    }

    /**
     * SIMULATES sending a push notification via a backend.
     * In a real app, this would be a network call to your server.
     */
    fun sendPushNotification(token: String, title: String, message: String) {
        if (token.isBlank()) {
            Log.w(TAG, "Cannot send push notification: FCM token is missing.")
            return
        }

        // In a real app, you would make a POST request to your server endpoint.
        // The server would then use the Firebase Admin SDK to send the message.
        Log.i(TAG, "--- SIMULATING PUSH NOTIFICATION ---")
        Log.i(TAG, "Recipient Token: $token")
        Log.i(TAG, "Title: $title")
        Log.i(TAG, "Message: $message")
        Log.i(TAG, "-------------------------------------")
    }
}
