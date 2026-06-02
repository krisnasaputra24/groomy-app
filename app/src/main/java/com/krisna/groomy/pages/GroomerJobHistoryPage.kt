package com.krisna.groomy.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerJobHistoryPage(navController: NavController) {
    val history = listOf(
        JobHistoryItem("Budi", "Ciki", "Full Grooming", "12 Mei, 10:00", "Rp 150.000"),
        JobHistoryItem("Ani", "Momo", "Bath & Brush", "11 Mei, 13:00", "Rp 80.000"),
        JobHistoryItem("Siti", "Lulu", "Nail Clipping", "10 Mei, 14:30", "Rp 45.000"),
        JobHistoryItem("Joko", "Rex", "Full Grooming", "09 Mei, 09:00", "Rp 150.000")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History Pekerjaan", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Pekerjaan", color = Color(0xFF64748B), fontSize = 14.sp)
                    Text("${history.size} Selesai", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            }
            items(history) { item ->
                JobHistoryCard(item)
            }
        }
    }
}

data class JobHistoryItem(
    val customerName: String,
    val petName: String,
    val service: String,
    val dateTime: String,
    val price: String
)

@Composable
fun JobHistoryCard(item: JobHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(text = "${item.petName} • ${item.service}", fontSize = 13.sp, color = Color(0xFF64748B))
                Text(text = item.dateTime, fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            Text(text = item.price, fontWeight = FontWeight.ExtraBold, color = Color(0xFF257DEF), fontSize = 15.sp)
        }
    }
}
