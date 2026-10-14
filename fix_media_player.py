import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''        mediaPlayer\?\.reset\(\)
        try \{
            val cm = getApplication<android\.app\.Application>\(\)\.getSystemService\(android\.content\.Context\.CONNECTIVITY_SERVICE\) as android\.net\.ConnectivityManager
            val isOffline = cm\.activeNetwork == null \|\| !cm\.getNetworkCapabilities\(cm\.activeNetwork\)!!\.hasCapability\(android\.net\.NetworkCapabilities\.NET_CAPABILITY_INTERNET\)
            
            var urlToPlay = track\.url
            if \(isOffline && !track\.url\.startsWith\("content://"\) && !track\.url\.startsWith\("android\.resource://"\)\) \{
                urlToPlay = "android\.resource://" \+ getApplication<android\.app\.Application>\(\)\.packageName \+ "/raw/offline_relax"
                android\.widget\.Toast\.makeText\(getApplication<android\.app\.Application>\(\), "Đang ngoại tuyến, phát nhạc offline dự phòng\.\.\.", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\)
            \}
            
            if \(urlToPlay\.startsWith\("content://"\) \|\| urlToPlay\.startsWith\("android\.resource://"\)\) \{
                mediaPlayer\?\.setDataSource\(getApplication\(\), android\.net\.Uri\.parse\(urlToPlay\)\)
            \} else \{
                mediaPlayer\?\.setDataSource\(urlToPlay\)
            \}
            mediaPlayer\?\.setVolume\(_volume\.value, _volume\.value\)
            mediaPlayer\?\.isLooping = AppPreferences\.loopMusic
            
            mediaPlayer\?\.setOnErrorListener \{ mp, what, extra ->
                _isPlaying\.value = false
                android\.util\.Log\.e\("MediaPlayer", "Error: $what, $extra"\)
                android\.os\.Handler\(android\.os\.Looper\.getMainLooper\(\)\)\.post \{
                    android\.widget\.Toast\.makeText\(getApplication<android\.app\.Application>\(\), "Không thể phát, đang phát offline dự phòng", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\)
                \}
                try \{
                    mp\.reset\(\)
                    mp\.setDataSource\(getApplication\(\), android\.net\.Uri\.parse\("android\.resource://" \+ getApplication<android\.app\.Application>\(\)\.packageName \+ "/raw/offline_relax"\)\)
                    mp\.prepareAsync\(\)
                \} catch\(e: Exception\) \{\}
                true
            \}
            
            _isPlaying\.value = false // waiting to buffer
            mediaPlayer\?\.setOnPreparedListener \{
                it\.start\(\)
                _isPlaying\.value = true
            \}
            mediaPlayer\?\.setOnCompletionListener \{
                _isPlaying\.value = false
            \}
            mediaPlayer\?\.prepareAsync\(\)
        \} catch \(e: Exception\) \{
            e\.printStackTrace\(\)
            android\.util\.Log\.e\("MediaPlayer", "setDataSource exception: " \+ e\.message\)
            _isPlaying\.value = false
            try \{
                mediaPlayer\?\.reset\(\)
                mediaPlayer\?\.setDataSource\(getApplication\(\), android\.net\.Uri\.parse\("android\.resource://" \+ getApplication<android\.app\.Application>\(\)\.packageName \+ "/raw/offline_relax"\)\)
                mediaPlayer\?\.prepareAsync\(\)
            \} catch\(e2: Exception\) \{
                e2\.printStackTrace\(\)
            \}
        \}'''

replacement = r'''        mediaPlayer?.reset()
        
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
                mp.setDataSource(getApplication(), android.net.Uri.parse("android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"))
                mp.prepareAsync()
            } catch(e: Exception) {}
            true
        }

        try {
            val cm = getApplication<android.app.Application>().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val isOffline = cm.activeNetwork == null || !cm.getNetworkCapabilities(cm.activeNetwork)!!.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            
            var urlToPlay = track.url
            if (isOffline && !track.url.startsWith("content://") && !track.url.startsWith("android.resource://")) {
                urlToPlay = "android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication<android.app.Application>(), "Đang ngoại tuyến, phát nhạc offline dự phòng...", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
            if (urlToPlay.startsWith("content://") || urlToPlay.startsWith("android.resource://")) {
                mediaPlayer?.setDataSource(getApplication(), android.net.Uri.parse(urlToPlay))
            } else {
                mediaPlayer?.setDataSource(urlToPlay)
            }
            mediaPlayer?.setVolume(_volume.value, _volume.value)
            mediaPlayer?.isLooping = AppPreferences.loopMusic
            
            _isPlaying.value = false // waiting to buffer
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
                mediaPlayer?.setDataSource(getApplication(), android.net.Uri.parse("android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"))
                mediaPlayer?.prepareAsync()
            } catch(e2: Exception) {
                e2.printStackTrace()
            }
        }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
