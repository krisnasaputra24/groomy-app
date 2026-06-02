package com.krisna.groomy.model

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val profilePhotoUrl: String? = null,
    val role: String = "User", // "User", "Pending", "Groomer"
    val isGroomerApproved: Boolean = false
)

data class Pet(
    val id: String,
    val name: String,
    val type: String,
    val breed: String,
    val age: String,
    val weight: String,
    val groomingHistory: List<String>,
    val petPhotoUrl: String? = null
)

data class Booking(
    val id: String,
    val customerName: String,
    val petName: String,
    val serviceType: String,
    val dateTime: String,
    var status: String, // "Pending", "Accepted", "Rejected", "In Progress", "Ready for Pickup", "Completed"
    var groomingProgress: Float = 0f // 0.0 to 1.0
)

data class Groomer(
    val id: String,
    val name: String,
    val location: String,
    val rating: Float,
    val reviewCount: Int,
    val profilePhotoUrl: String? = null,
    val services: List<GroomerService> = emptyList(),
    val description: String = ""
)

data class GroomerService(
    val name: String,
    val price: String,
    val duration: String,
    val id: String = java.util.UUID.randomUUID().toString(),
    val photoUrl: String? = null,
    val isPromo: Boolean = false,
    val promoDescription: String? = null
)
