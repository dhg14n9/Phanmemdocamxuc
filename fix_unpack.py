with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "r") as f:
    lines = f.readlines()

out = []
skip = 0
for line in lines:
    if "fun loadCustomTracks()" in line:
        out.append(line)
        skip = 5
        continue
    
    if skip > 0:
        skip -= 1
        continue
        
    out.append(line)

with open("app/src/main/java/com/example/ui/AuraViewModel.kt", "w") as f:
    f.writelines(out)
