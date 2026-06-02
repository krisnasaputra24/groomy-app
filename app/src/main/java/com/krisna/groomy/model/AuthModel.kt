package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Update Profile (Sesuaikan dengan UpdateUserDto di NestJS)
 */
data class ProfileUpdateRequest(
    @SerializedName("name")
    val name: String,
    
    // Jika masih tidak mau, coba ganti menjadi "phoneNumber" sesuai DTO backend Anda
    @SerializedName("phone")
    val phone: String
)

data class ProfileResponse(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("profilePicture")
    val profilePicture: String? = null,
    @SerializedName("groomerId")
    val groomerId: Int? = null,
    @SerializedName("groomers")
    val groomers: List<GroomerResponse>? = null,
    @SerializedName("message")
    val message: String? = null
)

// Model Login & Register tetap sama seperti sebelumnya...
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null
)
