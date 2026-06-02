package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

// ========================================
// UNIFIED ORDER STATUS (grooming + order)
// ========================================
enum class OrderStatus {
    @SerializedName("SCHEDULED") SCHEDULED,
    @SerializedName("PENDING") PENDING,
    @SerializedName("CONFIRMED") CONFIRMED,
    @SerializedName("CANCELLED") CANCELLED,
    @SerializedName("IN_PROGRESS") IN_PROGRESS,
    @SerializedName("COMPLETED") COMPLETED
}

// ========================================
// REQUEST / RESPONSE DTOs
// ========================================
data class CreateOrderRequest(
    val date: String,       // "2026-06-10"
    val time: String,       // "10:00"
    val groomerId: Int,
    val serviceId: Int,
    val petId: Int?,         // Optional (untuk grooming)
    val promoId: Int? = null
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus
)

data class OrderResponse(
    val id: Int,
    val uuid: String,
    val date: String,
    val time: String,
    val status: OrderStatus?,
    val petId: Int?,
    val userId: Int,
    val user: ProfileResponse?,
    val groomerId: Int,
    val groomer: GroomerResponse?,
    val serviceId: Int,
    val service: ServiceResponse?,
    val chats: List<ChatResponse>?,
    val createdAt: String,
    val updatedAt: String
)

// Legacy compatibility (Optional: you might want to remove these once all files are updated)
typealias BookingResponse = OrderResponse
typealias BookingRequest = CreateOrderRequest
