import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''            if \(urlToPlay\.startsWith\("content://"\) \|\| urlToPlay\.startsWith\("android\.resource://"\)\) \{
                mediaPlayer\?\.setDataSource\(getApplication\(\), android\.net\.Uri\.parse\(urlToPlay\)\)
            \} else \{
                mediaPlayer\?\.setDataSource\(urlToPlay\)
            \}'''

replacement = r'''            if (urlToPlay.startsWith("content://")) {
                val uri = android.net.Uri.parse(urlToPlay)
                val pfd = getApplication<android.app.Application>().contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    mediaPlayer?.setDataSource(pfd.fileDescriptor)
                    pfd.close()
                } else {
                    mediaPlayer?.setDataSource(getApplication(), uri)
                }
            } else if (urlToPlay.startsWith("android.resource://")) {
                mediaPlayer?.setDataSource(getApplication(), android.net.Uri.parse(urlToPlay))
            } else {
                mediaPlayer?.setDataSource(urlToPlay)
            }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
