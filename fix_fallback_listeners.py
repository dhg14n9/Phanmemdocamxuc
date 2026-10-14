import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern1 = r'''                try \{
                    mp\.reset\(\)
                    mp\.setDataSource\(getApplication\(\), android\.net\.Uri\.parse\("android\.resource://" \+ getApplication<android\.app\.Application>\(\)\.packageName \+ "/raw/offline_relax"\)\)
                    mp\.prepareAsync\(\)
                \} catch\(e: Exception\) \{\}'''

replacement1 = r'''                try {
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
                } catch(e: Exception) {}'''

pattern2 = r'''            try \{
                mediaPlayer\?\.reset\(\)
                mediaPlayer\?\.setDataSource\(getApplication\(\), android\.net\.Uri\.parse\("android\.resource://" \+ getApplication<android\.app\.Application>\(\)\.packageName \+ "/raw/offline_relax"\)\)
                mediaPlayer\?\.prepareAsync\(\)
            \} catch\(e2: Exception\) \{
                e2\.printStackTrace\(\)
            \}'''

replacement2 = r'''            try {
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
            }'''

content = re.sub(pattern1, replacement1, content, count=1)
content = re.sub(pattern2, replacement2, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
