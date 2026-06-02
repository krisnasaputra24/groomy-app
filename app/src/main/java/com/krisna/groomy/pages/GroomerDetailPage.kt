package com.krisna.groomy.pages

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.CreateOrderRequest
import com.krisna.groomy.model.GroomerResponse
import com.krisna.groomy.model.PetResponse
import com.krisna.groomy.model.ServiceResponse
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerDetailPage(
    navController: NavController,
    groomerId: Int
) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()

    var groomerInfo by remember { mutableStateOf<GroomerResponse?>(null) }
    val services = remember { mutableStateListOf<ServiceResponse>() }
    val pets = remember { mutableStateListOf<PetResponse>() }
    val promos = remember { mutableStateListOf<com.krisna.groomy.model.PromoResponse>() }
    
    var selectedService by remember { mutableStateOf<ServiceResponse?>(null) }
    var selectedPet by remember { mutableStateOf<PetResponse?>(null) }
    var selectedPromo by remember { mutableStateOf<com.krisna.groomy.model.PromoResponse?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isBookingLoading by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    fun fetchInitialData() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    val groomerRes = RetrofitClient.instance.getGroomerById("Bearer $token", groomerId)
                    if (groomerRes.isSuccessful) groomerInfo = groomerRes.body()

                    val servicesRes = RetrofitClient.instance.getAllServices(groomerId)
                    if (servicesRes.isSuccessful) {
                        services.clear()
                        servicesRes.body()?.let { services.addAll(it) }
                    }
                    
                    val petsRes = RetrofitClient.instance.getAllPets("Bearer $token")
                    if (petsRes.isSuccessful) {
                        pets.clear()
                        petsRes.body()?.let { pets.addAll(it) }
                    }
                    
                    val promosRes = RetrofitClient.instance.getAllPromos("Bearer $token", groomerId = groomerId)
                    if (promosRes.isSuccessful) {
                        promos.clear()
                        promosRes.body()?.let { promos.addAll(it) }
                    }
                    
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(groomerId) { fetchInitialData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Groomer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF257DEF))
            }
        } else if (groomerInfo == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Data groomer tidak ditemukan")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8FAFC)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!groomerInfo!!.profilePicture.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = if (groomerInfo!!.profilePicture!!.startsWith("http")) groomerInfo!!.profilePicture else "https://groomy-sigma.vercel.app/${groomerInfo!!.profilePicture}",
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = groomerInfo!!.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${groomerInfo!!.rating} (${groomerInfo!!.reviews} reviews)", fontSize = 14.sp, color = Color(0xFF64748B))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = "Lokasi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text(text = groomerInfo!!.location, fontSize = 14.sp, color = Color(0xFF257DEF))
                        }
                    }
                }

                item {
                    Text("Pilih Hewan Peliharaan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))
                    if (pets.isEmpty()) {
                        Text("Belum ada hewan. Harap tambah hewan di profil.", color = Color.Red, fontSize = 12.sp)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pets) { pet ->
                                PetSelectionCard(
                                    pet = pet,
                                    isSelected = selectedPet == pet,
                                    onSelect = { selectedPet = pet }
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Waktu Kedatangan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Text(if (selectedDate.isEmpty()) "Pilih Tanggal" else selectedDate)
                        }
                        OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Text(if (selectedTime.isEmpty()) "Pilih Jam" else selectedTime)
                        }
                    }
                }

                item {
                    Text("Pilih Layanan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }

                items(services) { service ->
                    ServiceDetailCardDynamic(
                        service = service,
                        isSelected = selectedService == service,
                        onSelect = { 
                            selectedService = service 
                            // Reset selected promo if not applicable to new service
                            if (selectedPromo?.serviceId != service.id) {
                                selectedPromo = null
                            }
                        }
                    )
                }

                if (selectedService != null) {
                    val applicablePromos = promos.filter { it.serviceId == selectedService!!.id }
                    if (applicablePromos.isNotEmpty()) {
                        item {
                            Text("Promo Tersedia", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(applicablePromos) { promo ->
                                    PromoSelectionCard(
                                        promo = promo,
                                        isSelected = selectedPromo == promo,
                                        onSelect = { 
                                            selectedPromo = if (selectedPromo == promo) null else promo 
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Ringkasan Pembayaran
                    if (selectedService != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Harga Layanan", color = Color(0xFF64748B))
                                    Text("Rp ${selectedService!!.price.toInt()}", fontWeight = FontWeight.Bold)
                                }
                                if (selectedPromo != null) {
                                    val discountAmount = (selectedService!!.price * selectedPromo!!.discount / 100).toInt()
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Promo (${selectedPromo!!.code})", color = Color(0xFF10B981))
                                        Text("- Rp $discountAmount", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White)
                                    val totalPrice = (selectedService!!.price - discountAmount).toInt()
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Pembayaran", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Text("Rp $totalPrice", fontWeight = FontWeight.ExtraBold, color = Color(0xFF257DEF), fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { 
                            val token = prefManager.getToken()
                            if (token != null && selectedService != null && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                                isBookingLoading = true
                                scope.launch {
                                    try {
                                        val request = CreateOrderRequest(
                                            date = selectedDate,
                                            time = selectedTime,
                                            groomerId = groomerId,
                                            serviceId = selectedService!!.id,
                                            petId = selectedPet?.id,
                                            promoId = selectedPromo?.id
                                        )
                                        val response = RetrofitClient.instance.createOrder("Bearer $token", request)
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Pesanan Berhasil!", Toast.LENGTH_LONG).show()
                                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                                        } else {
                                            Toast.makeText(context, "Gagal: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isBookingLoading = false
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Harap lengkapi semua pilihan", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = selectedService != null && selectedDate.isNotEmpty() && !isBookingLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
                    ) {
                        if (isBookingLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Konfirmasi Pesanan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = date
                        selectedDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Batal") } },
            title = { Text("Pilih Jam") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
fun PetSelectionCard(pet: PetResponse, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.size(100.dp).clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF257DEF) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                if (!pet.photo.isNullOrEmpty()) {
                    AsyncImage(
                        model = if (pet.photo!!.startsWith("http")) pet.photo else "https://groomy-sigma.vercel.app/${pet.photo}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF257DEF))
                }
                if (isSelected) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF257DEF).copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = pet.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1)
        }
    }
}

@Composable
fun PromoSelectionCard(
    promo: com.krisna.groomy.model.PromoResponse,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFDCFCE7) else Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF10B981) else Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (isSelected) Color(0xFF10B981) else Color.LightGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = promo.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Diskon ${promo.discount}%", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Text(text = "Hingga ${promo.expiryDate.take(10)}", fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun ServiceDetailCardDynamic(
    service: ServiceResponse,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF257DEF) else Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                if (!service.photo.isNullOrBlank()) {
                    AsyncImage(
                        model = if (service.photo!!.startsWith("http")) service.photo else "https://groomy-sigma.vercel.app/${service.photo}",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF94A3B8))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = service.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(text = service.description, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1)
            }
            Text(text = "Rp ${service.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF257DEF))
        }
    }
}
