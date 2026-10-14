import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''    fun playTrack\(track: MusicTrack\) \{
        _currentTrack.value = track
        _isCurrentTrackFavorite.value = AppPreferences.favoriteTracks.contains\(track.title\)
        
        if \(mediaPlayer == null\) \{
            mediaPlayer = MediaPlayer\(\).apply \{
                setAudioAttributes\(
                    AudioAttributes.Builder\(\)
                        .setContentType\(AudioAttributes.CONTENT_TYPE_MUSIC\)
                        .setUsage\(AudioAttributes.USAGE_MEDIA\)
                        .build\(\)
                \)
            \}
        \}
        mediaPlayer\?\.reset\(\)
        try \{
            if \(track.url.startsWith\("content://"\)\) \{
                mediaPlayer\?\.setDataSource\(getApplication\(\), android.net.Uri.parse\(track.url\)\)
            \} else \{
                mediaPlayer\?\.setDataSource\(track.url\)
            \}
            mediaPlayer\?\.setVolume\(_volume.value, _volume.value\)
            mediaPlayer\?\.isLooping = AppPreferences.loopMusic
            mediaPlayer\?\.prepareAsync\(\)
            _isPlaying.value = false // waiting to buffer
            mediaPlayer\?\.setOnPreparedListener \{
                it.start\(\)
                _isPlaying.value = true
            \}
            mediaPlayer\?\.setOnCompletionListener \{
                _isPlaying.value = false
                if \(!AppPreferences.loopMusic\) \{
                    playNextTrack\(\)
                \}
            \}
        \} catch \(e: Exception\) \{
            e.printStackTrace\(\)
            _isPlaying.value = false
        \}
    \}'''

replacement = r'''    fun playTrack(track: MusicTrack) {
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
            val isOffline = cm.activeNetwork == null || !cm.getNetworkCapabilities(cm.activeNetwork)!!.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            var urlToPlay = track.url
            if (isOffline && !track.url.startsWith("content://") && !track.url.startsWith("android.resource://")) {
                // Fallback to offline ambient track if we are offline and trying to play a remote URL
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
                    android.widget.Toast.makeText(getApplication<android.app.Application>(), "Không thể phát bài hát, đang phát offline dự phòng", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                // Cố gắng phát lại với offline_relax nếu lỗi
                try {
                    mp.reset()
                    mp.setDataSource(getApplication(), android.net.Uri.parse("android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"))
                    mp.prepareAsync()
                } catch(e: Exception) {}
                
                true
            }
            
            mediaPlayer?.prepareAsync()
            _isPlaying.value = false // waiting to buffer
            mediaPlayer?.setOnPreparedListener {
                it.start()
                _isPlaying.value = true
            }
            mediaPlayer?.setOnCompletionListener {
                _isPlaying.value = false
                if (!AppPreferences.loopMusic) {
                    playNextTrack()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
