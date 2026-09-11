package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NeonRed
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onFinished: () -> Unit
) {
    val progressAnim = remember { Animatable(0f) }
    var currentPercent by remember { mutableIntStateOf(0) }
    var loadingStatus by remember { mutableStateOf("INITIALIZING ENGINE...") }
    var isComplete by remember { mutableStateOf(false) }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Simulating game loading progress up to 100%
    LaunchedEffect(Unit) {
        val stages = listOf(
            Triple(15f, "INITIALIZING ENGINE & SHADERS...", 400L),
            Triple(32f, "LOADING SAN ANDREAS CACHE...", 500L),
            Triple(48f, "VERIFYING GAME ASSETS & SOUNDS...", 450L),
            Triple(68f, "CONNECTING TO SA-MP NETWORKS...", 550L),
            Triple(85f, "SYNCING MAP & PLAYER PROTOCOLS...", 400L),
            Triple(96f, "FINALIZING GRAPHICS & CONTROLS...", 350L),
            Triple(100f, "LOAD COMPLETE! WELCOME TO SA-MP", 300L)
        )

        var lastTarget = 0f
        for ((target, status, stepDuration) in stages) {
            loadingStatus = status
            val steps = 10
            val increment = (target - lastTarget) / steps
            for (i in 1..steps) {
                val nextVal = (lastTarget + increment * i).coerceAtMost(100f)
                progressAnim.snapTo(nextVal)
                currentPercent = nextVal.toInt()
                delay(stepDuration / steps)
            }
            lastTarget = target
        }
        currentPercent = 100
        isComplete = true
        delay(800L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_loading_screen")
    ) {
        // 1. User Uploaded Background Image (Oni Mask character)
        Image(
            painter = painterResource(id = R.drawable.img_bg_character),
            contentDescription = "SAMP Game Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Cinematic Gradient Scrim & Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color(0x33000000),
                            Color(0x88000000),
                            Color(0xF5050508)
                        )
                    )
                )
        )

        // 3. Top Status Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Server Ping Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x99101018))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PORTRAIT MODE • 60 FPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
            }

            // Skip Button
            Text(
                text = "SKIP >>",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x6622222E))
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .clickable { onFinished() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("skip_loading_button")
            )
        }

        // 4. Center Game Branding
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing SA-MP Badge
            Box(
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(12.dp), spotColor = CrimsonRed)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(DarkCrimson, CrimsonRed, FlameOrange)
                        )
                    )
                    .border(1.5.dp, Color(0xFFFF8080), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Game Icon",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SA-MP MOBILE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SAN ANDREAS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "MULTIPLAYER LAUNCHER",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CrimsonRed,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )
        }

        // 5. Bottom Game Loading Bar & Status
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Percent and Stage info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "SYSTEM STATUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = loadingStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                // Digital Percentage Counter
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$currentPercent",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (currentPercent == 100) CyberGold else NeonRed
                    )
                    Text(
                        text = "%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonRed,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The 0 to 100 Loading Bar Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xCC12121A))
                    .border(1.5.dp, if (currentPercent == 100) CyberGold else DarkCrimson, RoundedCornerShape(10.dp))
                    .padding(3.dp)
                    .testTag("loading_progress_bar")
            ) {
                // Filled progress indicator
                val progressFraction = (progressAnim.value / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DarkCrimson,
                                    CrimsonRed,
                                    NeonRed,
                                    FlameOrange
                                )
                            )
                        )
                ) {
                    // Animated white scanline shine
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = glowAlpha * 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-status note
            if (isComplete) {
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("enter_game_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Enter")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START SA-MP GAME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Online",
                        tint = CrimsonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYNCING WITH MASTER SERVER • PLEASE WAIT...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
