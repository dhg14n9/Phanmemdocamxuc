package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.AppCopyrightFooter
import com.example.ui.components.TopBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, vm: AuraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var geminiKey by remember { mutableStateOf(AppPreferences.geminiKey) }
    var spotifyToken by remember { mutableStateOf(AppPreferences.spotifyToken) }
    var blynkToken by remember { mutableStateOf(AppPreferences.blynkToken) }
    var blynkPinBpm by remember { mutableStateOf(AppPreferences.blynkPinBpm) }
    var blynkPinSpo2 by remember { mutableStateOf(AppPreferences.blynkPinSpo2) }
    
    var userGender by remember { mutableStateOf(AppPreferences.userGender) }
    var userWeight by remember { mutableStateOf(AppPreferences.userWeight.toString()) }
    var userHeight by remember { mutableStateOf(AppPreferences.userHeight.toString()) }
    var gadgetbridgeDbUri by remember { mutableStateOf(AppPreferences.gadgetbridgeDbUri) }
    var gadgetbridgeSyncIntervalMinutes by remember { mutableStateOf(AppPreferences.gadgetbridgeSyncIntervalMinutes.toString()) }
    
    var showPassword by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TopBar("Cài đặt", "Hệ thống", "Cấu hình") { navController.popBackStack() }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("API AI & Data", style = Typography.titleMedium.copy(color = Text1))
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gemini Settings
                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column {
                        Text("GEMINI API KEY", style = Typography.labelSmall.copy(color = PurpleNeon))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = geminiKey,
                            onValueChange = { geminiKey = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleNeon,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Text1,
                                unfocusedTextColor = Text1,
                                cursorColor = PurpleNeon
                            ),
                            placeholder = { Text("Nhập API Key") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                androidx.compose.material3.IconButton(onClick = { showPassword = !showPassword }) {
                                    Text(if (showPassword) "🫣" else "👁️", fontSize = 18.sp)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("KẾT NỐI API NHẠC TRẺ (SPOTIFY)", style = Typography.labelSmall.copy(color = PinkNeon))
                        Spacer(modifier = Modifier.height(8.dp))
                        var showSpotify by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = spotifyToken,
                            onValueChange = { spotifyToken = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkNeon,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Text1,
                                unfocusedTextColor = Text1,
                                cursorColor = PinkNeon
                            ),
                            placeholder = { Text("Nhập Spotify Client Secret / Token") },
                            visualTransformation = if (showSpotify) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                androidx.compose.material3.IconButton(onClick = { showSpotify = !showSpotify }) {
                                    Text(if (showSpotify) "🫣" else "👁️", fontSize = 18.sp)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Blynk Settings
                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("BLYNK IoT CONFIG", style = Typography.labelSmall.copy(color = CyanNeon))
                        
                        OutlinedTextField(
                            value = blynkToken,
                            onValueChange = { blynkToken = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Auth Token", color = Text3) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = CyanNeon, focusedLabelColor = CyanNeon, unfocusedLabelColor = Text3
                            )
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = blynkPinBpm,
                                onValueChange = { blynkPinBpm = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Pin BPM", color = Text3) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = CyanNeon, focusedLabelColor = CyanNeon, unfocusedLabelColor = Text3
                                )
                            )
                            OutlinedTextField(
                                value = blynkPinSpo2,
                                onValueChange = { blynkPinSpo2 = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Pin SpO2", color = Text3) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = CyanNeon, focusedLabelColor = CyanNeon, unfocusedLabelColor = Text3
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // THÔNG TIN THỂ CHẤT (PHYSICAL PROFILE)
                Text("Hồ sơ thể chất", style = Typography.titleMedium.copy(color = Text1))
                Spacer(modifier = Modifier.height(12.dp))
                
                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("THÔNG TIN CÁ NHÂN", style = Typography.labelSmall.copy(color = PurpleNeon))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Giới tính: ", color = Text2, style = Typography.bodyMedium, modifier = Modifier.width(80.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Nam", "Nữ", "Khác").forEach { g ->
                                    val isSelected = userGender == g
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) PurpleNeon.copy(alpha = 0.2f) else GlassHi)
                                            .border(1.dp, if (isSelected) PurpleNeon else GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable { userGender = g }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(g, color = if (isSelected) PurpleNeon else Text2, style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = userHeight,
                                onValueChange = { userHeight = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Chiều cao (cm)", color = Text3) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurpleNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = PurpleNeon, focusedLabelColor = PurpleNeon, unfocusedLabelColor = Text3
                                )
                            )
                            OutlinedTextField(
                                value = userWeight,
                                onValueChange = { userWeight = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Cân nặng (kg)", color = Text3) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurpleNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = PurpleNeon, focusedLabelColor = PurpleNeon, unfocusedLabelColor = Text3
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // ĐỒNG BỘ GADGETBRIDGE DB
                Text("Dữ liệu Gadgetbridge", style = Typography.titleMedium.copy(color = Text1))
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ĐỒNG BỘ FILE DATABASE (.DB)", style = Typography.labelSmall.copy(color = CyanNeon))
                        Text(
                            "Chọn file database của Gadgetbridge (thường có tên là 'gadgetbridge' hoặc 'gadgetbridge.db') để nhập trực tiếp lịch sử đo nhịp tim, oxy máu và tính toán chỉ số giấc ngủ.",
                            style = Typography.bodySmall.copy(color = Text2, lineHeight = 18.sp)
                        )
                        
                        val filePickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument(),
                            onResult = { uri ->
                                uri?.let {
                                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    try {
                                        context.contentResolver.takePersistableUriPermission(it, takeFlags)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    gadgetbridgeDbUri = it.toString()
                                    AppPreferences.gadgetbridgeDbUri = it.toString()
                                }
                            }
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GlassHi, RoundedCornerShape(12.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                }
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Đường dẫn file CSDL hiện tại:", style = Typography.labelSmall.copy(color = Text3))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (gadgetbridgeDbUri.isBlank()) "Bấm vào đây để chọn file" else gadgetbridgeDbUri,
                                    style = Typography.bodySmall.copy(color = if (gadgetbridgeDbUri.isBlank()) CyanNeon else Text1, fontStyle = if (gadgetbridgeDbUri.isBlank()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal),
                                    maxLines = 2
                                )
                            }
                        }

                        // Trạng thái & nút Đồng bộ thủ công
                        val syncStatus by vm.syncStatus.collectAsState()
                        val lastSync = AppPreferences.gadgetbridgeLastSync
                        val sdf = remember { java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", java.util.Locale.getDefault()) }
                        val lastSyncStr = if (lastSync == 0L) "Chưa đồng bộ" else sdf.format(java.util.Date(lastSync))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanNeon.copy(alpha = 0.08f))
                                .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "TRẠNG THÁI ĐỒNG BỘ",
                                        style = Typography.labelSmall.copy(color = CyanNeon, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (syncStatus.isNotEmpty()) syncStatus else "Lần cuối: $lastSyncStr",
                                        style = Typography.bodySmall.copy(color = Text1, fontWeight = FontWeight.Medium)
                                    )
                                }
                                if (gadgetbridgeDbUri.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CyanNeon.copy(alpha = 0.15f))
                                            .border(1.dp, CyanNeon, RoundedCornerShape(8.dp))
                                            .clickable { vm.syncGadgetbridgeDb(context) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Đồng bộ 🔄", color = CyanNeon, style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }

                        // Cấu hình thời gian chạy đồng bộ
                        OutlinedTextField(
                            value = gadgetbridgeSyncIntervalMinutes,
                            onValueChange = { gadgetbridgeSyncIntervalMinutes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tần suất tự động đồng bộ (phút)", color = Text3) },
                            placeholder = { Text("Mặc định: 15 phút") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = CyanNeon, focusedLabelColor = CyanNeon, unfocusedLabelColor = Text3
                            )
                        )
                        
                        Text(
                            "Hướng dẫn Auto-Export:\nTrong Gadgetbridge, bật 'Auto Export' ở phần Cài đặt và cấu hình thư mục lưu trữ. Chọn đúng file .db được tự động xuất ra đó.\n\n" +
                            "💡 Mẹo cập nhật nhanh (5-10 phút):\n" +
                            "• Máy Root: Trỏ trực tiếp file tại /data/data/nodomain.freeyourgadget.gadgetbridge/databases/gadgetbridge.\n" +
                            "• Máy chưa Root: Bật 'Đo nhịp tim liên tục' (Live Activity) trong Gadgetbridge. Aura sẽ tự động nhận dữ liệu nhịp tim Real-time qua Broadcast mà không cần chờ Auto-Export.\n" +
                            "• Dùng Tasker/MacroDroid: Thiết lập copy đè file gadgetbridge sang thư mục dùng chung mỗi 5 phút.",
                            style = Typography.bodySmall.copy(color = Text3, lineHeight = 18.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Headset Settings
                var headsetEnabled by remember { mutableStateOf(AppPreferences.headsetModeEnabled) }
                var headsetBeep by remember { mutableStateOf(AppPreferences.headsetBeepFeedback) }

                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("CHẾ ĐỘ TAI NGHE THÔNG MINH", style = Typography.labelSmall.copy(color = CyanNeon, fontWeight = FontWeight.Bold))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bật điều khiển qua Tai nghe", color = Text1, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Cho phép sử dụng nút cuộc gọi / Media trên tai nghe Bluetooth để bật/tắt ghi âm Aura AI", color = Text2, style = Typography.bodySmall)
                            }
                            androidx.compose.material3.Switch(
                                checked = headsetEnabled,
                                onCheckedChange = { checked ->
                                    headsetEnabled = checked
                                    AppPreferences.headsetModeEnabled = checked
                                },
                                colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = CyanNeon, checkedTrackColor = CyanNeon.copy(alpha = 0.4f))
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Âm thanh phản hồi (Beep)", color = Text1, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Phát âm bíp qua tai nghe khi bắt đầu và kết thúc nhận diện giọng nói", color = Text2, style = Typography.bodySmall)
                            }
                            androidx.compose.material3.Switch(
                                checked = headsetBeep,
                                onCheckedChange = { checked ->
                                    headsetBeep = checked
                                    AppPreferences.headsetBeepFeedback = checked
                                },
                                colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = CyanNeon, checkedTrackColor = CyanNeon.copy(alpha = 0.4f))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(GlassBorder))
                        Spacer(modifier = Modifier.height(4.dp))

                        // Headset Button Map Card UI
                        Text("SƠ ĐỒ PHÍM BẤM (YY706 / BLUETOOTH HEADSET)", style = Typography.labelSmall.copy(color = Text3, fontWeight = FontWeight.Bold))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("➕  ", style = Typography.bodyMedium)
                                Column {
                                    Text("Nút Tăng Âm Lượng (+)", color = Text1, style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Bấm 1 lần: Tăng âm lượng trợ lý / nhạc thiền", color = Text2, style = Typography.bodySmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("⭕  ", style = Typography.bodyMedium)
                                Column {
                                    Text("Nút Cuộc Gọi / Đa Năng (O)", color = Text1, style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Bấm 1 lần: Bật/Tắt Mic nói chuyện rảnh tay (Aura AI & Giọng nói)", color = CyanNeon, style = Typography.bodySmall)
                                    Text("Bấm giữ lâu: Tạm dừng / Tiếp tục phát nhạc thiền", color = Text2, style = Typography.bodySmall)
                                }
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("➖  ", style = Typography.bodyMedium)
                                Column {
                                    Text("Nút Giảm Âm Lượng (-)", color = Text1, style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Bấm 1 lần: Giảm âm lượng trợ lý / nhạc thiền", color = Text2, style = Typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Webhook Settings
                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("WEBHOOK BÁO CÁO TÌNH TRẠNG (Google Sheets)", style = Typography.labelSmall.copy(color = GreenNeon))
                        
                        var webhookUrl by remember { mutableStateOf(AppPreferences.webhookUrl) }
                        OutlinedTextField(
                            value = webhookUrl,
                            onValueChange = { 
                                webhookUrl = it 
                                AppPreferences.webhookUrl = it 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Webhook / Google Form URL", color = Text3) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenNeon, unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = GreenNeon, focusedLabelColor = GreenNeon, unfocusedLabelColor = Text3
                            )
                        )
                        Text(
                            "Dữ liệu cảnh báo (VD: Stress kéo dài) sẽ tự động được gửi qua dạng POST đến URL này.",
                            style = Typography.bodySmall.copy(color = Text2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var emergencyPhone by remember { mutableStateOf(AppPreferences.emergencyPhone) }
                        OutlinedTextField(
                            value = emergencyPhone,
                            onValueChange = { 
                                emergencyPhone = it 
                                AppPreferences.emergencyPhone = it 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Số điện thoại SOS", color = Text3) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF5555), unfocusedBorderColor = GlassBorder, focusedTextColor = Text1, unfocusedTextColor = Text1, cursorColor = Color(0xFFFF5555), focusedLabelColor = Color(0xFFFF5555), unfocusedLabelColor = Text3
                            )
                        )
                        Text(
                            "Số điện thoại sẽ được tự động gọi khi bạn nhấn nút SOS Khẩn Cấp.",
                            style = Typography.bodySmall.copy(color = Text2)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Settings
                Box(modifier = Modifier.fillMaxWidth().background(Glass).border(1.dp, GlassBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("THÔNG BÁO NHẮC NHỞ", style = Typography.labelSmall.copy(color = PinkNeon))
                        
                        var reminderEnabled by remember { mutableStateOf(AppPreferences.reminderEnabled) }
                        val context = androidx.compose.ui.platform.LocalContext.current
                        
                        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (!isGranted) {
                                reminderEnabled = false
                                AppPreferences.reminderEnabled = false
                                android.widget.Toast.makeText(context, "Không thể nhắc nhở nếu không có quyền", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        
                        var reminderHour by remember { mutableStateOf(AppPreferences.reminderHour) }
                        var reminderMinute by remember { mutableStateOf(AppPreferences.reminderMinute) }
                        
                        val timePickerDialog = android.app.TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                reminderHour = hour
                                reminderMinute = minute
                                AppPreferences.reminderHour = hour
                                AppPreferences.reminderMinute = minute
                                if (reminderEnabled) {
                                    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                    val intent = android.content.Intent(context, com.example.ReminderReceiver::class.java)
                                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                                        context, 1001, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                    )
                                    val calendar = java.util.Calendar.getInstance().apply {
                                        timeInMillis = System.currentTimeMillis()
                                        set(java.util.Calendar.HOUR_OF_DAY, hour)
                                        set(java.util.Calendar.MINUTE, minute)
                                        set(java.util.Calendar.SECOND, 0)
                                    }
                                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                    }
                                    alarmManager.setRepeating(
                                        android.app.AlarmManager.RTC_WAKEUP,
                                        calendar.timeInMillis,
                                        android.app.AlarmManager.INTERVAL_DAY,
                                        pendingIntent
                                    )
                                    android.widget.Toast.makeText(context, "Đã đặt nhắc nhở lúc ${String.format("%02d:%02d", hour, minute)} hàng ngày", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            reminderHour,
                            reminderMinute,
                            true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Bật nhắc nhở hàng ngày", color = Text1)
                                if (reminderEnabled) {
                                    Text(
                                        text = "Thời gian: ${String.format("%02d:%02d", reminderHour, reminderMinute)}", 
                                        color = CyanNeon, 
                                        style = Typography.bodySmall,
                                        modifier = Modifier.clickable { timePickerDialog.show() }.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            androidx.compose.material3.Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { checked ->
                                    reminderEnabled = checked
                                    AppPreferences.reminderEnabled = checked
                                    
                                    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                    val intent = android.content.Intent(context, com.example.ReminderReceiver::class.java)
                                    val pendingIntent = android.app.PendingIntent.getBroadcast(
                                        context, 1001, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                    )
                                    
                                    if (checked) {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }
                                        val calendar = java.util.Calendar.getInstance().apply {
                                            timeInMillis = System.currentTimeMillis()
                                            set(java.util.Calendar.HOUR_OF_DAY, reminderHour)
                                            set(java.util.Calendar.MINUTE, reminderMinute)
                                            set(java.util.Calendar.SECOND, 0)
                                        }
                                        if (calendar.timeInMillis <= System.currentTimeMillis()) {
                                            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                        }
                                        alarmManager.setRepeating(
                                            android.app.AlarmManager.RTC_WAKEUP,
                                            calendar.timeInMillis,
                                            android.app.AlarmManager.INTERVAL_DAY,
                                            pendingIntent
                                        )
                                        android.widget.Toast.makeText(context, "Đã bật nhắc nhở lúc ${String.format("%02d:%02d", reminderHour, reminderMinute)}", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        alarmManager.cancel(pendingIntent)
                                        android.widget.Toast.makeText(context, "Đã tắt nhắc nhở", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = PinkNeon, checkedTrackColor = PinkNeon.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Brush.linearGradient(listOf(PurpleNeon, CyanNeon)), RoundedCornerShape(28.dp))
                        .clickable {
                            AppPreferences.geminiKey = geminiKey
                            AppPreferences.spotifyToken = spotifyToken
                            AppPreferences.blynkToken = blynkToken
                            AppPreferences.blynkPinBpm = blynkPinBpm
                            AppPreferences.blynkPinSpo2 = blynkPinSpo2
                            AppPreferences.userGender = userGender
                            AppPreferences.userWeight = userWeight.toFloatOrNull() ?: 65.0f
                            AppPreferences.userHeight = userHeight.toFloatOrNull() ?: 170.0f
                            AppPreferences.gadgetbridgeDbUri = gadgetbridgeDbUri
                            gadgetbridgeSyncIntervalMinutes.toIntOrNull()?.let {
                                if (it > 0) AppPreferences.gadgetbridgeSyncIntervalMinutes = it
                            }
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Lưu & Áp Dụng", style = Typography.bodyLarge.copy(color = Color.White))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                AppCopyrightFooter()
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
