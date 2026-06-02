package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.BookingResponse
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@Composable
fun History() {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    val historyItems = remember { mutableStateListOf<BookingResponse>() }
    var isLoading by remember { mutableStateOf(true) }
    
    // Track rated bookings locally or assume if rating exists in response
    // (Note: Using a simple Map to simulate local tracking of 'isRated')
    val ratedBookings = remember { mutableStateMapOf<Int, Boolean>() }

    fun fetchHistory() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    val profileRes = RetrofitClient.instance.getProfile("Bearer $token")
                    val userId = profileRes.body()?.id
                    if (userId != null) {
                        val response = RetrofitClient.instance.getAllBookings("Bearer $token", userId = userId)
                        if (response.isSuccessful) {
                            historyItems.clear()
                            response.body()?.let { list ->
                                historyItems.addAll(list.sortedByDescending { it.updatedAt })
                            }
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
                items(historyItems) { booking ->
                    HistoryItemCard(
                        booking = booking,
                        isRated = ratedBookings[booking.id] ?: false, // Check if rated in this session
                        onRateSuccess = { ratedBookings[booking.id] = true }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(booking: BookingResponse, isRated: Boolean, onRateSuccess: () -> Unit) {
    val status = booking.status?.name ?: "UNKNOWN"
    val isCompleted = status == "COMPLETED"
    val statusColor = when(status) {
        "COMPLETED" -> Color(0xFF10B981)
        "REJECTED" -> Color(0xFFEF4444)
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
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(booking.date.take(10), fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = booking.service?.name ?: "Layanan Grooming",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Groomer: ${booking.groomer?.name ?: "Professional"}",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            if (isCompleted) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(16.dp))

                // Logika Rating: Hanya tampil jika belum dirating
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
                        Text("Terima kasih atas ulasan Anda! ✨", color = Color(0xFF10B981))
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            groomerId = booking.groomerId,
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
    var rating by remember { mutableStateOf(5) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Beri Rating Groomer") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Bagaimana pengalaman Anda?", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFB800) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
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
                                    mapOf("rating" to rating)
                                )
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Terima kasih atas ratingnya!", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal mengirim rating", Toast.LENGTH_SHORT).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Kirim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
