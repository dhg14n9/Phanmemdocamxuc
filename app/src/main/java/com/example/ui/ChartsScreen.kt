package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.ChartSession
import com.example.data.GadgetbridgeHealthRecord
import com.example.ui.components.TopBar
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChartsScreen(navController: NavController, vm: AuraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val allSessions by vm.allChartSessions.collectAsState(initial = emptyList())
    val dailyReports by vm.allDailyReports.collectAsState(initial = emptyList())
    val loadingMap by vm.dailyReportLoadingMap.collectAsState()
    
    val dateGroups = remember(allSessions) { allSessions.groupBy { it.dateStr } }
    val availableDates = remember(dateGroups) {
        dateGroups.entries
            .sortedByDescending { it.value.maxOfOrNull { s -> s.timestamp } ?: 0L }
            .map { it.key }
    }
    
    var selectedDate by remember { mutableStateOf("") }
    
    LaunchedEffect(availableDates) {
        if (selectedDate.isEmpty() && availableDates.isNotEmpty()) {
            selectedDate = availableDates.first()
        }
    }
    
    // Calculate global cumulative stats as requested
    val totalSessions = allSessions.size
    val voiceSessions = allSessions.count { it.source == "Giọng nói" }
    val aiSessions = allSessions.count { it.source == "Aura AI" }

    var highlightedTips by remember { mutableStateOf<String?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg0),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            TopBar("AURA", "Mood Diary", "Nhật ký tâm trạng")
        }
        
        // 1. Overall Stats Panel - Total sessions (including voice-specific and overall Aura logs)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "TỔNG PHIÊN ĐO TỪ KHI BẮT ĐẦU",
                    style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox(
                        title = "Tổng cộng",
                        value = "$totalSessions",
                        emoji = "📊",
                        bgGradient = Brush.linearGradient(listOf(Bg2, Bg3)),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Giọng nói",
                        value = "$voiceSessions",
                        emoji = "🎙",
                        bgGradient = Brush.linearGradient(listOf(Bg2, Bg3)),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "AURA AI",
                        value = "$aiSessions",
                        emoji = "🤖",
                        bgGradient = Brush.linearGradient(listOf(Bg2, Bg3)),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // 2. Interactive Calendar/Date Selector Row
        if (availableDates.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "CHỌN NGÀY XEM CẢM XÚC",
                    style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(availableDates) { date ->
                        val isSelected = date == selectedDate
                        val bgBrush = if (isSelected) {
                            Brush.linearGradient(listOf(Pink.copy(0.25f), Purple.copy(0.25f)))
                        } else {
                            androidx.compose.ui.graphics.SolidColor(Glass)
                        }
                        
                        Column(
                            modifier = Modifier
                                .clickable { selectedDate = date }
                                .background(bgBrush, RoundedCornerShape(18.dp))
                                .border(1.dp, if (isSelected) PinkNeon else GlassBorder, RoundedCornerShape(18.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📅",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = date,
                                style = Typography.labelMedium.copy(
                                    color = if (isSelected) CyanNeon else Text2,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Dynamic Recommendation Tips Overlay
        highlightedTips?.let { tip ->
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyanNeon.copy(alpha = 0.1f))
                        .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GỢI Ý ĐIỀU HOÀ CẢM XÚC",
                            style = Typography.labelMedium.copy(color = CyanNeon, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tip,
                            style = Typography.bodySmall.copy(color = Text1)
                        )
                    }
                    Text(
                        text = "✕",
                        color = Text3,
                        modifier = Modifier
                            .clickable { highlightedTips = null }
                            .padding(4.dp)
                    )
                }
            }
        }

        // 3. Header showing filtered day stats and average
        item {
            val sessionsForDate = dateGroups[selectedDate] ?: emptyList()
            val dayAvgStress = if (sessionsForDate.isNotEmpty()) sessionsForDate.map { it.stressLevel }.average().toInt() else 0
            val dayAvgBpm = if (sessionsForDate.isNotEmpty()) sessionsForDate.map { it.bpm }.average().toInt() else 0
            
            Spacer(modifier = Modifier.height(18.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (selectedDate.isNotEmpty()) "NHẬT KÝ NGÀY $selectedDate" else "DANH SÁCH NHẬT KÝ",
                        style = Typography.titleMedium.copy(color = Text1, fontWeight = FontWeight.Bold)
                    )
                    if (selectedDate.isNotEmpty()) {
                        Text(
                            text = "${sessionsForDate.size} phiên đo",
                            style = Typography.bodySmall.copy(color = Text3, fontWeight = FontWeight.Medium)
                        )
                    }
                }
                
                if (selectedDate.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Glass, RoundedCornerShape(14.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Stress TB", style = Typography.labelSmall.copy(color = Text3))
                            Text("$dayAvgStress%", style = Typography.titleMedium.copy(color = if (dayAvgStress > 50) PinkNeon else CyanNeon, fontWeight = FontWeight.Bold))
                        }
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(GlassBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BPM TB", style = Typography.labelSmall.copy(color = Text3))
                            Text("$dayAvgBpm", style = Typography.titleMedium.copy(color = Text1, fontWeight = FontWeight.Bold))
                        }
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(GlassBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Trạng Thái", style = Typography.labelSmall.copy(color = Text3))
                            val statusStr = when {
                                dayAvgStress < 25 -> "An yên 🌸"
                                dayAvgStress < 50 -> "Cân bằng ✨"
                                dayAvgStress < 75 -> "Căng nhẹ ⚡"
                                else -> "Báo động ⚠️"
                            }
                            Text(statusStr, style = Typography.titleMedium.copy(color = Text2, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
        
        // 3.5. Daily AI Report Summary Card
        if (selectedDate.isNotEmpty()) {
            val sessionsForDate = dateGroups[selectedDate] ?: emptyList()
            if (sessionsForDate.isNotEmpty()) {
                val reportForDate = dailyReports.find { it.dateStr == selectedDate }
                val isLoading = loadingMap[selectedDate] == true

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "BÁO CÁO TỔNG HỢP AI THEO NGÀY",
                            style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Glass, RoundedCornerShape(18.dp))
                                    .border(1.dp, PinkNeon.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        color = PinkNeon,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Aura AI đang phân tích dữ liệu & tổng hợp báo cáo ngày...",
                                        color = Text2,
                                        style = Typography.bodyMedium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else if (reportForDate != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(listOf(Bg2, Bg3)), RoundedCornerShape(18.dp))
                                    .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("✨", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Aura AI Nhận Xét",
                                            style = Typography.titleMedium.copy(color = CyanNeon, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Glass, RoundedCornerShape(12.dp))
                                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable { vm.generateAndSaveDailyReport(selectedDate, sessionsForDate) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Tạo lại 🔄",
                                            style = Typography.labelSmall.copy(color = Text2, fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = reportForDate.aiAssessment,
                                    color = Text1,
                                    style = Typography.bodyMedium.copy(lineHeight = 22.sp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column {
                                            Text("Stress TB", style = Typography.labelSmall.copy(color = Text3))
                                            Text("${reportForDate.avgStress}%", style = Typography.titleSmall.copy(color = if (reportForDate.avgStress > 50) PinkNeon else CyanNeon, fontWeight = FontWeight.Bold))
                                        }
                                        Column {
                                            Text("Nhịp tim TB", style = Typography.labelSmall.copy(color = Text3))
                                            Text("${reportForDate.avgBpm} bpm", style = Typography.titleSmall.copy(color = Text2, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                    
                                    val statusEmoji = when {
                                        reportForDate.avgStress < 25 -> "🌸 Yên bình"
                                        reportForDate.avgStress < 50 -> "✨ Cân bằng"
                                        reportForDate.avgStress < 75 -> "⚡ Căng thẳng nhẹ"
                                        else -> "⚠️ Cảnh báo áp lực"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(if (reportForDate.avgStress > 50) PinkNeon.copy(alpha = 0.15f) else CyanNeon.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = statusEmoji,
                                            style = Typography.labelSmall.copy(
                                                color = if (reportForDate.avgStress > 50) PinkNeon else CyanNeon,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // If report is not yet generated
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Glass, RoundedCornerShape(18.dp))
                                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📊", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Báo cáo tổng hợp ngày chưa tạo",
                                    style = Typography.titleSmall.copy(color = Text1, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bạn có ${sessionsForDate.size} phiên ghi nhận cảm xúc trong ngày. Hãy để Aura AI tổng hợp chỉ số stress, nhịp tim và đưa ra đánh giá, lời khuyên dành riêng cho bạn.",
                                    style = Typography.bodySmall.copy(color = Text2, lineHeight = 18.sp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.linearGradient(listOf(PinkNeon, Purple)), RoundedCornerShape(14.dp))
                                        .clickable { vm.generateAndSaveDailyReport(selectedDate, sessionsForDate) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Tổng hợp báo cáo ngày với Aura AI ✨",
                                        style = Typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 3.8. Daily Trend Line Chart (Stress & BPM)
        if (selectedDate.isNotEmpty()) {
            val sessionsForDate = dateGroups[selectedDate]?.sortedBy { it.timestamp } ?: emptyList()
            if (sessionsForDate.size > 1) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "BIỂU ĐỒ XU HƯỚNG TRONG NGÀY",
                            style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Glass, RoundedCornerShape(16.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                
                                val maxStress = 100f
                                val minStress = 0f
                                val maxBpm = 180f
                                val minBpm = 40f
                                
                                val stressPoints = sessionsForDate.mapIndexed { index, session ->
                                    val x = (index.toFloat() / (sessionsForDate.size - 1)) * width
                                    val y = height - ((session.stressLevel.toFloat() - minStress) / (maxStress - minStress)) * height
                                    androidx.compose.ui.geometry.Offset(x, y)
                                }
                                
                                val bpmPoints = sessionsForDate.mapIndexed { index, session ->
                                    val x = (index.toFloat() / (sessionsForDate.size - 1)) * width
                                    val y = height - ((session.bpm.toFloat() - minBpm) / (maxBpm - minBpm)) * height
                                    androidx.compose.ui.geometry.Offset(x, y)
                                }
                                
                                // Draw horizontal grid lines
                                for (i in 0..4) {
                                    val y = height - (i * height / 4)
                                    drawLine(
                                        color = GlassBorder,
                                        start = androidx.compose.ui.geometry.Offset(0f, y),
                                        end = androidx.compose.ui.geometry.Offset(width, y),
                                        strokeWidth = 1f
                                    )
                                }
                                
                                // Draw Stress Line (Pink)
                                val stressPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(stressPoints.first().x, stressPoints.first().y)
                                    for (i in 1 until stressPoints.size) {
                                        lineTo(stressPoints[i].x, stressPoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = stressPath,
                                    color = PinkNeon,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                                )
                                stressPoints.forEach { point ->
                                    drawCircle(color = PinkNeon, radius = 6f, center = point)
                                    drawCircle(color = Bg1, radius = 3f, center = point)
                                }
                                
                                // Draw BPM Line (Cyan)
                                val bpmPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(bpmPoints.first().x, bpmPoints.first().y)
                                    for (i in 1 until bpmPoints.size) {
                                        lineTo(bpmPoints[i].x, bpmPoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = bpmPath,
                                    color = CyanNeon,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                                )
                                bpmPoints.forEach { point ->
                                    drawCircle(color = CyanNeon, radius = 5f, center = point)
                                }
                            }
                            
                            // Legend
                            Row(
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(PinkNeon, androidx.compose.foundation.shape.CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stress (%)", style = Typography.labelSmall.copy(color = Text3, fontSize = 10.sp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(CyanNeon, androidx.compose.foundation.shape.CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("BPM", style = Typography.labelSmall.copy(color = Text3, fontSize = 10.sp))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 4. Time Timeline feed for selected date

        val sessionsList = if (selectedDate.isNotEmpty()) {
            dateGroups[selectedDate]?.sortedByDescending { it.timestamp } ?: emptyList()
        } else {
            allSessions.sortedByDescending { it.timestamp }
        }
        
        if (sessionsList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💮", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có dữ liệu cảm xúc cho ngày này",
                        style = Typography.bodyMedium.copy(color = Text3)
                    )
                    Text(
                        text = "Hãy trò chuyện hoặc đo bằng giọng nói để ghi nhật ký!",
                        style = Typography.bodySmall.copy(color = Text4),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            items(sessionsList) { session ->
                MoodDiaryRow(
                    session = session,
                    onSelect = {
                        highlightedTips = getEmotionTip(session.stressLevel, session.source)
                    }
                )
            }
        }
    }
}

@Composable
fun StatBox(title: String, value: String, emoji: String, bgGradient: Brush, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgGradient)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = title, style = Typography.bodySmall.copy(color = Text3))
            Text(text = emoji, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = Typography.titleLarge.copy(color = CyanNeon, fontWeight = FontWeight.Bold)
        )
    }
}

private data class RowMeta(val emoji: String, val stateName: String, val color: Color, val label: String)

@Composable
fun MoodDiaryRow(session: ChartSession, onSelect: () -> Unit) {
    val meta = remember(session.stressLevel, session.source) {
        when {
            session.source.contains("Lo âu") -> {
                when {
                    session.stressLevel < 25 -> RowMeta("🌸", "Thư thái & Ổn định", CyanNeon, "Lo âu")
                    session.stressLevel < 50 -> RowMeta("🧘", "Hơi bồn chồn bứt rứt", GreenNeon, "Lo âu")
                    session.stressLevel < 75 -> RowMeta("😟", "Bất an lo lắng", AmberNeon, "Lo âu")
                    else -> RowMeta("⚠️", "Hoảng sợ lo âu cao", PinkNeon, "Lo âu")
                }
            }
            session.source.contains("ADHD") -> {
                when {
                    session.stressLevel < 25 -> RowMeta("🎯", "Tập trung nhịp nhàng", CyanNeon, "ADHD")
                    session.stressLevel < 50 -> RowMeta("🧩", "Mất chú ý xao nhãng", GreenNeon, "ADHD")
                    session.stressLevel < 75 -> RowMeta("⚡", "Kích động bốc đồng", AmberNeon, "ADHD")
                    else -> RowMeta("⚠️", "Cản trở tập trung cao", PinkNeon, "ADHD")
                }
            }
            session.source.contains("Mất ngủ") -> {
                when {
                    session.stressLevel < 25 -> RowMeta("😴", "Ngủ ngon khoẻ khoắn", CyanNeon, "Mất ngủ")
                    session.stressLevel < 50 -> RowMeta("🛌", "Chập chờn trằn trọc", GreenNeon, "Mất ngủ")
                    session.stressLevel < 75 -> RowMeta("⏰", "Thức giấc lúc nửa đêm", AmberNeon, "Mất ngủ")
                    else -> RowMeta("⚠️", "Mất ngủ mệt mỏi", PinkNeon, "Mất ngủ")
                }
            }
            else -> {
                when {
                    session.stressLevel < 25 -> RowMeta("🌸", "An yên & Thư thái", CyanNeon, "Stress")
                    session.stressLevel < 50 -> RowMeta("🧘", "Cân bằng tích cực", GreenNeon, "Stress")
                    session.stressLevel < 75 -> RowMeta("⚡", "Căng thẳng nhẹ", AmberNeon, "Stress")
                    else -> RowMeta("⚠️", "Quá tải (Stress cao)", PinkNeon, "Stress")
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .fillMaxWidth()
            .clickable { onSelect() }
            .clip(RoundedCornerShape(18.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Emoji container matching glowing state color
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(meta.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, meta.color.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(meta.emoji, fontSize = 22.sp)
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        // Middle entry details
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = meta.stateName,
                    style = Typography.labelMedium.copy(color = Text1, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "⏰ ${session.timeStr}",
                    style = Typography.bodySmall.copy(color = Text3)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${meta.label}: ${session.stressLevel}%",
                    style = Typography.bodySmall.copy(color = meta.color, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Nhịp tim: ${session.bpm} BPM",
                    style = Typography.bodySmall.copy(color = Text2)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            // Bottom source badge indicating voice, chatbot or general bionics
            Row(verticalAlignment = Alignment.CenterVertically) {
                val badgeIcon = when {
                    session.source.contains("Lo âu") -> "😟"
                    session.source.contains("ADHD") -> "🧠"
                    session.source.contains("Mất ngủ") -> "😴"
                    session.source.contains("Giọng nói") -> "🎙"
                    session.source.contains("Aura AI") -> "🤖"
                    else -> "🌿"
                }
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$badgeIcon Phiên đo từ: ${session.source}",
                        style = Typography.labelSmall.copy(color = Text2, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

fun getEmotionTip(stress: Int, source: String): String {
    return when {
        source.contains("Lo âu") -> {
            when {
                stress < 25 -> "Chỉ số lo âu rất thấp ($stress%). Bạn đang kiểm soát lo âu xuất sắc, hãy tiếp tục duy trì!"
                stress < 50 -> "Mức lo lắng bồn chồn nhẹ ($stress%). Hãy áp dụng các bài thở nhịp nhàng để lấy lại thăng bằng."
                stress < 75 -> "Phát hiện mức lo âu đáng kể ($stress%). Đăng ký nghe nhạc healing giải phòng tâm lý và kéo dãn nhẹ."
                else -> "Cảnh báo hoảng loạn bồn chồn cao ($stress%)! Xin vui lòng tìm không gian tĩnh lặng, thở sâu 4-4-4 và thả lỏng."
            }
        }
        source.contains("ADHD") -> {
            when {
                stress < 25 -> "Sự tập trung tuyệt vời ($stress%). Thích hợp hoàn thành các nhiệm vụ phức tạp, sáng tạo."
                stress < 50 -> "Bắt đầu xao nhãng nhẹ ($stress%). Hãy dọn bàn, lọc tiếng ồn và tập trung từng quãng ngắn Pomodoro."
                stress < 75 -> "Chỉ số mất tập trung bốc đồng cao ($stress%). Xin hãy dừng màn hình lướt tin tức, hít thở và uống nước."
                else -> "Mất tập trung kích động cao ($stress%). Đề nghị nghỉ ngơi hoàn toàn, nghe nhạc alpha focus để xoa dịu."
            }
        }
        source.contains("Mất ngủ") -> {
            when {
                stress < 25 -> "Chất lượng ngủ lý tưởng ($stress%). Cơ thể bạn được khôi phục năng lượng tối ưu!"
                stress < 50 -> "Trằn trọc chập chờn nhẹ ($stress%). Gợi ý tắt đèn sớm, hạn chế xem điện thoại trước khi đi ngủ."
                stress < 75 -> "Giấc ngủ nông, dễ giật mình tỉnh giấc ($stress%). Bạn nên mở âm thanh tự nhiên hoặc sấm mưa để hỗ trợ."
                else -> "Trạng thái mất ngủ mệt mỏi trầm trọng ($stress%). Hãy dãn cơ nhẹ, ngâm chân nước ấm và giữ tinh thần tối giản."
            }
        }
        else -> {
            when {
                stress < 25 -> "Chỉ số hoàn hảo! Tâm trạng bạn đang vô cùng an yên ($stress%). Hãy tận hưởng khoảnh khắc tuyệt vời này nhé!"
                stress < 50 -> "Cảm xúc đang cân bằng và ôn hoà ($stress%). Thích hợp cho việc sáng tạo, tập trung suy ngẫm."
                stress < 75 -> "Phát hiện chỉ số căng thẳng nhẹ ($stress%) qua $source. Gợi ý bạn nghe một bản nhạc Healing tần số 432Hz ở tab Healing để thư giãn."
                else -> "Cảnh báo Stress Quá Tải ($stress%) đo từ $source! Nhịp tim đang tăng cao. Vui lòng dừng kết nối, hít thở nhịp nhàng 4-4-4 và thả lỏng tâm trí."
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: NavController, vm: AuraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val records by vm.gadgetbridgeRecords.collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg0),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            TopBar("Gadgetbridge", "Sync Journal", "Nhật ký đồng bộ")
        }
        
        // Stats Overview cards
        item {
            val latestBpm = records.firstOrNull { it.bpm != null }?.bpm
            val latestSpo2 = records.firstOrNull { it.spo2 != null }?.spo2
            val latestSleep = records.firstOrNull { it.sleepScore != null }
            
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassCardH(
                    V = latestBpm?.toString() ?: "--",
                    L = "BPM Gần Nhất",
                    unit = " ❤️",
                    color = RedNeon,
                    modifier = Modifier.weight(1f)
                )
                GlassCardH(
                    V = latestSpo2?.let { "$it%" } ?: "--",
                    L = "SpO2 Gần Nhất",
                    unit = " 🩸",
                    color = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                GlassCardH(
                    V = latestSleep?.sleepScore?.toString() ?: "--",
                    L = "Điểm Giấc Ngủ",
                    unit = "/100 😴",
                    color = PurpleNeon,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action block (Clear history)
        if (records.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Red.copy(alpha = 0.15f))
                            .border(1.dp, Red.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .clickable { vm.clearGadgetbridgeHistory() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🗑️ Xóa lịch sử đồng bộ",
                            style = Typography.labelSmall.copy(color = RedNeon, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // List Header
        item {
            Text(
                text = "LỊCH SỬ DỮ LIỆU ĐỒNG BỘ",
                style = Typography.labelMedium.copy(color = Text3, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        if (records.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⌚", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có dữ liệu lịch sử đồng bộ",
                        style = Typography.bodyMedium.copy(color = Text3)
                    )
                    Text(
                        text = "Hãy đồng bộ dữ liệu với Gadgetbridge từ Cài đặt để bắt đầu!",
                        style = Typography.bodySmall.copy(color = Text4),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            items(records) { record ->
                GadgetbridgeRecordRow(record, sdf)
            }
        }
    }
}

@Composable
fun GadgetbridgeRecordRow(record: GadgetbridgeHealthRecord, sdf: SimpleDateFormat) {
    val formattedTime = remember(record.timestamp) { sdf.format(Date(record.timestamp)) }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔄", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đồng bộ từ thiết bị",
                        style = Typography.labelMedium.copy(color = Text1, fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = formattedTime,
                    style = Typography.bodySmall.copy(color = Text3, fontSize = 11.sp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❤️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nhịp tim", style = Typography.bodySmall.copy(color = Text3))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.bpm?.let { "$it BPM" } ?: "N/A",
                        style = Typography.bodyMedium.copy(
                            color = if (record.bpm != null) RedNeon else Text2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🩸", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SpO2", style = Typography.bodySmall.copy(color = Text3))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.spo2?.let { "$it%" } ?: "N/A",
                        style = Typography.bodyMedium.copy(
                            color = if (record.spo2 != null) CyanNeon else Text2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("😴", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Giấc ngủ", style = Typography.bodySmall.copy(color = Text3))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val sleepStr = remember(record.sleepMinutes) {
                        if (record.sleepMinutes != null) {
                            val h = record.sleepMinutes / 60
                            val m = record.sleepMinutes % 60
                            "${h}g ${m}p"
                        } else {
                            "N/A"
                        }
                    }
                    val scoreStr = if (record.sleepScore != null) " (Đ: ${record.sleepScore})" else ""
                    Text(
                        text = "$sleepStr$scoreStr",
                        style = Typography.bodyMedium.copy(
                            color = if (record.sleepScore != null) PurpleNeon else Text2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏃", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bước chân", style = Typography.bodySmall.copy(color = Text3))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.steps?.let { "$it bước" } ?: "N/A",
                        style = Typography.bodyMedium.copy(
                            color = if (record.steps != null) GreenNeon else Text2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hoạt động", style = Typography.bodySmall.copy(color = Text3))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = record.activeMinutes?.let { "$it phút" } ?: "N/A",
                        style = Typography.bodyMedium.copy(
                            color = if (record.activeMinutes != null) AmberNeon else Text2,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1.2f))
            }
        }
    }
}

@Composable
fun GlassCardH(
    V: String, 
    L: String, 
    modifier: Modifier = Modifier,
    unit: String = "",
    color: Color = PurpleNeon
) {
    Column(
        modifier = modifier
            .background(Glass, RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(V, style = Typography.titleLarge.copy(color = color))
            if (unit.isNotEmpty()) {
                Text(unit, style = Typography.bodySmall.copy(color = Text2))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(L, style = Typography.labelSmall.copy(color = Text3), fontSize = 11.sp)
    }
}
