import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    content = f.read()

# find the vosk onResult
pattern = r'(analyzeVoiceSentiment\(text\)\s*\}\s*)(return)'
replacement = r'\1voskSpeechService?.stop(); voskSpeechService = null; \2'

# We only want to replace the FIRST occurrence (in Vosk), not the second (in Google API)
# Actually the Google API doesn't have a `return` right after it!
content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.write(content)
