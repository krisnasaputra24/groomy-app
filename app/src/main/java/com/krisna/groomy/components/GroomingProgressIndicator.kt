package com.krisna.groomy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThreeStageProgressIndicator(status: String) {
    val stages = listOf("Dalam Antrean", "Proses Grooming", "Selesai")
    val activeIndex = when (status.uppercase()) {
        "PENDING", "ACCEPTED", "SCHEDULED" -> 0
        "IN_PROGRESS" -> 1
        "READY_FOR_PICKUP", "COMPLETED" -> 2
        else -> 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isActive = index <= activeIndex
            val isCurrent = index == activeIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFF257DEF) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage,
                    fontSize = 10.sp,
                    color = if (isActive) Color(0xFF257DEF) else Color(0xFF94A3B8),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }

            if (index < stages.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.5f)
                        .background(if (index < activeIndex) Color(0xFF257DEF) else Color(0xFFE2E8F0))
                        .offset(y = (-10).dp)
                )
            }
        }
    }
}
