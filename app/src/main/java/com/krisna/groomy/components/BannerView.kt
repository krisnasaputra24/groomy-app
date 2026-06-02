// File: C:/Users/Krisna Saputra/AndroidStudioProjects/groomy2/app/src/main/java/com/krisna/groomy/components/BannerView.kt

package com.krisna.groomy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.krisna.groomy.model.PromoResponse
import kotlinx.coroutines.delay
import androidx.compose.foundation.interaction.collectIsDraggedAsState

@Composable
fun BannerView(
    modifier: Modifier = Modifier,
    promos: List<PromoResponse> = emptyList()
) {
    val pageCount = promos.size
    
    // Jika tidak ada promo, jangan tampilkan apa-apa
    if (pageCount == 0) return

    val pagerState = rememberPagerState(pageCount = { pageCount })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged, pageCount) {
        if (!isDragged && pageCount > 0) {
            while (true) {
                delay(5000)
                val nextPage = (pagerState.currentPage + 1) % pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val promo = promos[page]
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val servicePhoto = promo.service?.photo
                    val url = if (servicePhoto.isNullOrBlank()) null
                    else if (servicePhoto.startsWith("http")) servicePhoto
                    else "https://groomy-sigma.vercel.app/${servicePhoto}"

                    if (url != null) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.linearGradient(colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF)))
                        ))
                    }

                    // Gradient Overlay untuk keterbacaan teks
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 100f
                                )
                            )
                    )

                    // Promo Badge (Top Right)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        color = Color(0xFFFACC15), // Yellow Gold
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (promo.discount > 0) "${promo.discount}% OFF" else "HOT DEAL",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }

                    // Text Content (Bottom Left)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = promo.service?.name ?: "Special Offer",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = promo.code,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Berakhir ${promo.expiryDate.take(10)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Professional Dots Indicator
        if (pageCount > 1) {
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pageCount) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(if (active) 18.dp else 5.dp)
                            .clip(CircleShape)
                            .background(if (active) Color(0xFF257DEF) else Color(0xFFE2E8F0))
                    )
                }
            }
        }
    }
}
