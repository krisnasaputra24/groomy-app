package com.krisna.groomy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krisna.groomy.pages.Beranda
import com.krisna.groomy.pages.History
import com.krisna.groomy.pages.Layanan
import com.krisna.groomy.pages.Profile


import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun MainScreen(modifier: Modifier = Modifier, navController: NavController) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple("Beranda", Icons.Default.Home, 0),
                    Triple("History", Icons.Default.History, 1),
                    Triple("Layanan", Icons.Default.Pets, 2),
                    Triple("Profile", Icons.Default.Person, 3)
                )

                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        alwaysShowLabel = true,
                        label = { 
                            Text(
                                text = label, 
                                fontWeight = if(selectedItem == index) FontWeight.Bold else FontWeight.Normal,
                                color = if(selectedItem == index) Color(0xFF257DEF) else Color(0xFF94A3B8)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF257DEF),
                            unselectedIconColor = Color(0xFF94A3B8),
                            indicatorColor = Color(0xFF7DD3FC).copy(alpha = 0.2f)
                        ),
                        icon = { Icon(icon, contentDescription = null) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Ganti Box dengan Column agar padding tertangani lebih baik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedItem) {
                    0 -> Beranda(navController = navController)
                    1 -> History()
                    2 -> Layanan(navController = navController)
                    3 -> Profile(navController = navController)
                }
            }
        }
    }
}
