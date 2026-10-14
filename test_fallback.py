import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''        \} catch \(e: Exception\) \{
            e\.printStackTrace\(\)
            _isPlaying\.value = false
        \}'''

replacement = r'''        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MediaPlayer", "setDataSource exception: " + e.message)
            _isPlaying.value = false
            try {
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(getApplication(), android.net.Uri.parse("android.resource://" + getApplication<android.app.Application>().packageName + "/raw/offline_relax"))
                mediaPlayer?.prepareAsync()
            } catch(e2: Exception) {
                e2.printStackTrace()
            }
        }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
