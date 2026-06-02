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
import androidx.compose.material.icons.filled.Schedule
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
import com.krisna.groomy.components.ThreeStageProgressIndicator
import com.krisna.groomy.model.GroomerService
import com.krisna.groomy.model.PromoResponse
import com.krisna.groomy.model.ServiceResponse
import com.krisna.groomy.model.OrderResponse
import com.krisna.groomy.model.OrderStatus
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

    val groomers = remember { mutableStateListOf<com.krisna.groomy.model.GroomerResponse>() }

    val activePromos = remember { mutableStateListOf<PromoResponse>() }
    var isLoadingPromos by remember { mutableStateOf(true) }
    
    var activeOrder by remember { mutableStateOf<OrderResponse?>(null) }
    var isLoadingActiveOrder by remember { mutableStateOf(false) }

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
        val token = prefManager.getToken()
        scope.launch {
            try {
                isLoadingServices = true
                // Mengambil layanan terbaru dengan sorting dari backend
                val response = RetrofitClient.instance.getAllServices(
                    sortBy = "createdAt",
                    order = "desc"
                )
                if (response.isSuccessful) {
                    allServices.clear()
                    response.body()?.let { allServices.addAll(it) }
                }

                // Ambil data groomer untuk mendapatkan rating
                if (token != null) {
                    val groomerRes = RetrofitClient.instance.getAllGroomers("Bearer $token")
                    if (groomerRes.isSuccessful) {
                        groomers.clear()
                        groomerRes.body()?.let { groomers.addAll(it) }
                    }
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
                if (token != null) {
                    val response = RetrofitClient.instance.getAllPromos("Bearer $token")
                    if (response.isSuccessful) {
                        activePromos.clear()
                        response.body()?.let { activePromos.addAll(it) }
                    }
                }
            } catch (e: Exception) { }
            finally {
                isLoadingPromos = false
            }
        }
    }

    fun fetchActiveOrder() {
        val savedToken = prefManager.getToken()
        if (savedToken != null) {
            scope.launch {
                try {
                    isLoadingActiveOrder = true
                    val response = RetrofitClient.instance.getAllOrders("Bearer $savedToken")
                    if (response.isSuccessful) {
                        // Ambil semua order yang aktif (bukan COMPLETED/CANCELLED) 
                        // dan urutkan berdasarkan tanggal terdekat
                        val activeOrdersList = response.body()?.filter { 
                            it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED
                        }?.sortedBy { it.date + it.time }
                        
                        activeOrder = activeOrdersList?.firstOrNull()
                    }
                } catch (e: Exception) { }
                finally {
                    isLoadingActiveOrder = false
                }
            }
        }
    }

    // Mengambil data saat halaman dibuka
    LaunchedEffect(Unit) {
        fetchProfile()
        fetchAllServices()
        fetchActiveOrder()
        fetchPromos()
    }

    // Refresh data saat kembali ke halaman ini
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchProfile()
                fetchAllServices()
                fetchActiveOrder()
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

            item {
                BannerView(promos = promoBanners)
            }

            if (activeOrder != null) {
                item {
                    ActiveGroomingCard(
                        petName = activeOrder?.service?.name ?: "Pet",
                        status = activeOrder?.status?.name ?: "PENDING",
                        progress = 0.5f,
                        groomerPhone = activeOrder?.groomer?.phone,
                        onChatClick = {
                            navController.navigate("chat/${activeOrder?.id}/${activeOrder?.groomerId}/${activeOrder?.groomer?.name ?: "Groomer"}")
                        }
                    )
                }
            }

            item {
                RecommendationCardLight(
                    title = "Layanan Terbaru",
                    subtitle = "Baru saja ditambahkan",
                    icon = Icons.Default.AutoAwesome
                ) {
                    if (isLoadingServices) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else if (allServices.isEmpty()) {
                        Text("Belum ada layanan tersedia", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            allServices.take(3).forEach { service ->
                                // Temukan rating groomer untuk layanan ini
                                val groomer = groomers.find { it.id == service.groomerId }
                                val rating = groomer?.rating ?: 0
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("groomer_detail/${service.groomerId}") },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail Mini
                                    Box(
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val photoUrl = if (service.photo.isNullOrBlank()) null 
                                                      else if (service.photo.startsWith("http")) service.photo 
                                                      else "https://groomy-sigma.vercel.app/${service.photo}"
                                        
                                        if (photoUrl != null) {
                                            AsyncImage(
                                                model = photoUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = service.name,
                                            color = Color(0xFF1E293B),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Rp ${service.price.toInt()}",
                                            color = Color(0xFF257DEF),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    
                                    if (rating > 0) {
                                        Icon(
                                            Icons.Default.Star, 
                                            contentDescription = null, 
                                            tint = Color(0xFFFFB800), 
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = rating.toString(),
                                            modifier = Modifier.padding(start = 4.dp),
                                            color = Color(0xFF1E293B),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "New",
                                            color = Color(0xFF10B981),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (service != allServices.take(3).last()) {
                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                RecommendationCardLight(
                    title = "Jadwal Terdekat",
                    subtitle = "Jangan sampai terlewat",
                    icon = Icons.Default.CalendarMonth
                ) {
                    if (isLoadingActiveOrder) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    } else if (activeOrder != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    // Bisa diarahkan ke halaman detail order jika ada
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF257DEF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${activeOrder?.date?.take(10)} • ${activeOrder?.time}",
                                    color = Color(0xFF1E293B),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when(activeOrder?.status) {
                                                OrderStatus.PENDING -> Color(0xFFF59E0B)
                                                OrderStatus.CONFIRMED, OrderStatus.IN_PROGRESS -> Color(0xFF257DEF)
                                                else -> Color(0xFF64748B)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${activeOrder?.service?.name} - ${activeOrder?.groomer?.name}",
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        Text(
                            "Belum ada jadwal booking aktif",
                            color = Color(0xFF64748B),
                            fontSize = 15.sp
                        )
                    }
                }
            }

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
fun ActiveGroomingCard(
    petName: String, 
    status: String, 
    progress: Float, 
    groomerPhone: String? = null,
    onChatClick: () -> Unit
) {
    val context = LocalContext.current
    val statusColor = when (status.uppercase()) {
        "CONFIRMED", "IN_PROGRESS" -> Color(0xFF257DEF)
        "READY_FOR_PICKUP" -> Color(0xFFEA580C)
        "COMPLETED" -> Color(0xFF10B981)
        else -> Color(0xFF64748B)
    }

    val statusText = when (status.uppercase()) {
        "PENDING" -> "Menunggu Konfirmasi"
        "CONFIRMED" -> "Sedang Dimandikan"
        "IN_PROGRESS" -> "Sedang Dimandikan"
        "READY_FOR_PICKUP" -> "Siap Dijemput"
        "COMPLETED" -> "Selesai"
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
                    IconButton(
                        onClick = { onChatClick() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF257DEF).copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Internal Chat",
                            tint = Color(0xFF257DEF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
