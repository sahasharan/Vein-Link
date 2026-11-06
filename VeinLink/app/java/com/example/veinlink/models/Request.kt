package com.example.veinlink.models

data class Request(
    val requestId: String = "",
    val patientName: String = "",
    val bloodGroup: String = "",
    val location: String = "",
    val contactNumber: String = "",
    val status: String = "Pending"
)
