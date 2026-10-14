import re

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
i = 0
while i < len(lines):
    if "stopListeningProgrammatic()" in lines[i] and i + 1 < len(lines) and "return" in lines[i+1]:
        # Skip this line
        i += 1
        continue
    new_lines.append(lines[i])
    i += 1

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.writelines(new_lines)
