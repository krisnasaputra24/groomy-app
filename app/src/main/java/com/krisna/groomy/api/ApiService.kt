package com.krisna.groomy.api

import com.krisna.groomy.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTH ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @PATCH("auth/switch-role")
    suspend fun switchRole(@Header("Authorization") token: String): Response<LoginResponse>

    @GET("auth/refresh-token")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<LoginResponse>

    // --- USER ---
    @GET("user/me")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @PATCH("user")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Response<ProfileResponse>

    @Multipart
    @PATCH("user/profile-picture")
    suspend fun updateProfilePicture(
        @Header("Authorization") token: String,
        @Part profilePicture: MultipartBody.Part
    ): Response<ProfileResponse>

    // --- GROOMER ---
    @GET("groomers/{id}")
    suspend fun getGroomerById(
        @Header("Authorization") token: String,
        @Path("id") groomerId: Int
    ): Response<GroomerResponse>

    @GET("groomers")
    suspend fun getAllGroomers(
        @Header("Authorization") token: String
    ): Response<List<GroomerResponse>>

    @POST("groomers")
    suspend fun registerGroomer(
        @Header("Authorization") token: String,
        @Body request: GroomerRequest
    ): Response<GroomerResponse>

    @PATCH("groomers/{id}")
    suspend fun updateGroomerProfile(
        @Header("Authorization") token: String,
        @Path("id") groomerId: Int,
        @Body request: EditGroomerRequest
    ): Response<GroomerResponse>

    @Multipart
    @PATCH("groomers/{id}/profile-picture")
    suspend fun updateGroomerProfilePicture(
        @Header("Authorization") token: String,
        @Path("id") groomerId: Int,
        @Part profilePicture: MultipartBody.Part
    ): Response<GroomerResponse>

    // --- PETS ---
    @GET("pets")
    suspend fun getAllPets(
        @Header("Authorization") token: String
    ): Response<List<PetResponse>>

    @POST("pets")
    suspend fun addPet(
        @Header("Authorization") token: String,
        @Body request: PetRequest
    ): Response<PetResponse>

    @PATCH("pets/{id}")
    suspend fun updatePet(
        @Header("Authorization") token: String,
        @Path("id") petId: Int,
        @Body request: PetRequest
    ): Response<PetResponse>

    @DELETE("pets/{id}")
    suspend fun deletePet(
        @Header("Authorization") token: String,
        @Path("id") petId: Int
    ): Response<Unit>

    @Multipart
    @PATCH("pets/{id}/profile-picture")
    suspend fun updatePetPhoto(
        @Header("Authorization") token: String,
        @Path("id") petId: Int,
        @Part profilePicture: MultipartBody.Part
    ): Response<PetResponse>

    // --- SERVICES ---
    @GET("services")
    suspend fun getAllServices(
        @Query("groomerId") groomerId: Int? = null
    ): Response<List<ServiceResponse>>

    @Multipart
    @POST("services")
    suspend fun addService(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("groomerId") groomerId: RequestBody,
        @Part photo: MultipartBody.Part? = null
    ): Response<ServiceResponse>

    @Multipart
    @PATCH("services/{id}")
    suspend fun updateService(
        @Header("Authorization") token: String,
        @Path("id") serviceId: Int,
        @Part("name") name: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("price") price: RequestBody? = null,
        @Part("groomerId") groomerId: RequestBody? = null,
        @Part photo: MultipartBody.Part? = null
    ): Response<ServiceResponse>

    @DELETE("services/{id}")
    suspend fun deleteService(
        @Header("Authorization") token: String,
        @Path("id") serviceId: Int
    ): Response<Unit>

    // --- ORDERS (Unified System) ---
    @POST("orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>

    @GET("orders")
    suspend fun getAllOrders(
        @Header("Authorization") token: String,
        @Query("groomerId") groomerId: Int? = null,
        @Query("userId") userId: Int? = null
    ): Response<List<OrderResponse>>

    @GET("orders/{id}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<OrderResponse>

    @PATCH("orders/{id}")
    suspend fun updateOrder(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateOrderStatusRequest
    ): Response<OrderResponse>

    // --- PROMOS ---
    @POST("promos")
    suspend fun createPromo(
        @Header("Authorization") token: String,
        @Body request: PromoRequest
    ): Response<PromoResponse>

    @GET("promos")
    suspend fun getAllPromos(
        @Header("Authorization") token: String
    ): Response<List<PromoResponse>>

    @GET("promos/{id}")
    suspend fun getPromoById(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<PromoResponse>

    @PATCH("promos/{id}")
    suspend fun updatePromo(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: PromoRequest
    ): Response<PromoResponse>

    @DELETE("promos/{id}")
    suspend fun deletePromo(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    // --- REVIEWS / RATING ---
    @PATCH("groomers/{id}/rate")
    suspend fun rateGroomer(
        @Header("Authorization") token: String,
        @Path("id") groomerId: Int,
        @Body request: RatingRequest
    ): Response<GroomerResponse>

    // --- CHATS ---
    @POST("chats")
    suspend fun createChat(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @GET("chats")
    suspend fun getChats(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int? = null,
        @Query("groomerId") groomerId: Int? = null,
        @Query("orderId") orderId: Int? = null
    ): Response<List<ChatResponse>>

    @GET("orders/{orderId}/chats")
    suspend fun getOrderChats(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: Int
    ): Response<List<ChatResponse>>

}
