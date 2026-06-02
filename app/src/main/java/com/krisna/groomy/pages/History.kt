package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@Composable
fun History(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    val historyItems = remember { mutableStateListOf<OrderResponse>() }
    var isLoading by remember { mutableStateOf(true) }
    
    val ratedBookings = remember { mutableStateMapOf<Int, Boolean>() }

    fun fetchHistory() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    val response = RetrofitClient.instance.getAllOrders("Bearer $token")
                    if (response.isSuccessful) {
                        historyItems.clear()
                        response.body()?.let { list ->
                            historyItems.addAll(list.sortedByDescending { it.createdAt })
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { fetchHistory() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF257DEF))
        } else if (historyItems.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Belum ada riwayat pesanan", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyItems) { order ->
                    HistoryItemCard(
                        order = order,
                        isRated = ratedBookings[order.id] ?: false,
                        onRateSuccess = { ratedBookings[order.id] = true },
                        onChatClick = {
                            navController.navigate("chat/${order.id}/${order.groomerId}/${order.groomer?.name ?: "Groomer"}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    order: OrderResponse, 
    isRated: Boolean, 
    onRateSuccess: () -> Unit,
    onChatClick: () -> Unit
) {
    val status = order.status ?: OrderStatus.PENDING
    val isCompleted = status == OrderStatus.COMPLETED
    val statusColor = when(status) {
        OrderStatus.COMPLETED -> Color(0xFF10B981)
        OrderStatus.CANCELLED -> Color(0xFFEF4444)
        else -> Color(0xFF3B82F6)
    }

    var showRatingDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { onChatClick() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat",
                        tint = Color(0xFF257DEF),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.date.take(10), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = order.service?.name ?: "Layanan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Groomer: ${order.groomer?.name ?: "Professional"}",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            if (isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                if (!isRated) {
                    Button(
                        onClick = { showRatingDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beri Rating & Ulasan")
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terima kasih!", color = Color(0xFF10B981))
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            groomerId = order.groomerId,
            onDismiss = { showRatingDialog = false },
            onSuccess = {
                showRatingDialog = false
                onRateSuccess()
            }
        )
    }
}

@Composable
fun RatingDialog(groomerId: Int, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var reviewMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Beri Rating & Ulasan") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Bagaimana pengalaman Anda?", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFB800) else Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = reviewMessage,
                    onValueChange = { reviewMessage = it },
                    label = { Text("Tulis ulasan Anda (Opsional)") },
                    placeholder = { Text("Ceritakan pengalaman Anda...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val token = prefManager.getToken()
                    if (token != null) {
                        scope.launch {
                            try {
                                isSubmitting = true
                                val response = RetrofitClient.instance.rateGroomer(
                                    "Bearer $token",
                                    groomerId,
                                    com.krisna.groomy.model.RatingRequest(
                                        rating = rating,
                                        message = reviewMessage
                                    )
                                )
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Terima kasih atas ulasannya!", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                } else {
                                    Toast.makeText(context, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Koneksi error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Kirim Ulasan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
