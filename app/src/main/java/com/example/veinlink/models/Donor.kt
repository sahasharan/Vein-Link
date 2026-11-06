package com.example.veinlink.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donors")
data class Donor(
    @PrimaryKey
    val donorId: String = "",
    val name: String = "",
    val email: String = "", // Email field
    val bloodGroup: String = "", // Blood group field
    val location: String = "",
    val availability: Boolean = true,
    val fcmToken: String = "" // FCM token for push notifications
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this("", "", "", "", "", true, "")
}
