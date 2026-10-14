package com.example.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.TopBar
import com.example.ui.components.GlassCard
import com.example.ui.components.glow
import com.example.ui.theme.*

@Composable
fun VoiceScreen(navController: NavController, vm: AuraViewModel = viewModel()) {
    val results by vm.voiceSentiment.collectAsState()
    val currentMode by vm.voiceMode.collectAsState()
    val voiceState by vm.voiceState.collectAsState()
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        vm.currentVoiceTarget = "voice"
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg0),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            TopBar("Voice", "Measurement", "Đo cảm xúc qua giọng nói")
        }
        item {
            ModeSelector(vm, currentMode)
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            VoiceHero(vm, currentMode, voiceState)
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            ResultBars(results, currentMode)
        }
    }
}

@Composable
fun ModeSelector(vm: AuraViewModel, currentMode: MeasurementMode) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = "CHỌN CHUYÊN KHOA ĐO CẢM XÚC",
            style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            items(MeasurementMode.values()) { mode ->
                val isSelected = mode == currentMode
                val bgBrush = if (isSelected) {
                    Brush.linearGradient(listOf(Pink.copy(0.15f), Purple.copy(0.15f)))
                } else {
                    androidx.compose.ui.graphics.SolidColor(Glass)
                }
                Box(
                    modifier = Modifier
                        .clickable { vm.setVoiceMode(mode) }
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgBrush)
                        .border(1.dp, if (isSelected) PinkNeon else GlassBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(mode.emoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mode.title,
                            style = Typography.labelMedium.copy(
                                color = if (isSelected) CyanNeon else Text2,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "💡 Định hướng: ${currentMode.desc}",
            style = Typography.bodyMedium.copy(color = Text3, lineHeight = 18.sp),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun VoiceHero(vm: AuraViewModel, currentMode: MeasurementMode, voiceState: VoiceState) {
    val context = LocalContext.current
    
    val recordAudioLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            vm.toggleVoiceListening(context)
        } else {
            android.widget.Toast.makeText(context, "Cần cấp quyền ghi âm để sử dụng tính năng này", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val isListening = voiceState == VoiceState.LISTENING
    val isAnalyzing = voiceState == VoiceState.ANALYZING
    val isResponding = voiceState == VoiceState.RESPONDING

    // Concentric pulsing waves animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha1"
    )

    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveScale2"
    )
    val waveAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha2"
    )

    val centerColor = when {
        isListening -> Color(0xFFEF4444) // Red for recording
        isAnalyzing -> AmberNeon // Amber for AI thinking
        isResponding -> CyanNeon // Cyan for speaking response
        else -> PurpleNeon
    }

    val waveColor = centerColor.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .clickable {
                val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    vm.toggleVoiceListening(context)
                } else {
                    recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }
            }
            .padding(vertical = 40.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Animated center point
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isListening || isResponding) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer {
                                scaleX = waveScale1
                                scaleY = waveScale1
                                alpha = waveAlpha1
                            }
                            .background(waveColor, RoundedCornerShape(50.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .graphicsLayer {
                                scaleX = waveScale2
                                scaleY = waveScale2
                                alpha = waveAlpha2
                            }
                            .background(waveColor, RoundedCornerShape(50.dp))
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Brush.linearGradient(listOf(centerColor, centerColor.copy(alpha = 0.7f))),
                            RoundedCornerShape(45.dp)
                        )
                        .glow(color = centerColor, radius = 24.dp, alpha = 0.25f),
                    contentAlignment = Alignment.Center
                ) {
                    val iconText = when {
                        isListening -> "🛑"
                        isAnalyzing -> "⏳"
                        isResponding -> "🗣️"
                        else -> "🎙️"
                    }
                    Text(iconText, fontSize = 36.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val (statusTitle, statusDesc) = when {
                isListening -> Pair("ĐANG LẮNG NGHE GIỌNG NÓI...", "Hãy tự nhiên chia sẻ tâm tư, lo lắng hay cảm xúc của bạn")
                isAnalyzing -> Pair("ĐANG PHÂN TÍCH CẢM XÚC...", "AURA đang cảm nhận tần số giọng nói và ngữ điệu lời kể")
                isResponding -> Pair("AURA ĐANG PHẢN HỒI...", "Lắng nghe chia sẻ xoa dịu tâm trí từ trợ lý AI")
                else -> Pair("NHẤN ĐỂ BẮT ĐẦU NÓI", "Chạm vào micro để AURA đo chuyên sâu cảm xúc của bạn")
            }
            
            Text(
                text = statusTitle, 
                style = Typography.titleMedium.copy(
                    color = centerColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusDesc,
                style = Typography.bodyMedium.copy(color = Text2, lineHeight = 22.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chuyên đề đo: ${currentMode.title}",
                style = Typography.bodySmall.copy(color = Text3, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun ResultBars(results: Triple<Int, Int, Int>, currentMode: MeasurementMode) {
    val labels = when(currentMode) {
        MeasurementMode.ANXIETY -> Triple(
            "😌 Thư giãn ổn định",
            "😟 Bồn chồn bứt rứt",
            "😰 Lo lắng hoảng sợ"
        )
        MeasurementMode.STRESS -> Triple(
            "😌 Thoải mái nhẹ nhõm",
            "😟 Áp lực căng thẳng",
            "😫 Kiệt sức mệt mỏi"
        )
        MeasurementMode.ADHD -> Triple(
            "🎯 Tập trung điềm tĩnh",
            "🧩 Xao nhãng mất chú ý",
            "⚡ Bốc đồng kích động"
        )
        MeasurementMode.INSOMNIA -> Triple(
            "😴 Ngủ ngon khoẻ khoắn",
            "🛌 Trằn trọc khó vào giấc",
            "⏰ Thức giấc giữa đêm"
        )
        else -> Triple(
            "😌 Bình thản thư thái",
            "😟 Căng thẳng áp lực",
            "😊 Vui vẻ tích cực"
        )
    }

    val glowColors = when(currentMode) {
        MeasurementMode.ANXIETY -> Triple(GreenNeon, AmberNeon, PinkNeon)
        MeasurementMode.STRESS -> Triple(GreenNeon, AmberNeon, PinkNeon)
        MeasurementMode.ADHD -> Triple(CyanNeon, PurpleNeon, AmberNeon)
        MeasurementMode.INSOMNIA -> Triple(CyanNeon, AmberNeon, PinkNeon)
        else -> Triple(GreenNeon, AmberNeon, BlueNeon)
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("KẾT QUẢ ĐO CHUYÊN BIỆT", style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp))
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(20.dp)).padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                EBar("🟢", labels.first, "${results.first}%", glowColors.first, glowColors.first, glowColors.first, results.first / 100f)
                EBar("🟡", labels.second, "${results.second}%", glowColors.second, glowColors.second, glowColors.second, results.second / 100f)
                EBar("🔴", labels.third, "${results.third}%", glowColors.third, glowColors.third, glowColors.third, results.third / 100f)
            }
        }
    }
}

@Composable
fun EBar(emoji: String, label: String, v: String, tColor: Color, g1: Color, g2: Color, fraction: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).background(g1.copy(0.12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = Typography.bodyLarge.copy(color = Text2, fontWeight = FontWeight.Medium))
                Text(v, style = Typography.bodyLarge.copy(color = tColor, fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0x0DFFFFFF), RoundedCornerShape(4.dp))) {
                Box(modifier = Modifier.fillMaxWidth(if (fraction.isNaN()) 0f else fraction).height(8.dp).background(Brush.horizontalGradient(listOf(g1, g2)), RoundedCornerShape(4.dp)))
            }
        }
    }
}
