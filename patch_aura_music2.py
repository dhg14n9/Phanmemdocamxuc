import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'''            // Nếu phát hiện buồn, căng thẳng cao \(chỉ số căng thẳng > 40%\) thì tự động mở nhạc thư giãn phù hợp
            // Hoặc nếu đang offline, cũng mở nhạc luôn để đảm bảo trải nghiệm
            if \(sentiment\.second > 30 \|\| AppPreferences\.geminiKey\.isEmpty\(\) \|\| AppPreferences\.geminiKey == "MY_GEMINI_API_KEY"\) \{
                playMusicForMode\(mode\)
            \}'''

replacement = r'''            // Nếu phát hiện buồn, căng thẳng cao thì tự động mở nhạc thư giãn phù hợp
            // Trong chế độ offline, tỷ lệ căng thẳng ngẫu nhiên thường từ 30% trở lên, nên giảm ngưỡng để dễ phát nhạc
            if (sentiment.second >= 20 || AppPreferences.geminiKey.isEmpty() || AppPreferences.geminiKey == "MY_GEMINI_API_KEY") {
                playMusicForMode(mode)
            }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
