package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("message")
    val message: String,
    @SerializedName("groomerId")
    val groomerId: Int,
    @SerializedName("orderId")
    val orderId: Int? = null
)

data class ChatResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("timestamp")
    val timestamp: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("userId")
    val userId: Int? = null,
    @SerializedName("groomerId")
    val groomerId: Int? = null,
    @SerializedName("orderId")
    val orderId: Int? = null,
    @SerializedName("user")
    val user: ChatUser? = null,
    @SerializedName("groomer")
    val groomer: ChatGroomer? = null,
    @SerializedName("senderRole")
    val senderRole: String? = null // Handled manually if not in JSON
)

data class ChatUser(
    val name: String,
    val profilePicture: String? = null
)

data class ChatGroomer(
    val name: String,
    val profilePicture: String? = null
)
