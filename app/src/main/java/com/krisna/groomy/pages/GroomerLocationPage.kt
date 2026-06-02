package com.krisna.groomy.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerLocationPage(navController: NavController) {
    var selectedLocation by remember { mutableStateOf("Jl. Merdeka No. 123, Bandung") }
    var isLocating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lokasi Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Search Bar Placeholder
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Cari lokasi...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF257DEF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF257DEF),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            // Map Simulation Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0)), // Placeholder for Map
                contentAlignment = Alignment.Center
            ) {
                // Background Grid/Pattern to simulate map
                Column(modifier = Modifier.fillMaxSize()) {
                    repeat(20) {
                        Row {
                            repeat(10) {
                                Box(modifier = Modifier.size(50.dp).border(0.5.dp, Color.White.copy(alpha = 0.5f)))
                            }
                        }
                    }
                }
                
                // Map Marker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationOn, 
                        contentDescription = null, 
                        tint = Color(0xFFEF4444), 
                        modifier = Modifier.size(48.dp)
                    )
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Lokasi Dipilih", 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Current Location Button
                FloatingActionButton(
                    onClick = { 
                        isLocating = true
                        selectedLocation = "Menentukan lokasi terkini..."
                        // Simulate delay
                        /* kotlinx.coroutines.delay(2000) */
                        selectedLocation = "Dago Highland, Bandung"
                        isLocating = false
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = Color.White,
                    contentColor = Color(0xFF257DEF)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF257DEF), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
                    }
                }
            }

            // Bottom Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Detail Lokasi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF257DEF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF257DEF))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedLocation,
                            fontSize = 14.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
                    ) {
                        Text("Simpan Lokasi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
