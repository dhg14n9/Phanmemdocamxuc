package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ChartSession
import com.example.data.DailyReport
import com.example.data.GadgetbridgeHealthRecord
import com.example.data.ChatMessage as DbChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(val text: String, val isAi: Boolean, val time: String)
data class MusicTrack(val emoji: String, val title: String, val desc: String, val time: String, val url: String)
data class TrendAlert(val message: String, val trendType: String, val dates: List<String>, val values: List<Double>)

enum class MeasurementMode(val key: String, val title: String, val emoji: String, val desc: String) {
    STANDARD("STANDARD", "Bình thường", "😌", "Phân tích bình thản, căng thẳng, vui vẻ"),
    STRESS("STRESS", "Stress", "😫", "Đo lường căng thẳng, áp lực, mệt mỏi"),
    ANXIETY("ANXIETY", "Lo âu", "😟", "Đo bồn chồn, lo lắng, bất an"),
    ADHD("ADHD", "ADHD", "🧠", "Phân tích xao nhãng, bốc đồng, tăng động"),
    INSOMNIA("INSOMNIA", "Giấc ngủ", "😴", "Kiểm tra chất lượng ngủ, trằn trọc")
}

enum class VoiceState {
    IDLE, LISTENING, ANALYZING, RESPONDING
}

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository = AppRepository(AppDatabase.getDatabase(application))

    private val _bpm = MutableStateFlow(88)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()

    private val _spo2 = MutableStateFlow(97)
    val spo2: StateFlow<Int> = _spo2.asStateFlow()

    // Sleep Metrics & Sync Status
    private val _sleepMinutes = MutableStateFlow<Int?>(null)
    val sleepMinutes: StateFlow<Int?> = _sleepMinutes.asStateFlow()

    private val _deepSleepMinutes = MutableStateFlow<Int?>(null)
    val deepSleepMinutes: StateFlow<Int?> = _deepSleepMinutes.asStateFlow()

    private val _sleepScore = MutableStateFlow<Int?>(null)
    val sleepScore: StateFlow<Int?> = _sleepScore.asStateFlow()

    private val _steps = MutableStateFlow<Int?>(null)
    val steps: StateFlow<Int?> = _steps.asStateFlow()

    private val _activeMinutes = MutableStateFlow<Int?>(null)
    val activeMinutes: StateFlow<Int?> = _activeMinutes.asStateFlow()

    private val _syncStatus = MutableStateFlow<String>("")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isCurrentTrackFavorite = MutableStateFlow(false)
    val isCurrentTrackFavorite = _isCurrentTrackFavorite.asStateFlow()

    private val _favoriteTracksList = MutableStateFlow(AppPreferences.favoriteTracks)
    val favoriteTracksList = _favoriteTracksList.asStateFlow()

    private val _musicList = MutableStateFlow<List<MusicTrack>>(listOf(
        // Healing (Default)
        MusicTrack("🌊", "Ocean Waves — Deep Healing", "Bình thường, Lo âu", "6:12", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
        MusicTrack("🌙", "Midnight Rain", "Bình thường, Giấc ngủ", "7:05", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
        MusicTrack("🌌", "Stellar Ambience", "Lo âu, Stress", "8:45", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
        // Thiên nhiên
        MusicTrack("🌲", "Forest Morning Calm", "Bình thường, Stress", "5:44", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"),
        MusicTrack("🔥", "Warm Fireplace", "Lo âu", "8:10", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"),
        MusicTrack("🌧️", "Rain on Window", "Giấc ngủ", "4:30", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"),
        // Deep Focus
        MusicTrack("🧠", "Alpha Waves Focus", "ADHD, Stress", "12:00", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"),
        MusicTrack("☕", "Coffee Shop Ambience", "ADHD", "10:15", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"),
        // Sleep
        MusicTrack("😴", "Deep Sleep Delta", "Giấc ngủ", "15:00", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3"),
        // Nhạc trẻ
        MusicTrack("🎧", "Lofi Vietnamese Collection", "Bình thường, Stress", "18:20", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3"),
        MusicTrack("🎵", "Offline Relaxing Ambient", "Tất cả", "0:30", "android.resource://com.aistudio.aura.xxyyzz/raw/offline_relax"),
        MusicTrack("🎤", "Acoustic V-Pop", "Bình thường", "14:15", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3")
    ))
    val musicList = _musicList.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicTrack>(_musicList.value[0])
    val currentTrack = _currentTrack.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val allChartSessions = repository.allChartSessions.map { list ->
        list.filter { it.source != "Sinh học" }
    }
    val dbMessages = repository.allChatMessages
    val gadgetbridgeRecords = repository.allGadgetbridgeRecords
    val allDailyReports = repository.allDailyReports

    private val _dailyReportLoadingMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val dailyReportLoadingMap = _dailyReportLoadingMap.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Xin chào! 🌸 Tôi là AURA — trợ lý AI chăm sóc sức khoẻ tinh thần của bạn. Tôi đang theo dõi: nhịp tim 88 BPM và giọng nói của bạn có dấu hiệu căng thẳng nhẹ. Bạn cảm thấy thế nào?", true, "14:20")
    ))
    val messages = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private val _headsetConnected = MutableStateFlow(false)
    val headsetConnected = _headsetConnected.asStateFlow()
    
    fun updateHeadsetState(isConnected: Boolean) {
        _headsetConnected.value = isConnected
    }

    private val _watchConnected = MutableStateFlow(false)
    val watchConnected = _watchConnected.asStateFlow()

    private val _connectedWatchName = MutableStateFlow<String?>(null)
    val connectedWatchName = _connectedWatchName.asStateFlow()

    fun updateWatchConnection(isConnected: Boolean, name: String?) {
        _watchConnected.value = isConnected
        _connectedWatchName.value = name
    }

    fun checkAndConnectWatch() {
        // Can be used to check BLE or other watch integrations
    }

    private val _voiceSentiment = MutableStateFlow(Triple(64, 22, 10))
    val voiceSentiment = _voiceSentiment.asStateFlow()

    private val _voiceMode = MutableStateFlow(MeasurementMode.STANDARD)
    val voiceMode = _voiceMode.asStateFlow()

    private val _isOfflineVoiceMode = MutableStateFlow(false)
    val isOfflineVoiceMode = _isOfflineVoiceMode.asStateFlow()
    
    fun toggleOfflineVoiceMode() {
        _isOfflineVoiceMode.value = !_isOfflineVoiceMode.value
    }

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState = _voiceState.asStateFlow()

    fun setVoiceState(state: VoiceState) {
        _voiceState.value = state
    }

    var currentVoiceTarget = "voice" // "voice" or "chat"

    private var speechRecognizer: android.speech.SpeechRecognizer? = null
    private var voskModel: org.vosk.Model? = null
    private var voskSpeechService: org.vosk.android.SpeechService? = null

    fun playFeedbackTone(context: Context, toneType: Int) {
        if (!AppPreferences.headsetBeepFeedback) return
        try {
            val toneG = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
            toneG.startTone(toneType, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleVoiceListening(context: Context) {
        if (_voiceState.value == VoiceState.LISTENING) {
            stopListeningProgrammatic()
        } else if (_voiceState.value == VoiceState.IDLE) {
            startListeningProgrammatic(context)
        }
    }

    fun toggleHeadsetVoiceListening(context: Context) {
        if (!AppPreferences.headsetModeEnabled) return
        toggleVoiceListening(context)
    }


    private fun startVosk(context: android.content.Context) {
        if (voskModel == null) {
            android.widget.Toast.makeText(context, "Đang tải dữ liệu nhận diện ngoại tuyến. Vui lòng thử lại sau vài giây...", android.widget.Toast.LENGTH_SHORT).show()
            _voiceState.value = VoiceState.IDLE
            playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
            return
        }
        try {
            val recognizer = org.vosk.Recognizer(voskModel, 16000.0f)
            voskSpeechService = org.vosk.android.SpeechService(recognizer, 16000.0f)
            voskSpeechService?.startListening(object : org.vosk.android.RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {}
                
                override fun onResult(hypothesis: String?) {
                    if (hypothesis != null) {
                        try {
                            val json = org.json.JSONObject(hypothesis)
                            val text = json.getString("text")
                            if (text.isNotBlank()) {
                                playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_ACK)
                                if (currentVoiceTarget == "chat") {
                                    sendMessage(text)
                                } else {
                                    analyzeVoiceSentiment(text)
                                }
                                voskSpeechService?.stop(); voskSpeechService = null; return
                            }
                        } catch (e: Exception) {}
                    }
                    _voiceState.value = VoiceState.IDLE
                    playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
                }
                
                override fun onFinalResult(hypothesis: String?) {
                    onResult(hypothesis)
                }
                
                override fun onError(exception: Exception?) {
                    _voiceState.value = VoiceState.IDLE
                    playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
                    android.widget.Toast.makeText(context, "Lỗi Vosk: ${exception?.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                override fun onTimeout() {
                    _voiceState.value = VoiceState.IDLE
                    playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
                    android.widget.Toast.makeText(context, "Hết thời gian chờ", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            _voiceState.value = VoiceState.IDLE
            playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
        }
    }
    fun startListeningProgrammatic(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            if (_voiceState.value == VoiceState.LISTENING) return@launch

            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                android.widget.Toast.makeText(context, "Chưa cấp quyền ghi âm!", android.widget.Toast.LENGTH_SHORT).show()
                playFeedbackTone(context, android.media.ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE)
                return@launch
            }

            val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            val isOffline = capabilities == null || !capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)

            _voiceState.value = VoiceState.LISTENING
            playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_BEEP)

            if (isOffline) {
                startVosk(context)
            } else {
                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
                    putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    if (isOffline) {
                        putExtra(android.speech.RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    }
                }
                try {
                    speechRecognizer?.destroy()
                    val recognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
                    speechRecognizer = recognizer

                    recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                        override fun onReadyForSpeech(params: android.os.Bundle?) {
                            _voiceState.value = VoiceState.LISTENING
                        }
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _voiceState.value = VoiceState.ANALYZING
                        }
                        override fun onError(error: Int) {
                            if (error == android.speech.SpeechRecognizer.ERROR_CLIENT || error == android.speech.SpeechRecognizer.ERROR_NETWORK) {
                                android.widget.Toast.makeText(context, "Google Voice không khả dụng, đang dùng Vosk ngoại tuyến...", android.widget.Toast.LENGTH_SHORT).show()
                                startVosk(context)
                                return
                            }
                            
                            _voiceState.value = VoiceState.IDLE
                            playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)

                            val msg = when (error) {
                                android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được giọng nói."
                                android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian chờ giọng nói."
                                else -> "Lỗi nhận diện: $error"
                            }
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                        override fun onResults(results: android.os.Bundle?) {
                            val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_ACK)
                                val text = matches[0]
                                if (currentVoiceTarget == "chat") {
                                    sendMessage(text)
                                } else {
                                    analyzeVoiceSentiment(text)
                                }
                            } else {
                                _voiceState.value = VoiceState.IDLE
                                playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
                            }
                        }
                        override fun onPartialResults(partialResults: android.os.Bundle?) {}
                        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                    })
                    recognizer.startListening(intent)
                } catch (e: Exception) {
                    _voiceState.value = VoiceState.IDLE
                    playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
                }
            }
        }
    }


    fun stopListeningProgrammatic() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                voskSpeechService?.stop()
                voskSpeechService = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVoiceMode(mode: MeasurementMode) {
        _voiceMode.value = mode
        _voiceSentiment.value = Triple(55, 30, 15)
    }

    private val _textToSpeak = MutableStateFlow<String?>(null)
    val textToSpeak = _textToSpeak.asStateFlow()

    private val _volume = MutableStateFlow(0.5f)
    val volume = _volume.asStateFlow()

    private val _isLoopingState = MutableStateFlow(AppPreferences.loopMusic)
    val isLoopingState = _isLoopingState.asStateFlow()

    private val _isAiSpeaking = MutableStateFlow(false)
    val isAiSpeaking = _isAiSpeaking.asStateFlow()

    private val _trendAlert = MutableStateFlow<TrendAlert?>(null)
    val trendAlert = _trendAlert.asStateFlow()

    fun dismissTrendAlert() {
        _trendAlert.value = null
    }

    private var wasPlayingBeforeSpeech = false

    fun toggleLoop() {
        val next = !AppPreferences.loopMusic
        AppPreferences.loopMusic = next
        _isLoopingState.value = next
        mediaPlayer?.isLooping = next
    }

    fun onSpeechStarted() {
        viewModelScope.launch {
            _isAiSpeaking.value = true
            _voiceState.value = VoiceState.RESPONDING
            if (AppPreferences.muteMusicOnSpeech) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        wasPlayingBeforeSpeech = true
                        it.pause()
                        _isPlaying.value = false
                    }
                }
            }
        }
    }

    fun onSpeechFinished() {
        viewModelScope.launch {
            _isAiSpeaking.value = false
            _voiceState.value = VoiceState.IDLE
            if (AppPreferences.muteMusicOnSpeech && wasPlayingBeforeSpeech) {
                mediaPlayer?.let {
                    try {
                        it.start()
                        _isPlaying.value = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                wasPlayingBeforeSpeech = false
            }
        }
    }

    fun resetTextToSpeak() {
        _textToSpeak.value = null
    }

    fun updateBpm(newBpm: Int) {
        _bpm.value = newBpm
    }

    fun updateSpo2(newSpo2: Int) {
        _spo2.value = newSpo2
    }

    fun syncGadgetbridgeDb(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = "Đang đồng bộ..."
            val uri = AppPreferences.gadgetbridgeDbUri
            if (uri.isBlank()) {
                _syncStatus.value = "Chưa cấu hình CSDL"
                return@launch
            }
            val result = GadgetbridgeDbParser.syncFromDatabase(context, uri)
            if (result.success) {
                result.heartRate?.let { _bpm.value = it }
                result.spo2?.let { _spo2.value = it }
                _sleepMinutes.value = result.sleepMinutes
                _deepSleepMinutes.value = result.deepSleepMinutes
                _sleepScore.value = result.sleepScore
                _steps.value = result.steps
                _activeMinutes.value = result.activeMinutes
                
                // Save to historical database diary
                if (result.heartRate != null || result.spo2 != null || result.sleepMinutes != null || result.sleepScore != null || result.steps != null || result.activeMinutes != null) {
                    val record = GadgetbridgeHealthRecord(
                        bpm = result.heartRate,
                        spo2 = result.spo2,
                        sleepMinutes = result.sleepMinutes,
                        deepSleepMinutes = result.deepSleepMinutes,
                        sleepScore = result.sleepScore,
                        steps = result.steps,
                        activeMinutes = result.activeMinutes,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertGadgetbridgeRecord(record)
                }

                val hrStr = if (result.heartRate != null) "${result.heartRate} BPM" else "N/A"
                val spo2Str = if (result.spo2 != null) "${result.spo2}%" else "N/A"
                _syncStatus.value = "Đồng bộ thành công! (Nhịp tim: $hrStr, SpO2: $spo2Str)"
                AppPreferences.gadgetbridgeLastSync = System.currentTimeMillis()
            } else {
                _syncStatus.value = result.message
            }
        }
    }

    fun clearGadgetbridgeHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearGadgetbridgeRecords()
        }
    }

    fun addHealthData(type: String, value: Float, unit: String) {
        // Here we could persist health data to Room if needed.
        // For now, it's updating current states.
    }

    init {
        loadCustomTracks()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                org.vosk.android.StorageService.unpack(getApplication<Application>(), "model-vn", "model",
                    { model -> 
                        voskModel = model 
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            android.widget.Toast.makeText(getApplication<android.app.Application>(), "Đã tải xong mô hình Vosk!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    { e -> 
                        android.util.Log.e("AuraViewModel", "Vosk model error", e)
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            android.widget.Toast.makeText(getApplication<android.app.Application>(), "Lỗi unpack Vosk: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                )
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication<android.app.Application>(), "Exception Vosk: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
viewModelScope.launch {
            try {
                val itunesRes = RetrofitClient.itunesService.searchSongs("nhạc trẻ", limit = 10)
                if (itunesRes.results != null && itunesRes.results.isNotEmpty()) {
                    val tracks = itunesRes.results.filter { it.previewUrl != null }.map {
                        MusicTrack("🎶", it.trackName, "Nhạc trẻ (iTunes API Free)", "0:30 Preview", it.previewUrl!!)
                    }
                    if (tracks.isNotEmpty()) {
        val saved = AppPreferences.customTracks
                        val currentList = _musicList.value.toMutableList()
                        currentList.addAll(tracks)
                        _musicList.value = currentList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            repository.allFavoriteTracks.collect { tracks ->
                val titles = tracks.map { it.title }.toSet()
                _favoriteTracksList.value = titles
                _isCurrentTrackFavorite.value = titles.contains(_currentTrack.value.title)
                AppPreferences.favoriteTracks = titles
            }
        }
        
        viewModelScope.launch {
            dbMessages.collect { msgs ->
                if (msgs.isNotEmpty()) {
                    _messages.value = msgs.map { ChatMessage(it.text, it.isUser.not(), timeFormat.format(Date(it.timestamp))) }
                }
            }
        }

        viewModelScope.launch {
            try {
                val sessions = repository.allChartSessions.first()
                if (sessions.isEmpty()) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val now = System.currentTimeMillis()
                    val dayMs = 24 * 60 * 60 * 1000L
                    
                    listOf(3, 2, 1, 0).forEach { dayOffset ->
                        val dateMs = now - (dayOffset * dayMs)
                        val dateStr = dateFormat.format(Date(dateMs))
                        
                        val baseStress = when (dayOffset) {
                            3 -> 22 + Random.nextInt(8)  // average ~26
                            2 -> 35 + Random.nextInt(10) // average ~40
                            1 -> 52 + Random.nextInt(8)  // average ~56
                            else -> 66 + Random.nextInt(12) // average ~72
                        }
                        
                        listOf("09:15", "13:30", "17:45", "21:00").forEachIndexed { index, timeStr ->
                            val bpmMock = 66 + Random.nextInt(26)
                            val stressMock = baseStress + Random.nextInt(5) - 2
                            val mockTimestamp = dateMs - (12 - index * 3) * 60 * 60 * 1000L
                            val mockSource = if (index % 2 == 0) "Giọng nói" else "Aura AI"
                            repository.insertChartSession(
                                ChartSession(
                                    dateStr = dateStr,
                                    timeStr = timeStr,
                                    bpm = bpmMock,
                                    stressLevel = stressMock.coerceIn(10, 99),
                                    timestamp = mockTimestamp,
                                    source = mockSource
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            while(true) {
                delay(3000)
                if (AppPreferences.blynkToken.isNotEmpty()) {
                    val (b, s) = fetchBlynkData(
                        AppPreferences.blynkToken,
                        AppPreferences.blynkPinBpm,
                        AppPreferences.blynkPinSpo2
                    )
                    b?.let { _bpm.value = it }
                    s?.let { _spo2.value = it }
                } else if (AppPreferences.gadgetbridgeDbUri.isBlank()) {
                    _bpm.value = 82 + Random.nextInt(15)
                    _spo2.value = 95 + Random.nextInt(5)
                }
                
                // Periodic biological sessions from heart rate monitor are no longer saved to Aura Diary, as Aura Diary is strictly based on Voice & Aura AI sessions
            }
        }

        viewModelScope.launch {
            allChartSessions.collect { sessions ->
                checkEmotionalTrends(sessions)
            }
        }

        viewModelScope.launch {
            while (true) {
                val intervalMs = AppPreferences.gadgetbridgeSyncIntervalMinutes * 60 * 1000L
                val timeSinceLastSync = System.currentTimeMillis() - AppPreferences.gadgetbridgeLastSync
                if (AppPreferences.gadgetbridgeDbUri.isNotBlank() && timeSinceLastSync >= intervalMs) {
                    syncGadgetbridgeDb(application)
                }
                delay(15000) // Kiểm tra mỗi 15 giây để xem đã đến lúc đồng bộ chưa
            }
        }
    }

    fun setVolume(vol: Float) {
        _volume.value = vol
        mediaPlayer?.setVolume(vol, vol)
    }

    fun playTrack(track: MusicTrack) {
        _currentTrack.value = track
        _isCurrentTrackFavorite.value = AppPreferences.favoriteTracks.contains(track.title)
        
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
        }
        mediaPlayer?.reset()
        try {
            val cm = getApplication<android.app.Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val net = cm.activeNetwork
            val caps = if (net != null) cm.getNetworkCapabilities(net) else null
            val isOffline = caps == null || !caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            var urlToPlay = track.url
            if (isOffline && !track.url.startsWith("content://") && !track.url.startsWith("android.resource://")) {
                urlToPlay = "android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"
                android.widget.Toast.makeText(getApplication<android.app.Application>(), "Đang ngoại tuyến, phát nhạc offline dự phòng...", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            if (urlToPlay.startsWith("content://") || urlToPlay.startsWith("android.resource://")) {
                mediaPlayer?.setDataSource(getApplication(), android.net.Uri.parse(urlToPlay))
            } else {
                mediaPlayer?.setDataSource(urlToPlay)
            }
            mediaPlayer?.setVolume(_volume.value, _volume.value)
            mediaPlayer?.isLooping = AppPreferences.loopMusic
            
            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                _isPlaying.value = false
                android.util.Log.e("MediaPlayer", "Error: $what, $extra")
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication<android.app.Application>(), "Không thể phát, đang phát offline dự phòng", android.widget.Toast.LENGTH_SHORT).show()
                }
                try {
                    mp.reset()
                    mp.setOnPreparedListener { 
                        it.start() 
                        _isPlaying.value = true
                    }
                    mp.setOnCompletionListener {
                        _isPlaying.value = false
                    }
                    mp.setDataSource(getApplication(), android.net.Uri.parse("android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"))
                    mp.prepareAsync()
                } catch(e: Exception) {}
                true
            }
            
            _isPlaying.value = false // waiting to buffer
            mediaPlayer?.setOnPreparedListener {
                it.start()
                _isPlaying.value = true
            }
            mediaPlayer?.setOnCompletionListener {
                _isPlaying.value = false
            }
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MediaPlayer", "setDataSource exception: " + e.message)
            _isPlaying.value = false
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setOnPreparedListener { 
                    it.start() 
                    _isPlaying.value = true
                }
                mediaPlayer?.setOnCompletionListener {
                    _isPlaying.value = false
                }
                mediaPlayer?.setDataSource(getApplication(), android.net.Uri.parse("android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"))
                mediaPlayer?.prepareAsync()
            } catch(e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun togglePlay() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
            }
        } ?: run {
            playTrack(_currentTrack.value)
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                it.seekTo(0)
            }
        }
        _isPlaying.value = false
        _textToSpeak.value = null
    }

    fun replayCurrentTrack() {
        mediaPlayer?.let {
            try {
                it.seekTo(0)
                if (!it.isPlaying) {
                    it.start()
                    _isPlaying.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } ?: run {
            playTrack(_currentTrack.value)
        }
    }

    fun playNextTrack() {
        val list = _musicList.value
        val currentIndex = list.indexOfFirst { it.title == _currentTrack.value.title }
        if (currentIndex != -1) {
            val nextIndex = (currentIndex + 1) % list.size
            playTrack(list[nextIndex])
        }
    }

    fun playPreviousTrack() {
        val list = _musicList.value
        val currentIndex = list.indexOfFirst { it.title == _currentTrack.value.title }
        if (currentIndex != -1) {
            val prevIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
            playTrack(list[prevIndex])
        }
    }

    fun isNetworkOffline(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (cm == null) return true
            val net = cm.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(net) ?: return true
            !caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true
        }
    }

    fun playMusicForMode(mode: MeasurementMode) {
        val isOfflineSession = isNetworkOffline() || AppPreferences.geminiKey.isEmpty() || AppPreferences.geminiKey == "MY_GEMINI_API_KEY"
        val candidateTracks = _musicList.value.filter { 
            it.desc.contains(mode.title, ignoreCase = true) || it.desc.contains("Tất cả", ignoreCase = true)
        }
        
        if (isOfflineSession) {
            val offlineCandidates = candidateTracks.filter { it.url.startsWith("content://") || it.url.startsWith("android.resource://") }
            if (offlineCandidates.isNotEmpty()) {
                playTrack(offlineCandidates.random())
            } else {
                val defaultOffline = _musicList.value.filter { it.url.contains("offline_relax") }
                if (defaultOffline.isNotEmpty()) {
                    playTrack(defaultOffline.first())
                }
            }
            return
        }

        if (candidateTracks.isNotEmpty()) {
            playTrack(candidateTracks.random())
        } else {
            playRandomFavoriteOrHealing()
        }
    }

    fun playRandomFavoriteOrHealing() {
        val favTracks = _favoriteTracksList.value
        val candidateTracks = if (favTracks.isNotEmpty()) {
            _musicList.value.filter { favTracks.contains(it.title) }
        } else {
            _musicList.value
        }
        if (candidateTracks.isNotEmpty()) {
            val randomTrack = candidateTracks.random()
            playTrack(randomTrack)
        }
    }

    fun loadCustomTracks() {
        val realPackageName = getApplication<Application>().packageName
        val updatedDefaultList = _musicList.value.map { track ->
            if (track.url.contains("raw/offline_relax") && !track.url.contains(realPackageName)) {
                track.copy(url = "android.resource://$realPackageName/raw/offline_relax")
            } else {
                track
            }
        }
        val saved = AppPreferences.customTracks
        val currentList = updatedDefaultList.toMutableList()
        saved.forEach { str ->
            val parts = str.split("|")
            if (parts.size >= 3) {
                val title = parts[0]
                val desc = parts[1]
                val url = parts[2]
                val emoji = if (parts.size >= 4) parts[3] else "🎵"
                val time = if (parts.size >= 5) parts[4] else "Local"
                if (currentList.none { it.title == title }) {
                    currentList.add(MusicTrack(emoji, title, desc, time, url))
                }
            }
        }
        _musicList.value = currentList
    }

    fun addCustomMusicTrack(title: String, desc: String, uri: String) {
        val emoji = "🎵"
        val time = "Local"
        val newTrack = MusicTrack(emoji, title, desc, time, uri)
        _musicList.update { it + newTrack }
        
        val saved = AppPreferences.customTracks.toMutableSet()
        saved.add("$title|$desc|$uri|$emoji|$time")
        AppPreferences.customTracks = saved
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun toggleFavoriteCurrentTrack() {
        val track = _currentTrack.value
        val currentFavs = _favoriteTracksList.value
        viewModelScope.launch {
            if (currentFavs.contains(track.title)) {
                repository.deleteFavoriteTrack(track.title)
            } else {
                repository.insertFavoriteTrack(track.title)
            }
        }
    }

    fun toggleFavoriteList(track: MusicTrack) {
        val currentFavs = _favoriteTracksList.value
        viewModelScope.launch {
            if (currentFavs.contains(track.title)) {
                repository.deleteFavoriteTrack(track.title)
            } else {
                repository.insertFavoriteTrack(track.title)
            }
        }
    }

    fun analyzeVoiceSentiment(text: String) {
        viewModelScope.launch {
            _voiceState.value = VoiceState.ANALYZING
            val mode = _voiceMode.value
            
            var sentiment = if (AppPreferences.geminiKey.isEmpty() || AppPreferences.geminiKey == "MY_GEMINI_API_KEY") {
                fetchVoiceSentimentOffline(text, mode.key)
            } else {
                fetchVoiceSentiment(text, mode.key)
            }
            
            // Fallback if API fails (returns default 33,33,34)
            if (sentiment == Triple(33, 33, 34)) {
                sentiment = fetchVoiceSentimentOffline(text, mode.key)
            }
            
            _voiceSentiment.value = sentiment
            
            // Log a measurement session in the database
            try {
                val currDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                val currTime = timeFormat.format(Date())
                repository.insertChartSession(
                    ChartSession(
                        dateStr = currDate,
                        timeStr = currTime,
                        bpm = _bpm.value,
                        stressLevel = sentiment.second,
                        source = "Giọng nói (${mode.title})"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Nếu phát hiện buồn, căng thẳng cao thì tự động mở nhạc thư giãn phù hợp
            // Trong chế độ offline, tỷ lệ căng thẳng ngẫu nhiên thường từ 30% trở lên, nên giảm ngưỡng để dễ phát nhạc
            if (sentiment.second >= 20 || AppPreferences.geminiKey.isEmpty() || AppPreferences.geminiKey == "MY_GEMINI_API_KEY") {
                playMusicForMode(mode)
            }
            
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            repository.insertChatMessage(DbChatMessage(text = text, isUser = true))
        }
        _isTyping.value = true
        
        viewModelScope.launch {
            var replyText = generateGeminiReply(text, _bpm.value, _spo2.value)
            
            if (replyText.contains("[PLAY_FAV]")) {
                replyText = replyText.replace("[PLAY_FAV]", "").trim()
                playRandomFavoriteOrHealing()
            }
            
            _isTyping.value = false
            repository.insertChatMessage(DbChatMessage(text = replyText, isUser = false))
            _textToSpeak.value = replyText
        }
    }

    private fun checkEmotionalTrends(sessions: List<ChartSession>) {
        if (sessions.isEmpty()) return
        
        // Group by dateStr and compute daily average stress levels
        val sortedDailyAverages = sessions
            .groupBy { it.dateStr }
            .mapValues { (_, daySessions) ->
                val avgStress = daySessions.map { it.stressLevel }.average()
                val avgTimestamp = daySessions.map { it.timestamp }.average().toLong()
                avgStress to avgTimestamp
            }
            .entries
            .sortedBy { it.value.second } // sort chronologically by timestamp
            .map { entry ->
                Triple(entry.key, entry.value.first, entry.value.second)
            } // List of Triple(dateStr, averageStress, averageTimestamp)

        // We need at least 3 days to detect a 3-day consecutive trend
        if (sortedDailyAverages.size < 3) return
        
        // Let's analyze the last 3 days
        val lastThree = sortedDailyAverages.takeLast(3)
        val d1 = lastThree[0] // oldest day of the 3 (e.g. 2 days ago)
        val d2 = lastThree[1] // middle day
        val d3 = lastThree[2] // latest day (e.g. today)
        
        val stress1 = d1.second
        val stress2 = d2.second
        val stress3 = d3.second
        
        var trendDetected = false
        var trendType = ""
        var alertMessage = ""
        
        if (stress3 > stress2 && stress2 > stress1 && stress3 > 40.0) {
            // Strictly increasing trend
            trendDetected = true
            trendType = "increasing"
            alertMessage = "AURA phát hiện chỉ số căng thẳng tinh thần của bạn có xu hướng TĂNG LIÊN TỤC trong 3 ngày qua (từ ${stress1.toInt()}% lên ${stress3.toInt()}%). Thư giãn cùng nhạc Healing nhé!"
        } else if (stress3 >= 50.0 && stress2 >= 50.0 && stress1 >= 50.0) {
            // Sustained high stress/negativity
            trendDetected = true
            trendType = "sustained_high"
            alertMessage = "AURA nhận thấy mức độ căng thẳng của bạn duy trì ở mức CAO liên tục trong 3 ngày qua (trung bình trên ${stress3.toInt()}%). Đã đến lúc xoa dịu tâm trí!"
        }
        
        if (trendDetected) {
            val alert = TrendAlert(
                message = alertMessage,
                trendType = trendType,
                dates = lastThree.map { it.first },
                values = lastThree.map { it.second }
            )
            _trendAlert.value = alert
            
            // Check if we should fire a system notification (once per trend-end date to avoid duplicate notifications on same day)
            val latestDate = d3.first
            if (AppPreferences.lastNotifiedTrendDate != latestDate) {
                AppPreferences.lastNotifiedTrendDate = latestDate
                triggerTrendNotification(alertMessage)
                
                // Send to Webhook if configured
                val webhookUrl = AppPreferences.webhookUrl
                if (webhookUrl.isNotBlank()) {
                    sendAlertToWebhook(webhookUrl, alert)
                }
            }
        } else {
            _trendAlert.value = null
        }
    }

    private fun sendAlertToWebhook(url: String, alert: TrendAlert) {
        viewModelScope.launch {
            GoogleSheetsService.pushAlertToSheet(
                webhookUrl = url,
                type = "EMERGENCY_ALERT",
                message = alert.message,
                value = "Stress: ${alert.values.joinToString(" -> ")} %",
                details = "Xu hướng: ${alert.trendType} | Ngày: ${alert.dates.joinToString(" -> ")}"
            )
        }
    }

    private fun triggerTrendNotification(message: String) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Application.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "aura_trend_channel"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "AURA Cảnh báo tâm trạng",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh thông báo cảnh báo xu hướng tâm trạng tiêu cực"
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Cảnh báo Cảm xúc AURA ⚠️")
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        notificationManager.notify(2002, builder.build())
    }

    fun generateAndSaveDailyReport(dateStr: String, sessions: List<ChartSession>) {
        viewModelScope.launch {
            if (sessions.isEmpty()) return@launch
            _dailyReportLoadingMap.update { it + (dateStr to true) }
            try {
                val avgStress = sessions.map { it.stressLevel }.average().toInt()
                val avgBpm = sessions.map { it.bpm }.average().toInt()
                
                val sessionSummaries = sessions.joinToString("\n") { s ->
                    "- Lúc ${s.timeStr}: Nhịp tim ${s.bpm} bpm, Căng thẳng ${s.stressLevel}% (${s.source})"
                }
                
                val aiAssessment = generateAuraDailySummary(dateStr, avgStress, avgBpm, sessionSummaries)
                
                val report = DailyReport(
                    dateStr = dateStr,
                    avgStress = avgStress,
                    avgBpm = avgBpm,
                    aiAssessment = aiAssessment,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertDailyReport(report)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _dailyReportLoadingMap.update { it - dateStr }
            }
        }
    }
}
