package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

data class PromoRequest(
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("discount")
    val discount: Double,
    @SerializedName("expiryDate")
    val expiryDate: String,
    @SerializedName("serviceId")
    val serviceId: Int,
    @SerializedName("groomerId")
    val groomerId: Int
)

data class PromoResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("uuid")
    val uuid: String? = null,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("discount")
    val discount: Int,
    @SerializedName("expiryDate")
    val expiryDate: String,
    @SerializedName("serviceId")
    val serviceId: Int,
    @SerializedName("service")
    val service: ServiceResponse? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    // Fallback for UI if needed
    val bannerUrl: String? = null
)
