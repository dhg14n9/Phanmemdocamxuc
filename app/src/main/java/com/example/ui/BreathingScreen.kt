package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BreathingScreen(navController: NavController) {
    var isActive by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(BreathingPhase.IDLE) }
    var countdown by remember { mutableStateOf(0) }

    // Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val circleScale by animateFloatAsState(
        targetValue = when (phase) {
            BreathingPhase.INHALE -> 1.5f
            BreathingPhase.HOLD -> 1.5f
            BreathingPhase.EXHALE -> 1f
            BreathingPhase.IDLE -> 1f
        },
        animationSpec = tween(
            durationMillis = when (phase) {
                BreathingPhase.INHALE -> 4000
                BreathingPhase.HOLD -> 1000
                BreathingPhase.EXHALE -> 8000
                BreathingPhase.IDLE -> 1000
            },
            easing = LinearEasing
        ), label = "circleScale"
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                // Inhale (4s)
                phase = BreathingPhase.INHALE
                countdown = 4
                while (countdown > 0 && isActive) {
                    delay(1000)
                    countdown--
                }
                if (!isActive) break

                // Hold (7s)
                phase = BreathingPhase.HOLD
                countdown = 7
                while (countdown > 0 && isActive) {
                    delay(1000)
                    countdown--
                }
                if (!isActive) break

                // Exhale (8s)
                phase = BreathingPhase.EXHALE
                countdown = 8
                while (countdown > 0 && isActive) {
                    delay(1000)
                    countdown--
                }
            }
        } else {
            phase = BreathingPhase.IDLE
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar("AURA", "Hít thở", "Giảm căng thẳng tức thì") { navController.popBackStack() }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = when (phase) {
                BreathingPhase.IDLE -> "Nhấn BẮT ĐẦU để tập hít thở 4-7-8"
                BreathingPhase.INHALE -> "Hít vào từ từ bằng mũi..."
                BreathingPhase.HOLD -> "Giữ hơi thở..."
                BreathingPhase.EXHALE -> "Thở ra chậm rãi bằng miệng..."
            },
            style = Typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 30.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(300.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                val baseRadius = 80.dp.toPx()
                
                // Outer glowing circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanNeon.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    radius = baseRadius * circleScale * 1.2f,
                    center = center
                )

                // Main circle
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.8f),
                    radius = baseRadius * circleScale,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )
                
                // Inner solid circle
                drawCircle(
                    brush = Brush.linearGradient(listOf(BlueNeon, CyanNeon)),
                    radius = baseRadius * circleScale * 0.9f,
                    center = center
                )
            }
            
            if (isActive) {
                Text(
                    text = countdown.toString(),
                    style = Typography.displayLarge.copy(color = Color.White, fontSize = 60.sp, fontWeight = FontWeight.Bold)
                )
            } else {
                Text(
                    text = "BẮT ĐẦU",
                    style = Typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    modifier = Modifier.clickable { isActive = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        if (isActive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0x33FFFFFF))
                    .clickable { isActive = false }
                    .padding(horizontal = 30.dp, vertical = 12.dp)
            ) {
                Text("DỪNG LẠI", style = Typography.labelLarge.copy(color = Color.White))
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Phương pháp 4-7-8 giúp làm dịu hệ thần kinh tự chủ, giảm nhịp tim và mang lại sự thư giãn sâu.",
            style = Typography.bodySmall.copy(color = Text3, textAlign = TextAlign.Center),
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

enum class BreathingPhase {
    IDLE, INHALE, HOLD, EXHALE
}
