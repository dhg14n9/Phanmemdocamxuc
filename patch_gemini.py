import re

with open("app/src/main/java/com/example/ui/GeminiApiService.kt", "r") as f:
    content = f.read()

content = content.replace('if (e.code() == 503) {\n            "Máy chủ AI hiện đang quá tải (Lỗi 503). Vui lòng thử lại sau giây lát."\n        } else {',
'''if (e.code() == 503) {
            "Máy chủ AI hiện đang quá tải (Lỗi 503). Vui lòng thử lại sau giây lát."
        } else if (e.code() == 429) {
            "Mã lỗi 429: Google AI đã vượt quá giới hạn truy cập. Vui lòng thử lại sau."
        } else {''')

with open("app/src/main/java/com/example/ui/GeminiApiService.kt", "w") as f:
    f.write(content)
