package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Request Add Service (Mandatory Fields)
 */
data class AddServiceRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("price")
    val price: Int,
    
    @SerializedName("groomerId")
    val groomerId: Int,
    
    @SerializedName("photo")
    val photo: String? = null
)

/**
 * Model untuk Request Edit Service (Optional Fields)
 */
data class ServiceRequest(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("price")
    val price: Int? = null,
    
    @SerializedName("groomerId")
    val groomerId: Int? = null,
    
    @SerializedName("photo")
    val photo: String? = null
)

/**
 * Model untuk Response dari API Service
 */
data class ServiceResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("uuid")
    val uuid: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("photo")
    val photo: String?,
    
    @SerializedName("groomerId")
    val groomerId: Int,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?,
    
    @SerializedName("groomer")
    val groomer: GroomerResponse? = null
)
