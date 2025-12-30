package com.theveloper.pixelplay.data.network.eden.models

import com.google.gson.annotations.SerializedName

/**
 * Request model for user login
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

/**
 * Request model for user signup
 */
data class SignupRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("name")
    val name: String
)

/**
 * User information from authentication
 */
data class UserInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String // "user", "artist", or "admin"
)

/**
 * Response model for authentication (login/signup)
 */
data class AuthResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserInfo
)
