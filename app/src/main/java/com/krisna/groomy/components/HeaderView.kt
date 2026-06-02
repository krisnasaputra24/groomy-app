package com.krisna.groomy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@Composable
fun Headerview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

    // Fungsi untuk memformat URL Foto
    fun formatPhotoUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val baseUrl = if (rawUrl.startsWith("http")) rawUrl else "https://groomy-sigma.vercel.app/$rawUrl"
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl${separator}t=${System.currentTimeMillis()}"
    }

    LaunchedEffect(Unit) {
        val savedToken = prefManager.getToken()
        if (savedToken != null) {
            try {
                val response = RetrofitClient.instance.getProfile("Bearer $savedToken")
                if (response.isSuccessful) {
                    profilePictureUrl = formatPhotoUrl(response.body()?.profilePicture)
                }
            } catch (e: Exception) { }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Foto Profile
        Box(
            modifier = Modifier
                .size(50.dp)
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
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Photo",
                    tint = Color(0xFF257DEF),
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Icon Notif
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notification",
                tint = Color(0xFF257DEF)
            )
        }
    }
}
