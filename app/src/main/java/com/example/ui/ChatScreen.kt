package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.TopBar
import com.example.ui.components.glow
import com.example.ui.theme.*

@Composable
fun ChatScreen(navController: NavController, vm: AuraViewModel = viewModel()) {
    val messages by vm.messages.collectAsState()
    val isTyping by vm.isTyping.collectAsState()
    val textToSpeak by vm.textToSpeak.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        vm.currentVoiceTarget = "chat"
    }
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }

    DisposableEffect(context) {
        val textToSpeech = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts?.language = java.util.Locale("vi", "VN")
            }
        }
        textToSpeech.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                vm.onSpeechStarted()
            }
            override fun onDone(utteranceId: String?) {
                vm.onSpeechFinished()
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                vm.onSpeechFinished()
            }
            override fun onError(utteranceId: String?, errorCode: Int) {
                vm.onSpeechFinished()
            }
        })
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(textToSpeak) {
        textToSpeak?.let { text ->
            var success = false
            if (tts != null) {
                val params = android.os.Bundle().apply {
                    putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ai_chat")
                }
                val result = tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "ai_chat")
                if (result == android.speech.tts.TextToSpeech.SUCCESS) {
                    success = true
                }
            }
            if (!success) {
                vm.onSpeechFinished()
            }
            vm.resetTextToSpeak()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Bg0)) {
        TopBar("AURA", "Assistant", "Trợ lý AI")
        AiHeader()
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
            if(isTyping) {
                item { TypingIndicator() }
            }
        }
        ChatInput(vm)
    }
}

@Composable
fun AiHeader() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Purple.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .border(1.dp, PurpleNeon.copy(alpha = 0.35f), RoundedCornerShape(24.dp)), 
            contentAlignment = Alignment.Center
        ) {
            Text("🤖", fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("AURA — Emotion AI", style = Typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
            Text("Trực tuyến · Chăm sóc sức khoẻ tâm hồn", style = Typography.bodySmall.copy(color = GreenNeon, fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val alignment = if (msg.isAi) Alignment.CenterStart else Alignment.CenterEnd
    
    // Aesthetic color schemes
    val bgBrush = if (msg.isAi) {
        androidx.compose.ui.graphics.SolidColor(GlassHi)
    } else {
        Brush.linearGradient(listOf(Purple.copy(0.35f), Blue.copy(0.35f)))
    }
    
    val borderCol = if (msg.isAi) {
        PurpleNeon.copy(alpha = 0.25f)
    } else {
        CyanNeon.copy(alpha = 0.3f)
    }
    
    val shape = if (msg.isAi) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(), 
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bgBrush, shape)
                .border(1.dp, borderCol, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = msg.text, 
                style = Typography.bodyLarge.copy(
                    color = Text1,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = msg.time, 
                style = Typography.bodySmall.copy(
                    color = Text3,
                    fontSize = 11.sp
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .background(GlassHi, RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
            .border(1.dp, PurpleNeon.copy(alpha = 0.15f), RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("đang soạn thảo...", style = Typography.bodyMedium.copy(color = PurpleNeon, fontWeight = FontWeight.Medium))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(vm: AuraViewModel) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val voiceState by vm.voiceState.collectAsState()
    val isRecording = voiceState == VoiceState.LISTENING
    val context = androidx.compose.ui.platform.LocalContext.current

        val recordAudioLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                vm.toggleVoiceListening(context)
            } else {
                android.widget.Toast.makeText(context, "Cần cấp quyền ghi âm để sử dụng tính năng này", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

    // Modern floating bottom entry box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xE604041A)) // solid dark barrier at bottom
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Glass)
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accessible 48dp x 48dp microphone button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isRecording) Brush.linearGradient(listOf(Color.Red, PinkNeon)) else androidx.compose.ui.graphics.SolidColor(GlassHi))
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .clickable {
                        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)
                        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            vm.toggleVoiceListening(context)
                        } else {
                            recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(if (isRecording) "⏹" else "🎤", fontSize = 18.sp, color = Color.White)
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(if (isRecording) "Đang ghi âm..." else "Chia sẻ nỗi lòng của bạn...", style = Typography.bodyMedium.copy(color = Text3)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, 
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, 
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Text1, 
                    unfocusedTextColor = Text1
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Accessible 48dp x 48dp Send button with modern gradient and glowing effect
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(PurpleNeon, CyanNeon)))
                    .glow(color = PurpleNeon, radius = 10.dp, alpha = 0.3f)
                    .clickable {
                        if (text.text.isNotBlank()) {
                            vm.sendMessage(text.text)
                            text = TextFieldValue("")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("➤", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}
