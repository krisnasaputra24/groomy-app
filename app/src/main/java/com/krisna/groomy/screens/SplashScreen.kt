package com.krisna.groomy.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import com.krisna.groomy.R
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator

@Composable
fun SplashScreen(navController: NavHostController) {
    // LaunchedEffect
    val scale = remember {Animatable(0.8f)}
    val alpha = remember {Animatable(0f)}

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(3000) // Tunda selama 3 detik
        navController.navigate("login") {
            // Hapus SplashScreen dari backstack agar user tidak bisa kembali ke Splash
            popUpTo("splash") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF172D3D)),
        contentAlignment = Alignment.Center
    ){
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .alpha(0.1f)
                .background(Color(0xFF274857), shape = RoundedCornerShape(150.dp))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)){
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(12f)
                        .alpha(0.2f)
                        .background(Color(0xFF3385EA), shape = RoundedCornerShape(24.dp))
                        )
                Box(
                    modifier = Modifier
                        .size(75.dp)
                        .clip(RoundedCornerShape((20.dp)))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF))
                            )
                        ),
                    contentAlignment = Alignment.Center


                    //Logo Groomy
                ){
                    Image(
                        painter = painterResource(id = R.drawable.logogroomy),
                        contentDescription = "Logo Aplikasi",
                        modifier = Modifier.size(100.dp).padding(top = 10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

                //Text Branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Groomy",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color(0xFF7DD3FC),
                    modifier = Modifier.size(24.dp),

                )
            }
            //Text Slogan SPlash Screen
            Text(
                text = "Happy Pets, Happy Owners",
                color = Color(0xFF94A388),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        //bottom Loading
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .width(150.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF6366F1),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

