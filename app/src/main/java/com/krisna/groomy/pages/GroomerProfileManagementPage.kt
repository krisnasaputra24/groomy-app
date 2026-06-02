package com.krisna.groomy.pages

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.AddServiceRequest
import com.krisna.groomy.model.EditGroomerRequest
import com.krisna.groomy.model.ServiceRequest
import com.krisna.groomy.model.ServiceResponse
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerProfileManagementPage(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var groomerId by remember { mutableIntStateOf(0) }
    var groomerName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<Any?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val services = remember { mutableStateListOf<ServiceResponse>() }

    var showServiceDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceResponse?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var serviceToDelete by remember { mutableStateOf<ServiceResponse?>(null) }

    fun formatPhotoUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val baseUrl = if (rawUrl.startsWith("http")) rawUrl else "https://groomy-sigma.vercel.app/$rawUrl"
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl${separator}t=${System.currentTimeMillis()}"
    }

    // Fetch Profile (user/me) and extract Groomer info
    fun fetchData() {
        val token = prefManager.getToken()
        Log.d("DEBUG_API", "fetchData called with token: ${token?.take(10)}...")
        if (token != null) {
            scope.launch {
                try {
                    val response = RetrofitClient.instance.getProfile("Bearer $token")
                    if (response.isSuccessful) {
                        val userProfile = response.body()
                        val groomer = userProfile?.groomers?.firstOrNull()
                        
                        if (groomer != null) {
                            Log.d("DEBUG_API", "Groomer data found: ID=${groomer.id}")
                            groomerId = groomer.id
                            groomerName = groomer.name
                            location = groomer.location
                            description = groomer.description ?: ""
                            profileImageUrl = formatPhotoUrl(groomer.profilePicture)
                            
                            // Fetch Services for this groomer
                            val serviceResponse = RetrofitClient.instance.getAllServices(groomer.id)
                            if (serviceResponse.isSuccessful) {
                                services.clear()
                                serviceResponse.body()?.let { services.addAll(it) }
                            }
                        } else {
                            Log.e("DEBUG_API", "Groomer profile not found in user response")
                            Toast.makeText(context, "Profil Groomer belum dibuat", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("DEBUG_API", "Failed to fetch user profile: ${response.code()}")
                        Toast.makeText(context, "Gagal memuat profil: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("DEBUG_API", "Error in fetchData: ${e.message}")
                    Toast.makeText(context, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { fetchData() }

    val profilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("DEBUG_UI", "Photo selected: $it")
            profileImageUrl = it // Immediate UI update
            
            scope.launch {
                val token = prefManager.getToken()
                if (token != null && groomerId != 0) {
                    isLoading = true
                    try {
                        val file = uriToFile(context, it, "temp_groomer_profile.jpg")
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("profilePicture", file.name, requestFile)
                        
                        Log.d("DEBUG_API", "Uploading profile picture to groomers/$groomerId/profile-picture")
                        val response = RetrofitClient.instance.updateGroomerProfilePicture("Bearer $token", groomerId, body)
                        if (response.isSuccessful) {
                            profileImageUrl = formatPhotoUrl(response.body()?.profilePicture)
                            Toast.makeText(context, "Foto berhasil diunggah!", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("DEBUG_API", "Upload profile picture failed: $errorBody")
                            Toast.makeText(context, "Gagal upload: $errorBody", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("DEBUG_API", "Exception during upload: ${e.message}")
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                } else {
                    Log.e("DEBUG_API", "Cannot upload: token=${token != null}, groomerId=$groomerId")
                    Toast.makeText(context, "ID Groomer tidak ditemukan ($groomerId). Harap daftar kembali.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Kelola Profil & Layanan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Groomer Profile Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .clickable { profilePhotoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUrl != null) {
                                AsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                            }
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = groomerName,
                            onValueChange = { groomerName = it },
                            label = { Text("Nama Bisnis Grooming") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Lokasi") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF257DEF)) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Deskripsi") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                val token = prefManager.getToken()
                                Log.d("DEBUG_UI", "Save button clicked: token=${token?.take(5)}, groomerId=$groomerId")
                                if (token != null && groomerId != 0) {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val request = EditGroomerRequest(
                                                name = groomerName,
                                                location = location,
                                                description = description,
                                                profilePicture = null // Picture is handled separately
                                            )
                                            Log.d("DEBUG_API", "Sending updateGroomerProfile for ID: $groomerId")
                                            val response = RetrofitClient.instance.updateGroomerProfile(
                                                "Bearer $token",
                                                groomerId,
                                                request
                                            )
                                            if (response.isSuccessful) {
                                                Log.d("DEBUG_API", "Update successful")
                                                Toast.makeText(context, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val errorBody = response.errorBody()?.string()
                                                val statusCode = response.code()
                                                Log.e("DEBUG_API", "Update Profile Error: status=$statusCode, body=$errorBody")
                                                Toast.makeText(context, "Gagal ($statusCode): $errorBody", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("DEBUG_API", "Exception during profile update: ${e.message}")
                                            Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    Log.e("DEBUG_UI", "Pre-save check failed: token=${token != null}, groomerId=$groomerId")
                                    Toast.makeText(context, "Profil belum siap: ID Groomer ($groomerId) belum ada", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Services Management Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Layanan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    
                    Button(
                        onClick = {
                            editingService = null
                            showServiceDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(services) { service ->
                EditableServiceCard(
                    service = service,
                    onEdit = {
                        editingService = service
                        showServiceDialog = true
                    },
                    onDelete = {
                        serviceToDelete = service
                        showDeleteConfirm = true
                    }
                )
            }
        }
    }

    if (showDeleteConfirm && serviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Layanan", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus layanan '${serviceToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        val token = prefManager.getToken()
                        if (token != null && serviceToDelete != null) {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.instance.deleteService("Bearer $token", serviceToDelete!!.id)
                                    if (response.isSuccessful) {
                                        services.remove(serviceToDelete)
                                        Toast.makeText(context, "Layanan dihapus", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Gagal menghapus layanan", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    showDeleteConfirm = false
                                    serviceToDelete = null
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showServiceDialog) {
        ServiceEditDialog(
            service = editingService,
            onDismiss = { showServiceDialog = false },
            onSave = { fetchData() }
        )
    }
}

@Composable
fun EditableServiceCard(
    service: ServiceResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                val url = if (service.photo.isNullOrBlank()) null 
                else if (service.photo.startsWith("http")) service.photo 
                else "https://groomy-sigma.vercel.app/${service.photo}"

                if (url != null) {
                    AsyncImage(
                        model = url,
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
                Text(
                    text = service.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(text = "Rp ${service.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF257DEF))
                Text(text = service.description, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1)
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditDialog(
    service: ServiceResponse?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var price by remember { mutableStateOf(service?.price?.toInt()?.toString() ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("DEBUG_UI", "Service photo selected: $it")
            selectedImageUri = it // Show in UI immediately
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (service == null) "Tambah Layanan" else "Edit Layanan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val currentPhotoUrl = if (service?.photo.isNullOrBlank()) null 
                    else if (service!!.photo!!.startsWith("http")) service.photo 
                    else "https://groomy-sigma.vercel.app/${service.photo}"

                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else if (currentPhotoUrl != null) {
                        AsyncImage(model = currentPhotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                            Text("Klik untuk pilih foto", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Layanan") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = price, 
                    onValueChange = { price = it }, 
                    label = { Text("Harga") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val token = prefManager.getToken()
                    if (token != null) {
                        isLoading = true
                        scope.launch {
                            try {
                                val userRes = RetrofitClient.instance.getProfile("Bearer $token")
                                val activeGroomerId = userRes.body()?.groomers?.firstOrNull()?.id ?: 0

                                if (activeGroomerId == 0) {
                                    Toast.makeText(context, "Gagal mendapatkan ID Groomer", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                    return@launch
                                }

                                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                                val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                                val pricePart = price.toRequestBody("text/plain".toMediaTypeOrNull())
                                val groomerIdPart = activeGroomerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                
                                var photoPart: MultipartBody.Part? = null
                                selectedImageUri?.let { uri ->
                                    val file = uriToFile(context, uri, "service_photo.jpg")
                                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                    photoPart = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                                }

                                val response = if (service == null) {
                                    RetrofitClient.instance.addService("Bearer $token", namePart, descPart, pricePart, groomerIdPart, photoPart)
                                } else {
                                    RetrofitClient.instance.updateService("Bearer $token", service.id, namePart, descPart, pricePart, groomerIdPart, photoPart)
                                }

                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                    onSave()
                                    onDismiss()
                                } else {
                                    val errorBody = response.errorBody()?.string()
                                    Log.e("DEBUG_API", "Service Save Error: code=${response.code()}, body=$errorBody")
                                    Toast.makeText(context, "Gagal: $errorBody", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Log.e("DEBUG_API", "Error in Service Save: ${e.message}")
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading && name.isNotBlank() && price.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Batal")
            }
        }
    )
}

private fun uriToFile(context: android.content.Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}
