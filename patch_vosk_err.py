import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'            \{ e -> android.util.Log.e\("AuraViewModel", "Vosk model error", e\) \}'
replacement = r'''            { e -> 
                android.util.Log.e("AuraViewModel", "Vosk model error", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication<android.app.Application>(), "Lỗi tải mô hình Vosk: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
