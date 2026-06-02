package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.components.ThreeStageProgressIndicator
import com.krisna.groomy.model.BookingResponse
import com.krisna.groomy.model.GroomerResponse
import com.krisna.groomy.model.UpdateBookingStatusRequest
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch
import com.krisna.groomy.model.GroomingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerDashboard(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()

    var groomerInfo by remember { mutableStateOf<GroomerResponse?>(null) }
    val receivedBookings = remember { mutableStateListOf<BookingResponse>() }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchData() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    val profileRes = RetrofitClient.instance.getProfile("Bearer $token")
                    if (profileRes.isSuccessful) {
                        val groomer = profileRes.body()?.groomers?.firstOrNull()
                        groomerInfo = groomer

                        if (groomer != null) {
                            val bookingsRes = RetrofitClient.instance.getAllBookings(
                                "Bearer $token",
                                groomerId = groomer.id
                            )
                            if (bookingsRes.isSuccessful) {
                                val body = bookingsRes.body()
                                receivedBookings.clear()
                                body?.let {
                                    // Sesuai request: Hilangkan yang statusnya COMPLETED atau REJECTED
                                    receivedBookings.addAll(it.filter { b -> 
                                        b.status?.name != "COMPLETED" && b.status?.name != "REJECTED"
                                    }.sortedByDescending { b -> b.createdAt })
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { fetchData() }

    fun updateStatus(bookingId: Int, newStatus: String) {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    val response = RetrofitClient.instance.updateBookingStatus(
                        "Bearer $token",
                        bookingId,
                        UpdateBookingStatusRequest(GroomingStatus.valueOf(newStatus))
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Status diperbarui ke $newStatus", Toast.LENGTH_SHORT).show()
                        fetchData()
                    } else {
                        Toast.makeText(context, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Groomer Dashboard",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF1E293B)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { GroomerHeader(groomerInfo) }

                item {
                    Text(
                        "Kelola Pekerjaan",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (receivedBookings.isEmpty()) {
                    item {
                        Text(
                            "Belum ada pesanan masuk",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(receivedBookings) { booking ->
                        BookingItemDynamic(
                            booking = booking,
                            onStatusUpdate = { status -> updateStatus(booking.id, status) },
                            onChatClick = {
                                navController.navigate("chat/${booking.id}/${booking.groomerId}/${booking.user?.name ?: "Customer"}")
                            }
                        )
                    }
                }

                item {
                    HorizontalDivider(
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    Text(
                        "Menu Kendali",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GroomerMenuRowLight("Profil & Layanan", Icons.Default.Person) {
                            navController.navigate("groomer_profile_management")
                        }
                        GroomerMenuRowLight("Kelola Promo", Icons.Default.Percent) {
                            navController.navigate("groomer_promo_management")
                        }
                        GroomerMenuRowLight("Chat Customer", Icons.AutoMirrored.Filled.Chat) {
                            navController.navigate("groomer_schedule")
                        }
                        GroomerMenuRowLight("Jadwal Grooming", Icons.Default.CalendarMonth) {
                            navController.navigate("groomer_schedule")
                        }
                        GroomerMenuRowLight("Riwayat Transaksi", Icons.Default.ReceiptLong) {
                            navController.navigate("groomer_transactions")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroomerHeader(groomer: GroomerResponse?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (groomer?.profilePicture != null) {
                AsyncImage(
                    model = if (groomer.profilePicture!!.startsWith("http"))
                        groomer.profilePicture
                    else
                        "https://groomy-sigma.vercel.app/${groomer.profilePicture}",
                    contentDescription = null,
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
                text = groomer?.name ?: "Loading...",
                color = Color(0xFF1E293B),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${groomer?.rating ?: 0} (${groomer?.reviews ?: 0} Ulasan)",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BookingItemDynamic(
    booking: BookingResponse, 
    onStatusUpdate: (String) -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.user?.name ?: "Customer",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 17.sp
                    )
                    Text(
                        "${booking.service?.name ?: "Grooming"} • ${booking.time}",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        booking.date.take(10),
                        fontSize = 12.sp,
                        color = Color(0xFF257DEF),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onChatClick() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF257DEF).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            tint = Color(0xFF257DEF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadgeLight(booking.status?.name ?: "UNKNOWN")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ThreeStageProgressIndicator(booking.status?.name ?: "UNKNOWN")
            Spacer(modifier = Modifier.height(16.dp))

            val currentStatus = booking.status?.name ?: "UNKNOWN"

            when (currentStatus) {

                // PENDING: Groomer bisa Mulai Grooming (IN_PROGRESS) atau Tolak (REJECTED)
                "PENDING" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onStatusUpdate("REJECTED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Text("Tolak", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onStatusUpdate("IN_PROGRESS") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF257DEF)
                            )
                        ) {
                            Text(
                                "Mulai Grooming",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // SCHEDULED: Sama seperti PENDING
                "SCHEDULED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onStatusUpdate("REJECTED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Text("Tolak", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onStatusUpdate("IN_PROGRESS") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF257DEF)
                            )
                        ) {
                            Text(
                                "Mulai Grooming",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // IN_PROGRESS: Groomer menyelesaikan → COMPLETED
                "IN_PROGRESS" -> {
                    Button(
                        onClick = { onStatusUpdate("COMPLETED") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Text(
                            "Selesai Grooming",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // COMPLETED: Tidak ada aksi
                "COMPLETED" -> {
                    Text(
                        text = "✅ Grooming Selesai",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }

                // REJECTED: Tidak ada aksi
                "REJECTED" -> {
                    Text(
                        text = "❌ Pesanan Ditolak",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadgeLight(status: String) {
    val color = when (status.uppercase()) {
        "PENDING" -> Color(0xFF64748B)
        "SCHEDULED" -> Color(0xFF8B5CF6)
        "IN_PROGRESS" -> Color(0xFF3B82F6)
        "COMPLETED" -> Color(0xFF10B981)
        "REJECTED" -> Color(0xFFEF4444)
        else -> Color(0xFF94A3B8)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GroomerMenuRowLight(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color(0xFF257DEF),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1)
            )
        }
    }
}
