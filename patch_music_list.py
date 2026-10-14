import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

pattern = r'        MusicTrack\("🎧", "Lofi Vietnamese Collection", "Bình thường, Stress", "18:20", "https://www\.soundhelix\.com/examples/mp3/SoundHelix-Song-11\.mp3"\),'

replacement = r'''        MusicTrack("🎧", "Lofi Vietnamese Collection", "Bình thường, Stress", "18:20", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3"),
        MusicTrack("🎵", "Offline Relaxing Ambient", "Tất cả", "0:30", "android.resource://com.aistudio.aura.xxyyzz/raw/offline_relax"),'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
