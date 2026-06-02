package com.krisna.groomy.pages

import android.widget.Toast
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
import com.krisna.groomy.model.OrderResponse
import com.krisna.groomy.model.OrderStatus
import com.krisna.groomy.model.UpdateOrderStatusRequest
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerSchedulePage(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()

    val schedules = remember { mutableStateListOf<OrderResponse>() }
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
                        val response = RetrofitClient.instance.getAllOrders("Bearer $token", groomerId = groomer.id)
                        if (response.isSuccessful) {
                            schedules.clear()
                            response.body()?.let { list ->
                                // Filter hanya yang aktif ditujukan untuk groomer ini dan status belum selesai
                                schedules.addAll(
                                    list.filter { 
                                        it.status != OrderStatus.COMPLETED &&
                                        it.status != OrderStatus.CANCELLED
                                    }.sortedByDescending { it.createdAt }
                                )
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

    fun updateOrderStatus(orderId: Int, newStatus: OrderStatus) {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    val response = RetrofitClient.instance.updateOrderStatus(
                        "Bearer $token",
                        orderId,
                        UpdateOrderStatusRequest(newStatus)
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Status diperbarui!", Toast.LENGTH_SHORT).show()
                        fetchSchedules()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(context, "Gagal: $errorBody", Toast.LENGTH_LONG).show()
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
                    "Tidak ada jadwal aktif",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF64748B)
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
                            onUpdateStatus = { status -> updateOrderStatus(item.id, status) },
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
    item: OrderResponse, 
    onUpdateStatus: (OrderStatus) -> Unit,
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
                    onClick = { onChatClick() },
                    modifier = Modifier.size(40.dp).background(Color(0xFF257DEF).copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat, 
                        contentDescription = "Chat", 
                        modifier = Modifier.size(20.dp), 
                        tint = Color(0xFF257DEF)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadgeSmall(item.status?.name ?: "UNKNOWN")
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
            val status = item.status ?: OrderStatus.SCHEDULED

            when (status) {
                OrderStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            )
                        ) {
                            Text("Tolak", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onUpdateStatus(OrderStatus.CONFIRMED) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
                        ) {
                            Text("Terima", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OrderStatus.SCHEDULED, OrderStatus.CONFIRMED -> {
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.IN_PROGRESS) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Proses", fontWeight = FontWeight.Bold)
                    }
                }

                OrderStatus.IN_PROGRESS -> {
                    Button(
                        onClick = { onUpdateStatus(OrderStatus.COMPLETED) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selesai", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                else -> {
                    Text(text = "Status: ${status.name}", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun StatusBadgeSmall(status: String) {
    val color = when (status.uppercase()) {
        "SCHEDULED", "PENDING" -> Color(0xFFF59E0B)
        "CONFIRMED", "IN_PROGRESS" -> Color(0xFF257DEF)
        "COMPLETED" -> Color(0xFF10B981)
        else -> Color(0xFF94A3B8)
    }
    val label = when(status.uppercase()) {
        "SCHEDULED" -> "TERJADWAL"
        "IN_PROGRESS" -> "PROSES"
        "COMPLETED" -> "SELESAI"
        else -> status
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
