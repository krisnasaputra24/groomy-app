package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Request Add/Edit Pet
 */
data class PetRequest(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("breed")
    val breed: String? = null,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("photo")
    val photo: String? = null
)

/**
 * Model untuk Response dari API Pet
 */
data class PetResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("uuid")
    val uuid: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("breed")
    val breed: String?,
    
    @SerializedName("age")
    val age: Int?,
    
    @SerializedName("photo")
    val photo: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?,
    
    @SerializedName("updatedAt")
    val updatedAt: String?
)
