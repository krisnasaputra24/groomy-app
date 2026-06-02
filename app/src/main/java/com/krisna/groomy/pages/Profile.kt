package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.PetResponse
import com.krisna.groomy.model.UserProfile
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@Composable
fun Profile(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                name = "Memuat...",
                email = "...",
                phone = "...",
                address = "",
                role = "USER",
                isGroomerApproved = false
            )
        )
    }

    var hasGroomerProfile by remember { mutableStateOf(false) }
    var petList by remember { mutableStateOf<List<PetResponse>>(emptyList()) }

    fun fetchData() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    // Fetch Profile
                    val profileRes = RetrofitClient.instance.getProfile("Bearer $token")
                    if (profileRes.isSuccessful) {
                        profileRes.body()?.let { profile ->
                            android.util.Log.d("DEBUG_API", "User Profile Data: role=${profile.role}, hasGroomer=${!profile.groomers.isNullOrEmpty()}")
                            
                            val roleFromBackend = profile.role ?: "USER"
                            val hasGroomer = !profile.groomers.isNullOrEmpty()

                            // AUTO REFRESH TOKEN: Jika di database sudah ada groomer tapi token masih role USER
                            if (roleFromBackend.uppercase() == "USER" && hasGroomer) {
                                android.util.Log.d("DEBUG_API", "Role mismatch detected! Attempting auto refresh-token...")
                                val refreshRes = RetrofitClient.instance.refreshToken("Bearer $token")
                                if (refreshRes.isSuccessful) {
                                    val newToken = refreshRes.body()?.accessToken
                                    if (newToken != null) {
                                        prefManager.saveToken(newToken)
                                        // Panggil fetchData lagi dengan token baru
                                        fetchData()
                                        return@launch
                                    }
                                }
                            }

                            val rawPhoto = profile.profilePicture
                            val photoUrl = when {
                                rawPhoto.isNullOrEmpty() -> null
                                rawPhoto.startsWith("http") -> "$rawPhoto?t=${System.currentTimeMillis()}"
                                rawPhoto.startsWith("uploads/") -> "https://groomy-sigma.vercel.app/$rawPhoto?t=${System.currentTimeMillis()}"
                                else -> "https://groomy-sigma.vercel.app/uploads/$rawPhoto?t=${System.currentTimeMillis()}"
                            }

                            userProfile = UserProfile(
                                name = profile.name ?: "",
                                email = profile.email ?: "",
                                phone = profile.phone ?: "-",
                                address = "",
                                role = roleFromBackend,
                                isGroomerApproved = true,
                                profilePhotoUrl = photoUrl
                            )
                            
                            hasGroomerProfile = hasGroomer
                        }
                    }

                    // Fetch Pets
                    val petsRes = RetrofitClient.instance.getAllPets("Bearer $token")
                    if (petsRes.isSuccessful) {
                        petList = petsRes.body() ?: emptyList()
                    }
                } catch (e: Exception) { }
            }
        }
    }

    LaunchedEffect(Unit) { fetchData() }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "Profil Saya",
                    color = Color(0xFF1E293B),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userProfile.profilePhotoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = userProfile.profilePhotoUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = Color(0xFF257DEF)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(userProfile.name, color = Color(0xFF1E293B), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(userProfile.email, color = Color(0xFF64748B), fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        ProfileDetailRowLight("No. HP", userProfile.phone)
                        ProfileDetailRowLight("Status Akun", userProfile.role)

                        Spacer(modifier = Modifier.height(32.dp))

                        val userRole = userProfile.role.uppercase()
                        if (userRole == "GROOMER" || userRole == "ADMIN" || hasGroomerProfile) {
                            Button(
                                onClick = { navController.navigate("groomer_dashboard") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
                                        )
                                    ),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Groomer Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        } else {
                            Button(
                                onClick = { navController.navigate("register_groomer") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                        )
                                    ),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Daftar Menjadi Groomer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { navController.navigate("edit_profile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF))
                                    )
                                ),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Edit Profil", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hewan Peliharaan", color = Color(0xFF1E293B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { navController.navigate("add_pet") },
                        modifier = Modifier.clip(CircleShape).background(Color(0xFF257DEF).copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF257DEF))
                    }
                }
            }

            if (petList.isEmpty()) {
                item {
                    Text("Belum ada hewan peliharaan", color = Color.Gray, fontSize = 14.sp)
                }
            }

            items(petList) { pet ->
                PetCardItem(pet) {
                    navController.navigate("edit_pet/${pet.id}/${pet.name}/${pet.type}/${pet.breed ?: "-"}/${pet.age ?: 0}/0")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
                TextButton(onClick = { 
                    prefManager.clear()
                    navController.navigate("login") { popUpTo(0) } 
                }) {
                    Text("Keluar dari Akun", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRowLight(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 14.sp)
        Text(text = value, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun PetCardItem(pet: PetResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (!pet.photo.isNullOrEmpty()) {
                    AsyncImage(
                        model = pet.photo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = pet.name, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = "${pet.type} • ${pet.breed ?: "-"}", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1))
        }
    }
}
