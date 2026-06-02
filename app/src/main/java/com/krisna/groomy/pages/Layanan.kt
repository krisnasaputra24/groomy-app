package com.krisna.groomy.pages

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.components.BannerView
import com.krisna.groomy.model.GroomerResponse
import com.krisna.groomy.model.PromoResponse
import com.krisna.groomy.model.ServiceResponse
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layanan(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedMenu by remember { mutableStateOf("Layanan Populer") }
    var userLocation by remember { mutableStateOf("Jakarta Pusat, DKI Jakarta") }
    var showLocationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val groomers = remember { mutableStateListOf<GroomerResponse>() }
    val services = remember { mutableStateListOf<ServiceResponse>() }
    val promos = remember { mutableStateListOf<PromoResponse>() }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchData() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    
                    // Logic penentuan parameter API berdasarkan menu yang dipilih
                    val sortBy = if (selectedMenu == "Layanan Populer") "rating" else null
                    val order = if (selectedMenu == "Layanan Populer") "desc" else null
                    val apiFilter = if (selectedMenu == "Layanan Populer") "popular" else null

                    // 1. Fetch Services (Sekarang menggunakan filter/sorting dari Backend)
                    val serviceRes = RetrofitClient.instance.getAllServices(
                        sortBy = sortBy,
                        order = order,
                        filter = apiFilter,
                        search = if (searchQuery.isNotBlank()) searchQuery else null
                    )
                    
                    if (serviceRes.isSuccessful) {
                        services.clear()
                        serviceRes.body()?.let { services.addAll(it) }
                    }

                    // 2. Fetch All Promos (Tetap diperlukan untuk filter "Promo" lokal atau badge)
                    val promoRes = RetrofitClient.instance.getAllPromos("Bearer $token")
                    if (promoRes.isSuccessful) {
                        promos.clear()
                        promoRes.body()?.let { promos.addAll(it) }
                    }

                    // 3. Fetch All Groomers (untuk mapping rating di UI)
                    val groomerRes = RetrofitClient.instance.getAllGroomers("Bearer $token")
                    if (groomerRes.isSuccessful) {
                        groomers.clear()
                        groomerRes.body()?.let { groomers.addAll(it) }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Trigger fetch ulang saat menu filter atau pencarian berubah
    LaunchedEffect(selectedMenu) { fetchData() }
    
    // Gunakan debounce untuk pencarian agar tidak terlalu sering menembak API
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            kotlinx.coroutines.delay(500) // Tunggu user selesai mengetik
        }
        fetchData()
    }

    Box(
        modifier = modifier
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 0. Location Picker Header
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Lokasi Kamu",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        modifier = Modifier
                            .clickable { showLocationSheet = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF257DEF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userLocation,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 1. Header & Banner Promo
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Layanan Grooming",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BannerView(promos = promos) // Menampilkan hanya promo dari backend
                }
            }

            // 2. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Cari layanan...", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF257DEF)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF257DEF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            // 3. Menu Filter
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val menus = listOf("Layanan Populer", "Promo", "Groomer Terdekat")
                    items(menus) { menu ->
                        FilterChipLuxury(
                            text = menu,
                            isSelected = selectedMenu == menu,
                            onClick = { selectedMenu = menu }
                        )
                    }
                }
            }

            // 4. Content Section
            item {
                Text(
                    text = selectedMenu,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF257DEF))
                    }
                }
            } else {
                // Logika Filter Lokal (Hanya untuk Promo, lainnya sudah dihandle API)
                val filteredServices = if (selectedMenu == "Promo") {
                    services.filter { service -> promos.any { it.serviceId == service.id } }
                } else {
                    services
                }

                if (filteredServices.isEmpty()) {
                    item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Tidak ada layanan ditemukan", color = Color.Gray) } }
                } else {
                    items(filteredServices) { service ->
                        // Cari data groomer yang sesuai untuk mengambil rating aslinya
                        val groomer = groomers.find { it.id == service.groomerId }
                        val rating = groomer?.rating?.toDouble() ?: 0.0
                        
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            ServiceCardDynamic(
                                service = service,
                                rating = rating
                            ) {
                                // Saat layanan diklik, buka detail groomernya
                                navController.navigate("groomer_detail/${service.groomerId}")
                            }
                        }
                    }
                }
            }
        }

        // Location Selection Sheet
        if (showLocationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLocationSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pilih Lokasi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LocationOption(
                        title = "Gunakan Lokasi Terkini",
                        subtitle = "Aktifkan GPS untuk lokasi presisi",
                        icon = Icons.Default.MyLocation,
                        onClick = {
                            userLocation = "Lokasi Terkini (GPS)"
                            showLocationSheet = false
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LocationOption(
                        title = "Pilih dari Maps",
                        subtitle = "Tentukan lokasi secara manual",
                        icon = Icons.Default.Map,
                        onClick = {
                            userLocation = "Lokasi dari Maps"
                            showLocationSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCardDynamic(service: ServiceResponse, rating: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!service.photo.isNullOrBlank()) {
                    AsyncImage(
                        model = if (service.photo.startsWith("http")) service.photo else "https://groomy-sigma.vercel.app/${service.photo}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(30.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = service.description,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp ${service.price.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF257DEF)
                )
            }
            
            if (rating > 0) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                Text(
                    text = rating.toString(), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                Text(
                    text = "New", 
                    fontSize = 12.sp, 
                    color = Color(0xFF10B981), 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FilterChipLuxury(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF257DEF) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF64748B),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun LocationOption(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF257DEF).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}
