package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Request Registrasi Groomer
 */
data class GroomerRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("phone")
    val phone: String
)

/**
 * Model untuk Request Edit Profile Groomer
 */
data class EditGroomerRequest(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("profilePicture")
    val profilePicture: String? = null
)

data class GroomerResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("uuid")
    val uuid: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("rating")
    val rating: Int,
    
    @SerializedName("reviews")
    val reviews: Int,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("profilePicture")
    val profilePicture: String?,
    
    @SerializedName("userId")
    val userId: Int
)
