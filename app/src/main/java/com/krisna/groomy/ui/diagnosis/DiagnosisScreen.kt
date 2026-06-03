package com.krisna.groomy.ui.diagnosis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(viewModel: DiagnosisViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Asisten Groomy AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF257DEF)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Ceritakan Keluhan Hewan Anda",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Text(
                "AI kami akan membantu menganalisis perawatan yang tepat.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.textInput,
                onValueChange = { viewModel.onTextInputChange(it) },
                label = { Text("Masukkan keluhan (contoh: kucing saya gatal dan ada kutu)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading,
                placeholder = { Text("Gunakan kalimat lengkap agar hasil lebih akurat.") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.analyzeComplaint() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState.textInput.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analisis Keluhan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = uiState.isResultVisible) {
                ResultCard(
                    prediction = uiState.prediction,
                    confidence = uiState.confidence,
                    recommendation = uiState.recommendation
                )
            }
        }
    }
}

@Composable
fun ResultCard(prediction: String, confidence: Float, recommendation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("🐾 Hasil Analisis AI", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF257DEF))
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Masalah Utama:", color = Color.Gray, fontSize = 12.sp)
            Text(prediction, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1E293B))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Akurasi Deteksi:", color = Color.Gray, fontSize = 12.sp)
            LinearProgressIndicator(
                progress = { confidence / 100f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                color = if (confidence > 70) Color(0xFF10B981) else Color(0xFFF59E0B),
                trackColor = Color.White
            )
            Text(String.format(Locale.getDefault(), "%.1f%%", confidence), fontWeight = FontWeight.Bold, color = if (confidence > 70) Color(0xFF10B981) else Color(0xFFF59E0B))
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Saran Penanganan:", color = Color.Gray, fontSize = 12.sp)
            Text(
                recommendation,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultCardPreview() {
    MaterialTheme {
        ResultCard(
            prediction = "Infeksi Jamur",
            confidence = 89.2f,
            recommendation = "Gunakan sampo antijamur ketoconazole. Isolasi dari hewan lain karena jamur bisa menular."
        )
    }
}
