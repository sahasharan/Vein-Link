package com.example.veinlink.models

// This class defines a User, which might be different from a Donor.
// The duplicate Donor data class has been removed from this file.
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "" // Assuming phone is specific to this User model
)
