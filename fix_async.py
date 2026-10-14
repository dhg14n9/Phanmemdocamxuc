import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''        viewModelScope.launch\(Dispatchers.IO\) \{
            try \{
                org.vosk.android.StorageService.unpack\(getApplication<Application>\(\), "model-vn", "model",
                    \{ model -> 
                        voskModel = model 
                        android.os.Handler\(android.os.Looper.getMainLooper\(\)\).post \{
                            android.widget.Toast.makeText\(getApplication<android.app.Application>\(\), "Đã tải xong mô hình Vosk!", android.widget.Toast.LENGTH_SHORT\).show\(\)
                        \}
                    \},
                    \{ e -> 
                        android.util.Log.e\("AuraViewModel", "Vosk model error", e\)
                        android.os.Handler\(android.os.Looper.getMainLooper\(\)\).post \{
                            android.widget.Toast.makeText\(getApplication<android.app.Application>\(\), "Lỗi unpack Vosk: \$\{e.message\}", android.widget.Toast.LENGTH_LONG\).show\(\)
                        \}
                    \}
                \)
            \} catch \(e: Exception\) \{
                android.os.Handler\(android.os.Looper.getMainLooper\(\)\).post \{
                    android.widget.Toast.makeText\(getApplication<android.app.Application>\(\), "Exception Vosk: \$\{e.message\}", android.widget.Toast.LENGTH_LONG\).show\(\)
                \}
            \}
        \}'''

replacement = r'''        try {
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
        }'''

content = content.replace(pattern, replacement)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
