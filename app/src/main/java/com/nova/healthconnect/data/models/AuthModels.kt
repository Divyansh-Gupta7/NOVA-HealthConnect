package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String = "NOVA Explorer",
    val photoUrl: String? = null,
    val token: String? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    val status: String = "success",
    val token: String? = null,
    val user: UserSummary? = null,
    val message: String? = null
)
