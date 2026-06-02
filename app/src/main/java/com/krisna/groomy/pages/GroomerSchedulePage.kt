package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.BookingResponse
import com.krisna.groomy.model.UpdateBookingStatusRequest
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch
import com.krisna.groomy.model.GroomingStatus
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerSchedulePage(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()

    val schedules = remember { mutableStateListOf<BookingResponse>() }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchSchedules() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    val profileRes = RetrofitClient.instance.getProfile("Bearer $token")
                    val groomer = profileRes.body()?.groomers?.firstOrNull()

                    if (groomer != null) {
                        // Menggunakan endpoint /orders sesuai dokumentasi terbaru
                        val response = RetrofitClient.instance.getAllOrders("Bearer $token")
                        if (response.isSuccessful) {
                            schedules.clear()
                            response.body()?.let {
                                schedules.addAll(it.sortedByDescending { b -> b.date })
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

    fun updateBookingStatus(bookingId: Int, newStatus: String) {
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
                        Toast.makeText(context, "Status diperbarui!", Toast.LENGTH_SHORT).show()
                        fetchSchedules()
                    } else {
                        Toast.makeText(context, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { fetchSchedules() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal & Kendali Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF257DEF)
                )
            } else if (schedules.isEmpty()) {
                Text(
                    "Tidak ada pesanan masuk",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(schedules) { item ->
                        ScheduleControlCard(
                            item = item,
                            onUpdateStatus = { status -> updateBookingStatus(item.id, status) },
                            onChatClick = {
                                navController.navigate("chat/${item.id}/${item.groomerId}/${item.user?.name ?: "Customer"}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleControlCard(
    item: BookingResponse, 
    onUpdateStatus: (String) -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF257DEF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.date.take(10)} • ${item.time}",
                    fontSize = 12.sp,
                    color = Color(0xFF257DEF),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { 
                        println("DEBUG: Navigating to chat/${item.id}/${item.groomerId}/${item.user?.name ?: "Customer"}")
                        onChatClick() 
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat, 
                        contentDescription = "Chat", 
                        modifier = Modifier.size(24.dp), 
                        tint = Color(0xFF257DEF)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadgeSmall(item.status?.name ?: "")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.user?.name ?: "Customer",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                text = item.service?.name ?: "Layanan Grooming",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Kendali Status
            val status = item.status?.name ?: ""

            when (status) {
                // PENDING: Bisa Mulai Grooming (IN_PROGRESS) atau Tolak (REJECTED)
                "PENDING" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateStatus("REJECTED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tolak")
                        }

                        Button(
                            onClick = { onUpdateStatus("IN_PROGRESS") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mulai Grooming")
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
                            onClick = { onUpdateStatus("REJECTED") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tolak")
                        }

                        Button(
                            onClick = { onUpdateStatus("IN_PROGRESS") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mulai Grooming")
                        }
                    }
                }

                // IN_PROGRESS: Selesaikan → COMPLETED
                "IN_PROGRESS" -> {
                    Button(
                        onClick = { onUpdateStatus("COMPLETED") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selesai Grooming")
                    }
                }

                // COMPLETED: Tidak ada aksi
                "COMPLETED" -> {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✅ Grooming Telah Selesai", color = Color(0xFF10B981))
                    }
                }

                // REJECTED: Tidak ada aksi
                "REJECTED" -> {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("❌ Pesanan Ditolak", color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(status: String) {
    val color = when (status.uppercase()) {
        "PENDING" -> Color(0xFFF59E0B)
        "SCHEDULED" -> Color(0xFF8B5CF6)
        "IN_PROGRESS" -> Color(0xFF3B82F6)
        "COMPLETED" -> Color(0xFF10B981)
        "REJECTED" -> Color(0xFFEF4444)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
