import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

start_func = """    fun startListeningProgrammatic(context: Context) {
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

            if (isOffline && voskModel != null) {
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
                                        return
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
                            _voiceState.value = VoiceState.IDLE
                            playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)

                            val msg = when (error) {
                                android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được giọng nói."
                                android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian chờ giọng nói."
                                android.speech.SpeechRecognizer.ERROR_NETWORK -> "Lỗi mạng. Vui lòng kiểm tra kết nối."
                                android.speech.SpeechRecognizer.ERROR_CLIENT -> "Lỗi thiết bị (Lỗi 5). Dịch vụ không khả dụng."
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
    }"""

pattern = re.compile(r'    fun startListeningProgrammatic\(context: Context\) \{.*?\n    \}', re.DOTALL)
content = re.sub(pattern, start_func, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
