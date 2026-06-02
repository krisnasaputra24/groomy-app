package com.krisna.groomy.model

import com.google.gson.annotations.SerializedName

// -------------------------------------------------
// 1️⃣  Enum status – harus sama dengan yang ada di API
// -------------------------------------------------
enum class GroomingStatus {
    @SerializedName("PENDING") PENDING,
    @SerializedName("SCHEDULED") SCHEDULED,
    @SerializedName("ACCEPTED") ACCEPTED,
    @SerializedName("IN_PROGRESS") IN_PROGRESS,
    @SerializedName("READY_FOR_PICKUP") READY_FOR_PICKUP,
    @SerializedName("COMPLETED") COMPLETED,
    @SerializedName("REJECTED") REJECTED
}

// -------------------------------------------------
// 2️⃣  Payload request
// -------------------------------------------------
data class BookingRequest(
    // contoh: "2026-07-15"
    val date: String,
    // contoh: "14:30"
    val time: String,
    val groomerId: Int,
    val serviceId: Int,
    val petId: Int
)

data class UpdateBookingStatusRequest(
    // Pakai enum agar tidak typo
    val status: GroomingStatus
)

// -------------------------------------------------
// 3️⃣  Payload response
// -------------------------------------------------
data class BookingResponse(
    val id: Int,
    val uuid: String,
    val date: String,
    val time: String,
    val status: GroomingStatus?,
    val userId: Int,
    val user: ProfileResponse? = null,
    val groomerId: Int,
    val groomer: GroomerResponse? = null,
    val serviceId: Int,
    val service: ServiceResponse? = null,
    val createdAt: String,
    val updatedAt: String
)