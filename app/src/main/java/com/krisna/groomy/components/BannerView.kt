package com.krisna.groomy.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.krisna.groomy.R
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.krisna.groomy.model.PromoResponse

@Composable
fun BannerView(
    modifier: Modifier = Modifier,
    promos: List<PromoResponse> = emptyList()
) {
    // Kombinasi banner statis dan promo dari groomer
    val staticBanners = listOf(
        R.drawable.banner1,
        R.drawable.banner2
    )

    val pageCount = staticBanners.size + promos.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragged, pageCount) {
        if (!isDragged && pageCount > 0) {
            while (true) {
                delay(4000) 
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
                .height(180.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 0.dp
        ) { page ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (page < staticBanners.size) {
                    // Tampilan Banner Statis
                    Image(
                        painter = painterResource(id = staticBanners[page]),
                        contentDescription = "Banner $page",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Tampilan Banner Promo dari Groomer sesuai spek terbaru
                    val promo = promos[page - staticBanners.size]
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Menggunakan foto service jika ada, karena promo response mengembalikan service object
                        val servicePhoto = promo.service?.photo
                        val url = if (servicePhoto.isNullOrBlank()) null 
                                 else if (servicePhoto.startsWith("http")) servicePhoto 
                                 else "https://groomy-sigma.vercel.app/${servicePhoto}"

                        if (url != null) {
                            AsyncImage(
                                model = url,
                                contentDescription = promo.code,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(Color(0xFF7DD3FC), Color(0xFF257DEF))
                                        )
                                    )
                            )
                        }
                        
                        // Overlay Teks Promo (Update ke 'code' dan 'discount')
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "KODE: ${promo.code}",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Diskon ${promo.discount}% - ${promo.service?.name ?: "Semua Layanan"}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Hingga: ${promo.expiryDate.take(10)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Luxury Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color(0xFF257DEF) else Color(0xFFE2E8F0)
                val width = if (pagerState.currentPage == iteration) 16.dp else 6.dp
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}
