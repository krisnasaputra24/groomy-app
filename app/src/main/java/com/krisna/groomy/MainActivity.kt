package com.krisna.groomy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.krisna.groomy.navigation.AppNavigation
import com.krisna.groomy.pages.Beranda
import com.krisna.groomy.ui.theme.GroomyTheme
import com.krisna.groomy.utils.CloudinaryHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryHelper.init(this)
        enableEdgeToEdge()
        setContent {
            GroomyTheme {
                AppNavigation()
            }
        }
    }
}
