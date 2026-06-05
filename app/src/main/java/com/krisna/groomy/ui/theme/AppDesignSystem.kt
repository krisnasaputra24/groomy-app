package com.krisna.groomy.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Groomy Design System
 * Centralized variables for colors, icons, and gradients.
 */

object AppColors {
    val Primary = Color(0xFF257DEF)
    val Secondary = Color(0xFF7DD3FC)
    val Accent = Color(0xFFFACC15)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Background = Color(0xFFF8FAFC)
    val Surface = Color.White
    val TextPrimary = Color(0xFF1E293B)
    val TextSecondary = Color(0xFF64748B)
    val Border = Color(0xFFE2E8F0)
}

object AppIcons {
    val Pet = Icons.Default.Pets
    val Diagnosis = Icons.Default.AutoAwesome
    val Chat = Icons.AutoMirrored.Filled.Chat
    val Profile = Icons.Default.Person
    val Search = Icons.Default.Search
    val Location = Icons.Default.LocationOn
    val Calendar = Icons.Default.CalendarMonth
    val Schedule = Icons.Default.Schedule
    val Star = Icons.Default.Star
    val Notification = Icons.Default.Notifications
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val History = Icons.Default.History
    val Check = Icons.Default.CheckCircle
    val Add = Icons.Default.Add
    val Edit = Icons.Default.Edit
    val Delete = Icons.Default.Delete
}

object AppGradients {
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(AppColors.Secondary, AppColors.Primary)
    )
}

object AppShapes {
    val Medium = RoundedCornerShape(16.dp)
    val Large = RoundedCornerShape(24.dp)
}
