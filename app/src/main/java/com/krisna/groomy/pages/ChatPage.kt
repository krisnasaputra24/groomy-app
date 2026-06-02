package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.krisna.groomy.model.ChatRequest
import com.krisna.groomy.model.ChatResponse
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(navController: NavController, orderId: Int, groomerId: Int, userName: String) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    val chats = remember { mutableStateListOf<ChatResponse>() }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    fun fetchChats() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    // Berdasarkan dokumentasi terbaru, gunakan endpoint /orders/:orderId/chats
                    val response = RetrofitClient.instance.getOrderChats(
                        "Bearer $token",
                        orderId = orderId
                    )
                    if (response.isSuccessful) {
                        val body = response.body() ?: emptyList()
                        if (body.size != chats.size) {
                            chats.clear()
                            chats.addAll(body)
                            // Scroll ke bawah jika ada pesan baru
                            if (chats.isNotEmpty()) {
                                listState.animateScrollToItem(chats.size - 1)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun sendMessage() {
        if (messageText.isBlank()) return
        
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    val response = RetrofitClient.instance.createChat(
                        "Bearer $token",
                        ChatRequest(messageText, groomerId, orderId)
                    )
                    if (response.isSuccessful) {
                        messageText = ""
                        fetchChats()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Polling setiap 3 detik
    LaunchedEffect(Unit) {
        while(true) {
            fetchChats()
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Order #$orderId", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tulis pesan...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF257DEF),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            if (isLoading && chats.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (chats.isEmpty()) {
                Text("Belum ada pesan", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats) { chat ->
                        // Logika sederhana: jika nama user adalah "Admin" atau nama groomer, anggap me?
                        // Berdasarkan JSON, user "Admin" atau field "groomer" terisi menunjukkan pengirim
                        val isMe = chat.user?.name == "Admin" || chat.groomer != null
                        
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                color = if (isMe) Color(0xFF257DEF) else Color.White,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 16.dp
                                ),
                                tonalElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = chat.message,
                                        color = if (isMe) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    val time = (chat.timestamp ?: chat.createdAt ?: "").takeLast(13).take(5)
                                    if (time.isNotEmpty()) {
                                        Text(
                                            text = time,
                                            color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
