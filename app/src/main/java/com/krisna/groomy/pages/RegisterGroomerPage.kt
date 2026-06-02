package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.GroomerRequest
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterGroomerPage(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    var currentStep by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Form States
    var fullName by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var experienceYear by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var portfolioLink by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Menjadi Groomer", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            if (currentStep > 1 && currentStep < 3) currentStep-- 
                            else navController.popBackStack() 
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Progress Stepper
                RegistrationStepper(currentStep = currentStep)

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "RegistrationStep"
                ) { step ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (step) {
                            1 -> StepPersonalData(
                                fullName = fullName,
                                onFullNameChange = { fullName = it },
                                nik = nik,
                                onNikChange = { nik = it },
                                city = city,
                                onCityChange = { city = it },
                                phoneNumber = phoneNumber,
                                onPhoneChange = { phoneNumber = it },
                                onNext = { currentStep = 2 }
                            )
                            2 -> StepProfessionalData(
                                experience = experienceYear,
                                onExperienceChange = { experienceYear = it },
                                portfolio = portfolioLink,
                                onPortfolioChange = { portfolioLink = it },
                                isLoading = isLoading,
                                onNext = {
                                    val token = prefManager.getToken()
                                    if (token != null) {
                                        isLoading = true
                                        scope.launch {
                                            try {
                                                // 1. Panggil API Pendaftaran Groomer
                                                val request = GroomerRequest(
                                                    name = fullName,
                                                    location = city,
                                                    description = "Pengalaman $experienceYear tahun. $portfolioLink",
                                                    phone = phoneNumber
                                                )
                                                val response = RetrofitClient.instance.registerGroomer("Bearer $token", request)
                                                
                                                if (response.isSuccessful) {
                                                    // 2. Jika Berhasil, panggil Refresh Token untuk mendapatkan role terbaru
                                                    val refreshRes = RetrofitClient.instance.refreshToken("Bearer $token")
                                                    
                                                    if (refreshRes.isSuccessful) {
                                                        val newToken = refreshRes.body()?.accessToken
                                                        if (newToken != null) {
                                                            // 3. Simpan Token Baru (dengan Role Groomer)
                                                            prefManager.saveToken(newToken)
                                                            currentStep = 3 // Langsung ke layar sukses
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "Gagal memperbarui data token", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    val error = response.errorBody()?.string()
                                                    Toast.makeText(context, "Gagal Daftar: $error", Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            )
                            3 -> StepSuccess(
                                onFinish = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepIcon(step = 1, currentStep = currentStep, icon = Icons.Default.Person)
        StepLine(isActive = currentStep >= 2)
        StepIcon(step = 2, currentStep = currentStep, icon = Icons.Default.Badge)
        StepLine(isActive = currentStep >= 3)
        StepIcon(step = 3, currentStep = currentStep, icon = Icons.Default.CheckCircle)
    }
}

@Composable
fun StepIcon(step: Int, currentStep: Int, icon: ImageVector) {
    val isActive = currentStep >= step
    val color = if (isActive) Color(0xFF257DEF) else Color(0xFFE2E8F0)
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isActive) color.copy(alpha = 0.1f) else Color.Transparent)
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun RowScope.StepLine(isActive: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .background(if (isActive) Color(0xFF257DEF) else Color(0xFFE2E8F0))
    )
}

@Composable
fun StepPersonalData(
    fullName: String, onFullNameChange: (String) -> Unit,
    nik: String, onNikChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    phoneNumber: String, onPhoneChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Text(
        "Data Pribadi",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        "Lengkapi data diri Anda sesuai KTP untuk proses verifikasi.",
        fontSize = 14.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    LuxuryTextField(value = fullName, onValueChange = onFullNameChange, label = "Nama Bisnis / Toko", icon = Icons.Default.Store)
    LuxuryTextField(value = phoneNumber, onValueChange = onPhoneChange, label = "Nomor Telepon Bisnis", icon = Icons.Default.Phone)
    LuxuryTextField(value = nik, onValueChange = onNikChange, label = "NIK (16 Digit)", icon = Icons.Default.AssignmentInd)
    LuxuryTextField(value = city, onValueChange = onCityChange, label = "Kota Lokasi Bisnis", icon = Icons.Default.LocationCity)

    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF)),
        enabled = fullName.isNotBlank() && phoneNumber.isNotBlank() && city.isNotBlank()
    ) {
        Text("Lanjut", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun StepProfessionalData(
    experience: String, onExperienceChange: (String) -> Unit,
    portfolio: String, onPortfolioChange: (String) -> Unit,
    isLoading: Boolean,
    onNext: () -> Unit
) {
    Text(
        "Keahlian & Pengalaman",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Informasi Keahlian", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Tuliskan pengalaman Anda sebagai groomer profesional agar pelanggan lebih percaya.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }

    LuxuryTextField(value = experience, onValueChange = onExperienceChange, label = "Lama Pengalaman (Tahun)", icon = Icons.Default.Work)
    LuxuryTextField(value = portfolio, onValueChange = onPortfolioChange, label = "Deskripsi Keahlian", icon = Icons.Default.Description)

    Spacer(modifier = Modifier.height(24.dp))
    
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF)),
        enabled = !isLoading && experience.isNotBlank()
    ) {
        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        else Text("Kirim Data Pendaftaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun StepSuccess(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(80.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Pendaftaran Berhasil!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "Profil Groomer Anda telah berhasil dibuat. Sekarang Anda dapat mulai mengelola layanan dan menerima pesanan.",
            textAlign = TextAlign.Center,
            color = Color(0xFF64748B),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
        ) {
            Text("Masuk ke Beranda", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LuxuryTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF257DEF), modifier = Modifier.size(24.dp)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF257DEF),
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
