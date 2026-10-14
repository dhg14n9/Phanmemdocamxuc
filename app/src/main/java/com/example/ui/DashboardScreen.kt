package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.remember
import com.example.ui.components.AppCopyrightFooter
import com.example.ui.components.GlassCard
import com.example.ui.components.TopBar
import com.example.ui.theme.*

@Composable
fun DashboardScreen(navController: NavController, vm: AuraViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val bpm by vm.bpm.collectAsState()
    val spo2 by vm.spo2.collectAsState()
    val allSessions by vm.allChartSessions.collectAsState(initial = emptyList())
    val totalSessions = allSessions.size

    val affirmations = listOf(
        "Mọi chuyện rồi sẽ ổn thôi, bạn đang làm rất tốt.",
        "Hít một hơi thật sâu, bạn xứng đáng được bình yên.",
        "Bạn mạnh mẽ hơn bạn nghĩ rất nhiều.",
        "Hãy nhẹ nhàng với bản thân hôm nay nhé.",
        "Một ngày tồi tệ không có nghĩa là một cuộc đời tồi tệ."
    )
    val dailyAffirmation = androidx.compose.runtime.remember { affirmations.random() }

    GadgetbridgeListener(viewModel = vm)

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (AppPreferences.gadgetbridgeDbUri.isNotBlank()) {
            vm.syncGadgetbridgeDb(context)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            TopBar("AURA", "Dashboard", "Xin chào trở lại") { navController.navigate("settings") }
        }
        item {
            val isHeadsetConnected by vm.headsetConnected.collectAsState()
            if (isHeadsetConnected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(GlassHi, RoundedCornerShape(12.dp))
                            .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎧", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Đã kết nối",
                                style = Typography.labelSmall.copy(color = CyanNeon, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
        item {
            DailyAffirmation(dailyAffirmation)
        }
        item {
            EmergencySOS(vm)
        }
        item {
            val trendAlert by vm.trendAlert.collectAsState()
            if (trendAlert != null) {
                TrendAlertBanner(trendAlert!!, onDismiss = { vm.dismissTrendAlert() }, onAction = { navController.navigate("music") })
            } else {
                StressAlert { navController.navigate("music") }
            }
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            EmotionHero(allSessions)
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            BioMetrics(bpm, spo2)
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            SleepMetricsCard(vm)
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            PersonalizedProfileCard()
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            QuickActionsGrid(navController)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            AppCopyrightFooter()
        }
    }
}

@Composable
fun DailyAffirmation(affirmation: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(PurpleNeon.copy(0.15f), CyanNeon.copy(0.15f))))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("✨ Thông điệp hôm nay", style = Typography.labelSmall.copy(color = CyanNeon, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$affirmation\"",
                style = Typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Medium, lineHeight = 20.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun EmergencySOS(vm: AuraViewModel) {
    val bpm by vm.bpm.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var isSending by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val callPhoneLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val phone = AppPreferences.emergencyPhone
            if (phone.isNotBlank()) {
                val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                    data = android.net.Uri.parse("tel:$phone")
                }
                context.startActivity(intent)
            }
        } else {
            android.widget.Toast.makeText(context, "Cần cấp quyền gọi điện để dùng chức năng này", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33FF0000))
            .border(1.dp, Color(0x66FF0000), RoundedCornerShape(20.dp))
            .clickable(enabled = !isSending) {
                isSending = true
                vm.viewModelScope.launch {
                    val success = GoogleSheetsService.pushAlertToSheet(
                        webhookUrl = AppPreferences.webhookUrl,
                        type = "SOS_EMERGENCY",
                        message = "NGƯỜI DÙNG NHẤN NÚT SOS!",
                        value = "BPM hiện tại: $bpm",
                        details = "Cần hỗ trợ khẩn cấp."
                    )
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isSending = false
                        android.widget.Toast.makeText(
                            context,
                            if (success) "Đã gửi cảnh báo khẩn cấp tới người thân!" else "Gửi thất bại, kiểm tra mạng/webhook.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        
                        // Tự động gọi số khẩn cấp
                        val phone = AppPreferences.emergencyPhone
                        if (phone.isNotBlank()) {
                            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
                            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
                                    data = android.net.Uri.parse("tel:$phone")
                                }
                                context.startActivity(intent)
                            } else {
                                callPhoneLauncher.launch(android.Manifest.permission.CALL_PHONE)
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Chưa cấu hình số điện thoại SOS trong Cài đặt", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("🚨", fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            if (isSending) "ĐANG GỬI SOS..." else "GỬI TÍN HIỆU KHẨN CẤP (SOS)",
            style = Typography.labelMedium.copy(color = Color(0xFFFF5555), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        )
    }
}

@Composable
fun StressAlert(onNavigateToMusic: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Glass)
            .border(1.dp, AmberNeon.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.padding(top = 6.dp).size(10.dp).background(Amber, RoundedCornerShape(5.dp)))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text("⚡ Mức stress tăng nhẹ", style = Typography.labelMedium.copy(color = AmberNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AI phát hiện nhịp tim 92 BPM + giọng nói có dấu hiệu căng thẳng trong 40 phút qua. Gợi ý: nghe nhạc healing thư giãn.", 
                style = Typography.bodyMedium.copy(color = Text2, lineHeight = 18.sp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clickable { onNavigateToMusic() }
                    .background(CyanNeon.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎵", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mở nhạc Healing ngay", style = Typography.labelSmall.copy(color = CyanNeon))
            }
        }
    }
}

@Composable
fun TrendAlertBanner(alert: TrendAlert, onDismiss: () -> Unit, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Pink.copy(alpha = 0.15f))
            .border(2.dp, PinkNeon, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️ CẢNH BÁO NGUY CƠ STRESS", style = Typography.labelMedium.copy(color = PinkNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(PinkNeon, RoundedCornerShape(4.dp)))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.message,
                style = Typography.bodyMedium.copy(color = Text1, lineHeight = 18.sp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            
            // Render quick visual trend representation values:
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                alert.dates.forEachIndexed { idx, date ->
                    val valPercent = alert.values.getOrNull(idx)?.toInt() ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(date.substringBefore("/"), style = Typography.labelSmall.copy(color = Text3, fontSize = 10.sp))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("${valPercent}%", style = Typography.labelSmall.copy(color = if (idx == 2) PinkNeon else Text2, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                    if (idx < 2) {
                        Text("➔", style = Typography.labelSmall.copy(color = Text3, fontSize = 11.sp))
                    }
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Xoa dịu ngay 🌊",
                    color = Color.Black,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    modifier = Modifier
                        .background(PinkNeon, RoundedCornerShape(10.dp))
                        .clickable { onAction() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
                Text(
                    text = "Bỏ qua",
                    color = Text1,
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    modifier = Modifier
                        .background(GlassHi, RoundedCornerShape(10.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EmotionHero(sessions: List<com.example.data.ChartSession>) {
    var filter by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Tất cả") }
    
    val filteredSessions = androidx.compose.runtime.remember(sessions, filter) {
        when (filter) {
            "Hôm nay" -> {
                val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                sessions.filter { it.dateStr == today }
            }
            "7 Ngày" -> {
                val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
                sessions.filter { it.timestamp >= sevenDaysAgo }
            }
            else -> sessions
        }
    }
    
    val totalSessions = filteredSessions.size
    val avgStress = if (filteredSessions.isNotEmpty()) filteredSessions.map { it.stressLevel }.average().toInt() else 22
    val peaceLevel = 100 - avgStress
    val sweepAngle = (peaceLevel / 100f) * 360f

    val title = if (avgStress < 30) "Bình Thản & Ổn Định" else if (avgStress < 60) "Bình Thường" else "Căng Thẳng"
    val emoji = if (avgStress < 30) "😌" else if (avgStress < 60) "😐" else "😰"
    val chip1 = if (avgStress < 30) "😊 Ổn định" else if (avgStress < 60) "🤔 Đang theo dõi" else "⚠️ Cần thư giãn"
    val chip2 = if (avgStress < 30) "🧠 Khỏe mạnh" else if (avgStress < 60) "☁️ Tạm ổn" else "💨 Hít thở sâu"

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                listOf("Hôm nay", "7 Ngày", "Tất cả").forEach { option ->
                    val isSelected = filter == option
                    Text(
                        text = option,
                        style = Typography.labelSmall.copy(
                            color = if (isSelected) CyanNeon else Text3,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier
                            .clickable { filter = option }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Glass, style = Stroke(6.dp.toPx()))
                        drawArc(
                            brush = Brush.linearGradient(listOf(PurpleNeon, CyanNeon)),
                            startAngle = -90f, sweepAngle = sweepAngle, useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 28.sp)
                        Text("${peaceLevel}%", style = Typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title, 
                        style = Typography.titleLarge.copy(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tổng phiên theo dõi cảm xúc: $totalSessions", 
                        style = Typography.bodyMedium.copy(color = Text2)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(chip1, BlueNeon, Blue)
                        Chip(chip2, PurpleNeon, Purple)
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, colorNeon: Color, colorBase: Color) {
    Text(
        text = text,
        style = Typography.labelSmall.copy(color = colorNeon, fontWeight = FontWeight.Bold, fontSize = 11.sp),
        modifier = Modifier
            .background(colorBase.copy(alpha = 0.12f), RoundedCornerShape(percent = 50))
            .border(1.dp, colorBase.copy(alpha = 0.25f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun BioMetrics(bpm: Int, spo2: Int) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = "CHỈ SỐ SINH HIỆU THỜI GIAN THỰC",
            style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(
                modifier = Modifier.weight(1f),
                glowColor = PinkNeon.copy(alpha = 0.15f),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("NHỊP TIM (BPM)", style = Typography.labelSmall.copy(color = Text2, letterSpacing = 0.5.sp, fontWeight = FontWeight.SemiBold))
                        Text("❤️", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = bpm.toString(),
                        style = Typography.displayMedium.copy(color = PinkNeon, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Trạng thái: Bình thường", style = Typography.bodySmall.copy(color = Text3, fontSize = 11.sp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0x0DFFFFFF), RoundedCornerShape(3.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(bpm / 120f).height(6.dp).background(Brush.horizontalGradient(listOf(PinkNeon, Color(0xFFF472B6))), RoundedCornerShape(3.dp)))
                    }
                }
            }
            GlassCard(
                modifier = Modifier.weight(1f),
                glowColor = CyanNeon.copy(alpha = 0.15f),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("NỒNG ĐỘ OXY (SPO₂)", style = Typography.labelSmall.copy(color = Text2, letterSpacing = 0.5.sp, fontWeight = FontWeight.SemiBold))
                        Text("💧", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$spo2%",
                        style = Typography.displayMedium.copy(color = CyanNeon, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Trạng thái: Tuyệt vời", style = Typography.bodySmall.copy(color = Text3, fontSize = 11.sp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0x0DFFFFFF), RoundedCornerShape(3.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(spo2 / 100f).height(6.dp).background(Brush.horizontalGradient(listOf(CyanNeon, Color(0xFF22D3EE))), RoundedCornerShape(3.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = "TIỆN ÍCH CHĂM SÓC SỨC KHOẺ",
            style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                emoji = "🎙️", 
                title = "Phân tích giọng", 
                subtitle = "Chẩn đoán qua âm sắc", 
                color = PurpleNeon, 
                modifier = Modifier.weight(1f)
            ) { navController.navigate("voice") }
            QuickActionCard(
                emoji = "🤖", 
                title = "AURA AI Chat", 
                subtitle = "Trợ lý ảo lắng nghe", 
                color = BlueNeon, 
                modifier = Modifier.weight(1f)
            ) { navController.navigate("chat") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                emoji = "🎵", 
                title = "Nhạc Healing", 
                subtitle = "Âm tần sóng não", 
                color = PinkNeon, 
                modifier = Modifier.weight(1f)
            ) { navController.navigate("music") }
            QuickActionCard(
                emoji = "🗓️", 
                title = "Nhật ký cảm xúc", 
                subtitle = "Biểu đồ & Nhật ký", 
                color = CyanNeon, 
                modifier = Modifier.weight(1f)
            ) { navController.navigate("charts") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                emoji = "🌬️", 
                title = "Hít thở sâu", 
                subtitle = "Giảm căng thẳng", 
                color = AmberNeon, 
                modifier = Modifier.weight(1f)
            ) { navController.navigate("breathing") }
            QuickActionCard(
                emoji = "⌚", 
                title = "Đồng bộ Gadgetbridge", 
                subtitle = "Nhật ký BPM, SpO2, Ngủ", 
                color = GreenNeon, 
                modifier = Modifier.weight(1f)
            ) { navController.navigate("history") }
        }
    }
}

@Composable
fun QuickActionCard(emoji: String, title: String, subtitle: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, style = Typography.bodyLarge.copy(color = Text1, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = Typography.bodySmall.copy(color = Text2))
        }
    }
}

@Composable
fun SleepMetricsCard(vm: AuraViewModel) {
    val sleepMinutes by vm.sleepMinutes.collectAsState()
    val deepSleepMinutes by vm.deepSleepMinutes.collectAsState()
    val sleepScore by vm.sleepScore.collectAsState()

    if (sleepMinutes != null) {
        val totalHours = sleepMinutes!! / 60
        val totalMins = sleepMinutes!! % 60
        val deepHours = (deepSleepMinutes ?: 0) / 60
        val deepMins = (deepSleepMinutes ?: 0) % 60
        val score = sleepScore ?: 75

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "CHỈ SỐ GIẤC NGỦ HÔM NAY",
                style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            GlassCard(
                glowColor = PurpleNeon.copy(alpha = 0.15f),
                cornerRadius = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("THỜI GIAN NGỦ", style = Typography.labelSmall.copy(color = Text2, letterSpacing = 0.5.sp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${totalHours}g ${totalMins}p",
                                style = Typography.displayMedium.copy(color = PurpleNeon, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(PurpleNeon.copy(alpha = 0.12f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("😴", fontSize = 24.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ngủ sâu", style = Typography.bodySmall.copy(color = Text3))
                            Text("${deepHours}g ${deepMins}p", style = Typography.bodyMedium.copy(color = Text1, fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Điểm giấc ngủ", style = Typography.bodySmall.copy(color = Text3))
                            Text("$score / 100", style = Typography.bodyMedium.copy(color = PurpleNeon, fontWeight = FontWeight.Bold))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Sleep quality bar
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0x0DFFFFFF), RoundedCornerShape(3.dp))) {
                        Box(modifier = Modifier.fillMaxWidth(score / 100f).height(6.dp).background(Brush.horizontalGradient(listOf(PurpleNeon, BlueNeon)), RoundedCornerShape(3.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalizedProfileCard() {
    val gender = AppPreferences.userGender
    val weight = AppPreferences.userWeight
    val height = AppPreferences.userHeight

    // BMI calculation
    val heightInMeters = height / 100f
    val bmi = if (heightInMeters > 0) weight / (heightInMeters * heightInMeters) else 0f
    val bmiCategory = when {
        bmi < 18.5f -> "Thiếu cân"
        bmi < 24.9f -> "Bình thường"
        bmi < 29.9f -> "Thừa cân"
        else -> "Béo phì"
    }
    val bmiColor = when {
        bmi < 18.5f -> CyanNeon
        bmi < 24.9f -> GreenNeon
        bmi < 29.9f -> AmberNeon
        else -> PinkNeon
    }

    // Daily recommended water intake
    val recommendedWaterMl = (weight * 35).toInt()
    val recommendedWaterL = String.format(Locale.getDefault(), "%.2f", recommendedWaterMl / 1000f)

    // Daily BMR (Basal Metabolic Rate) Harris-Benedict
    val bmr = if (gender == "Nam") {
        88.362f + (13.397f * weight) + (4.799f * height) - (5.677f * 25f) // Assume default age 25
    } else {
        447.593f + (9.247f * weight) + (3.098f * height) - (4.330f * 25f)
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = "HỒ SƠ THỂ CHẤT CÁ NHÂN",
            style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        GlassCard(
            glowColor = AmberNeon.copy(alpha = 0.15f),
            cornerRadius = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("CHỈ SỐ THỂ TRẠNG (BMI)", style = Typography.labelSmall.copy(color = Text2, letterSpacing = 0.5.sp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", bmi),
                            style = Typography.displayMedium.copy(color = bmiColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Phân loại: $bmiCategory",
                            style = Typography.bodySmall.copy(color = bmiColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(AmberNeon.copy(alpha = 0.12f), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚖️", fontSize = 24.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Thông tin", style = Typography.bodySmall.copy(color = Text3))
                        Text("$gender • ${height.toInt()}cm • ${weight.toInt()}kg", style = Typography.bodyMedium.copy(color = Text1, fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Nước khuyên dùng", style = Typography.bodySmall.copy(color = Text3))
                        Text("$recommendedWaterL Lít / ngày", style = Typography.bodyMedium.copy(color = CyanNeon, fontWeight = FontWeight.Bold))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Năng lượng nghỉ (BMR):", style = Typography.bodySmall.copy(color = Text3))
                    Text("${bmr.toInt()} kcal", style = Typography.bodyMedium.copy(color = AmberNeon, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
