package com.krisna.groomy.pages

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import coil.compose.SubcomposeAsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.ProfileUpdateRequest
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun formatPhotoUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        // Log untuk debug, lihat di Logcat Android Studio
        Log.d("DEBUG_PHOTO", "Raw URL dari server: $rawUrl")
        
        val baseUrl = if (rawUrl.startsWith("http")) rawUrl else "https://groomy-sigma.vercel.app/$rawUrl"
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl${separator}t=${System.currentTimeMillis()}"
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val token = prefManager.getToken()
                if (token != null) {
                    isLoading = true
                    try {
                        val file = uriToFile(context, it)
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("profilePicture", file.name, requestFile)
                        
                        val response = RetrofitClient.instance.updateProfilePicture("Bearer $token", body)
                        if (response.isSuccessful) {
                            val newPhoto = response.body()?.profilePicture
                            profilePictureUrl = formatPhotoUrl(newPhoto)
                            Toast.makeText(context, "Foto berhasil diunggah!", Toast.LENGTH_SHORT).show()
                        } else {
                            val error = response.errorBody()?.string()
                            Toast.makeText(context, "Gagal upload: $error", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val savedToken = prefManager.getToken()
        if (savedToken != null) {
            try {
                val response = RetrofitClient.instance.getProfile("Bearer $savedToken")
                if (response.isSuccessful) {
                    val profile = response.body()
                    name = profile?.name ?: ""
                    phone = profile?.phone ?: ""
                    profilePictureUrl = formatPhotoUrl(profile?.profilePicture)
                }
            } catch (e: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .background(Color(0xFFF8FAFC)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FOTO PROFIL SECTION
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!profilePictureUrl.isNullOrEmpty()) {
                    // Menggunakan SubcomposeAsyncImage agar bisa menampilkan loading/error
                    SubcomposeAsyncImage(
                        model = profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(modifier = Modifier.scale(0.5f))
                        },
                        error = {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
                        }
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(32.dp))
                }
            }
            Text("Ketuk untuk ganti foto", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor HP") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(context, "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val savedToken = prefManager.getToken()
                            if (savedToken != null) {
                                val token = "Bearer $savedToken" 
                                val request = ProfileUpdateRequest(name, phone)
                                val response = RetrofitClient.instance.updateProfile(token, request)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF))
                        )
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_profile_pic.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}
