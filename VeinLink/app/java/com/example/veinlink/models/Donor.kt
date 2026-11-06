package com.example.veinlink.models

import kotlinx.serialization.Serializable

@Serializable
data class Donor(
    val donorId: String = "",
    val name: String = ""
)
