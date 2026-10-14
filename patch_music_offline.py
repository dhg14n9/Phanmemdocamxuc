import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''    fun playMusicForMode\(mode: MeasurementMode\) \{
        val candidateTracks = _musicList.value.filter \{ it.desc.contains\(mode.title, ignoreCase = true\) \}
        if \(candidateTracks.isNotEmpty\(\)\) \{
            playTrack\(candidateTracks.random\(\)\)
        \} else \{
            playRandomFavoriteOrHealing\(\)
        \}
    \}'''

replacement = r'''    fun playMusicForMode(mode: MeasurementMode) {
        val isOfflineSession = AppPreferences.geminiKey.isEmpty() || AppPreferences.geminiKey == "MY_GEMINI_API_KEY"
        val candidateTracks = _musicList.value.filter { it.desc.contains(mode.title, ignoreCase = true) }
        
        if (isOfflineSession) {
            val offlineCandidates = candidateTracks.filter { it.url.startsWith("content://") || it.url.startsWith("android.resource://") }
            if (offlineCandidates.isNotEmpty()) {
                playTrack(offlineCandidates.random())
            } else {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication<android.app.Application>(), "Không có nhạc MP3 offline danh mục ${mode.title}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            return
        }

        if (candidateTracks.isNotEmpty()) {
            playTrack(candidateTracks.random())
        } else {
            playRandomFavoriteOrHealing()
        }
    }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
