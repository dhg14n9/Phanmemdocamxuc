import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'            if \(isOffline && voskModel != null\) \{'
replacement = r'''            if (isOffline) {
                if (voskModel == null) {
                    android.widget.Toast.makeText(context, "Đang tải dữ liệu nhận diện ngoại tuyến. Vui lòng thử lại sau vài giây...", android.widget.Toast.LENGTH_SHORT).show()
                    _voiceState.value = VoiceState.IDLE
                    playFeedbackTone(context, android.media.ToneGenerator.TONE_PROP_NACK)
                    return@launch
                }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
