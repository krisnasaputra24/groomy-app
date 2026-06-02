package com.krisna.groomy.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krisna.groomy.components.BannerView
import com.krisna.groomy.components.Headerview
import com.krisna.groomy.components.ThreeStageProgressIndicator
import com.krisna.groomy.model.GroomerService
import com.krisna.groomy.model.PromoResponse
import com.krisna.groomy.model.ServiceResponse
import com.krisna.groomy.model.BookingResponse
import android.content.Intent
import android.net.Uri

import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun Beranda(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    var userName by remember { mutableStateOf("Krisna") }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val allServices = remember { mutableStateListOf<ServiceResponse>() }
    var isLoadingServices by remember { mutableStateOf(true) }

    val activePromos = remember { mutableStateListOf<PromoResponse>() }
    var isLoadingPromos by remember { mutableStateOf(true) }
    
    var activeBooking by remember { mutableStateOf<BookingResponse?>(null) }
    var isLoadingActiveBooking by remember { mutableStateOf(false) }

    // Fungsi untuk memformat URL Foto (Sama seperti di Profile & EditProfile)
    fun formatPhotoUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val baseUrl = if (rawUrl.startsWith("http")) rawUrl else "https://groomy-sigma.vercel.app/$rawUrl"
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl${separator}t=${System.currentTimeMillis()}"
    }

    // Fungsi untuk mengambil data profile
    fun fetchProfile() {
        val savedToken = prefManager.getToken()
        if (savedToken != null) {
            scope.launch {
                try {
                    val response = RetrofitClient.instance.getProfile("Bearer $savedToken")
                    if (response.isSuccessful) {
                        val profile = response.body()
                        userName = profile?.name?.split(" ")?.get(0) ?: "Krisna" 
                        profilePictureUrl = formatPhotoUrl(profile?.profilePicture)
                    }
                } catch (e: Exception) { }
            }
        }
    }

    fun fetchAllServices() {
        scope.launch {
            try {
                isLoadingServices = true
                val response = RetrofitClient.instance.getAllServices(null)
                if (response.isSuccessful) {
                    allServices.clear()
                    response.body()?.let { allServices.addAll(it) }
                }
            } catch (e: Exception) { }
            finally {
                isLoadingServices = false
            }
        }
    }

    fun fetchPromos() {
        val token = prefManager.getToken()
        scope.launch {
            try {
                isLoadingPromos = true
                val response = if (token != null) {
                    RetrofitClient.instance.getAllPromos("Bearer $token")
                } else {
                    null
                }
                
                if (response?.isSuccessful == true) {
                    activePromos.clear()
                    response.body()?.let { activePromos.addAll(it) }
                }
            } catch (e: Exception) { }
            finally {
                isLoadingPromos = false
            }
        }
    }

    fun fetchActiveBooking() {
        val savedToken = prefManager.getToken()
        if (savedToken != null) {
            scope.launch {
                try {
                    isLoadingActiveBooking = true
                    val profileRes = RetrofitClient.instance.getProfile("Bearer $savedToken")
                    if (profileRes.isSuccessful) {
                        val userId = profileRes.body()?.id
                        if (userId != null) {
                            val response = RetrofitClient.instance.getAllBookings("Bearer $savedToken", userId = userId)
                            if (response.isSuccessful) {
                                // Ambil booking terbaru yang statusnya bukan COMPLETED atau REJECTED
                                activeBooking = response.body()?.firstOrNull { 
                                    it.status?.name != "COMPLETED" && it.status?.name != "REJECTED"
                                }
                            }
                        }
                    }
                } catch (e: Exception) { }
                finally {
                    isLoadingActiveBooking = false
                }
            }
        }
    }

    // Mengambil data saat halaman dibuka
    LaunchedEffect(Unit) {
        fetchProfile()
        fetchAllServices()
        fetchActiveBooking()
        fetchPromos()
    }

    // Refresh data saat kembali ke halaman ini
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchProfile()
                fetchAllServices()
                fetchActiveBooking()
                fetchPromos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Convert ServiceResponse to PromoResponse for Banner compatibility if no active promos
    val promoBanners = if (activePromos.isNotEmpty()) {
        activePromos
    } else {
        allServices.take(3).map { 
            PromoResponse(
                id = it.id,
                code = "GROOMY",
                description = it.description,
                discount = 0,
                serviceId = it.id,
                service = it,
                expiryDate = "2026-12-31"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            // 1. Premium Light Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile Photo Placeholder
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF257DEF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePictureUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profilePictureUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF257DEF),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Halo, $userName!",
                                color = Color(0xFF1E293B),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Berikan perawatan terbaik hari ini",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF257DEF))
                    }
                }
            }

            // 2. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari layanan atau produk...", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF257DEF)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF257DEF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                )
            }

            // 3. Banner
            item {
                BannerView(promos = promoBanners)
            }

            // 3.5 Active Grooming Status
            if (activeBooking != null) {
                item {
                    ActiveGroomingCard(
                        petName = activeBooking?.service?.name ?: "Pet",
                        status = activeBooking?.status?.name ?: "PENDING",
                        progress = 0.5f, // Handled by 3-stage indicator internally
                        groomerPhone = activeBooking?.groomer?.phone
                    )
                }
            }

            // 4. Recommendation Section (Dynamic from Services)
            item {
                RecommendationCardLight(
                    title = "Layanan Terbaru",
                    subtitle = "Dari groomer profesional",
                    icon = Icons.Default.AutoAwesome
                ) {
                    if (isLoadingServices) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else if (allServices.isEmpty()) {
                        Text("Belum ada layanan tersedia", color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allServices.take(3).forEach { service ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${service.name} - Rp ${service.price.toInt()}",
                                        color = Color(0xFF475569),
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. ML Jadwal (Still Placeholder)
            item {
                RecommendationCardLight(
                    title = "Jadwal Terdekat",
                    subtitle = "Jangan sampai terlewat",
                    icon = Icons.Default.CalendarMonth
                ) {
                    Text(
                        "Belum ada jadwal booking aktif",
                        color = Color(0xFF64748B),
                        fontSize = 15.sp
                    )
                }
            }

            // 6. Care Insight
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF7DD3FC).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF257DEF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Care Insight",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Menyikat gigi anjing secara teratur dapat mencegah penumpukan karang gigi dan penyakit gusi.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveGroomingCard(petName: String, status: String, progress: Float, groomerPhone: String? = null) {
    val context = LocalContext.current
    val statusColor = when (status) {
        "In Progress" -> Color(0xFF257DEF)
        "READY_FOR_PICKUP" -> Color(0xFFEA580C)
        else -> Color(0xFF64748B)
    }

    val statusText = when (status) {
        "ACCEPTED" -> "Pesanan Diterima"
        "IN_PROGRESS" -> "Sedang Dimandikan"
        "READY_FOR_PICKUP" -> "Siap Dijemput ✨"
        else -> status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Pets, 
                            contentDescription = null, 
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Status Grooming $petName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!groomerPhone.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                val url = "https://api.whatsapp.com/send?phone=$groomerPhone"
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(url)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366).copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat WhatsApp",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (status == "READY_FOR_PICKUP") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { /* Navigasi ke Maps jika perlu */ },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Jemput", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Unified 3-Stage Progress Indicator
            ThreeStageProgressIndicator(status)
        }
    }
}

@Composable
fun RecommendationCardLight(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF257DEF),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}
