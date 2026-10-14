import re

with open("app/src/main/java/com/example/ui/MusicScreen.kt", "r") as f:
    content = f.read()

pattern = r'''                    Spacer\(modifier = Modifier\.height\(8\.dp\)\)
                    OutlinedTextField\(
                        value = newMusicDesc,
                        onValueChange = \{ newMusicDesc = it \},
                        label = \{ Text\("Thể loại \(Ví dụ: Healing\)", color = Text2\) \},
                        modifier = Modifier\.fillMaxWidth\(\),
                        colors = androidx\.compose\.material3\.OutlinedTextFieldDefaults\.colors\(
                            focusedTextColor = Color\.White,
                            unfocusedTextColor = Color\.White,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = GlassBorder
                        \)
                    \)'''

replacement = r'''                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Danh mục Healing", color = Text2, style = Typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val cats = listOf("Bình thường", "Stress", "Lo âu", "ADHD", "Giấc ngủ")
                        items(cats.size) { i ->
                            val cat = cats[i]
                            CatChip(cat, cat, newMusicDesc == cat) { newMusicDesc = cat }
                        }
                    }'''

content = re.sub(pattern, replacement, content, count=1)

with open("app/src/main/java/com/example/ui/MusicScreen.kt", "w") as f:
    f.write(content)
