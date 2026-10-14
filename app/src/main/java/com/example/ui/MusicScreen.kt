package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.TopBar
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun MusicScreen(navController: NavController, vm: AuraViewModel = viewModel()) {
    val isPlaying by vm.isPlaying.collectAsState()
    val currentTrack by vm.currentTrack.collectAsState()
    val isFavorite by vm.isCurrentTrackFavorite.collectAsState()
    val volume by vm.volume.collectAsState()
    val listFavoriteTracks by vm.favoriteTracksList.collectAsState()
    val allMusicList by vm.musicList.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("Bình thường") }
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var newMusicTitle by remember { mutableStateOf("") }
    var newMusicDesc by remember { mutableStateOf("Bình thường") }
    var newMusicUri by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            newMusicUri = it.toString()
            showAddDialog = true
        }
    }

    if (showAddDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Thêm MP3 Cá Nhân", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newMusicTitle,
                        onValueChange = { newMusicTitle = it },
                        label = { Text("Tên bài hát", color = Text2) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = GlassBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Danh mục Healing", color = Text2, style = Typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val cats = listOf("Bình thường", "Stress", "Lo âu", "ADHD", "Giấc ngủ")
                        items(cats.size) { i ->
                            val cat = cats[i]
                            CatChip(cat, cat, newMusicDesc == cat) { newMusicDesc = cat }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (newMusicTitle.isNotBlank()) {
                            vm.addCustomMusicTrack(newMusicTitle, newMusicDesc, newMusicUri)
                        }
                        showAddDialog = false
                        newMusicTitle = ""
                        newMusicUri = ""
                    }
                ) {
                    Text("Lưu", color = CyanNeon)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showAddDialog = false }
                ) {
                    Text("Hủy", color = Text3)
                }
            },
            containerColor = Bg2
        )
    }
    
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            TopBar("Healing", "Music", "Mood-Sync")
        }
        item {
            PlayerHero(isPlaying, currentTrack, isFavorite, volume, { vm.togglePlay() }, { vm.toggleFavoriteCurrentTrack() }, { vm.setVolume(it) }, vm)
        }
        item {
            MoodCategories(selectedCategory) { selectedCategory = it }
        }
        val favoriteList = allMusicList.filter { listFavoriteTracks.contains(it.title) }
        val filteredOtherList = allMusicList.filter { !listFavoriteTracks.contains(it.title) && it.desc.contains(selectedCategory, ignoreCase = true) }
        
        if (favoriteList.isNotEmpty()) {
            item {
                Text("Nhạc Yêu Thích (${favoriteList.size})", style = Typography.labelLarge.copy(color = PinkNeon), modifier = Modifier.padding(start = 16.dp, top = 16.dp))
            }
            item {
                Playlist(favoriteList, listFavoriteTracks, { vm.toggleFavoriteList(it) }) { track -> vm.playTrack(track) }
            }
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gợi Ý: $selectedCategory", style = Typography.labelLarge.copy(color = Text2))
                Box(
                    modifier = Modifier.clickable { filePickerLauncher.launch(arrayOf("audio/*")) }
                        .background(GlassHi, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("➕ Thêm MP3", style = Typography.labelSmall.copy(color = CyanNeon))
                }
            }
        }

        item {
            Playlist(filteredOtherList, listFavoriteTracks, { vm.toggleFavoriteList(it) }) { track -> vm.playTrack(track) }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun PlayerHero(isPlaying: Boolean, track: MusicTrack, isFavorite: Boolean, volume: Float, onTogglePlay: () -> Unit, onToggleFavorite: () -> Unit, onVolumeChange: (Float) -> Unit, vm: AuraViewModel) {
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column {
            Text(track.title, style = Typography.titleLarge.copy(color = Color.White))
            Text("${track.desc} · 432 Hz", style = Typography.bodySmall.copy(color = Text2))
            
            Spacer(modifier = Modifier.height(24.dp))
            // Animated visualizer
            Row(modifier = Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                for(i in 0..20) {
                    val phase = i * 0.35f
                    val sinValue = if (isPlaying) (sin(phase + time) * 0.3f + sin(phase * 2 - time * 0.5f) * 0.2f + 0.5f) else 0.1f
                    val h = (sinValue * 50 + 10).dp
                    val bg = if (isPlaying) Brush.verticalGradient(listOf(PinkNeon, PurpleNeon)) else androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha=0.2f))
                    Box(modifier = Modifier.width(4.dp).height(h).background(bg, RoundedCornerShape(2.dp)))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                 Text("🔈", fontSize = 16.sp, color = Text2)
                 androidx.compose.material3.Slider(
                     value = volume, 
                     onValueChange = { onVolumeChange(it) }, 
                     valueRange = 0f..1f,
                     modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                     colors = androidx.compose.material3.SliderDefaults.colors(
                         thumbColor = CyanNeon,
                         activeTrackColor = CyanNeon.copy(alpha = 0.5f)
                     )
                 )
                 Text("🔊", fontSize = 16.sp, color = Text2)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val heartColor = if (isFavorite) PinkNeon else Text2
                Text("♥", fontSize = 24.sp, color = heartColor, modifier = Modifier.clickable { onToggleFavorite() })
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⏮", fontSize = 22.sp, color = Text2, modifier = Modifier.clickable { vm.playPreviousTrack() })
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { onTogglePlay() }
                            .background(if (isPlaying) Brush.linearGradient(listOf(PinkNeon, PurpleNeon)) else androidx.compose.ui.graphics.SolidColor(Glass), RoundedCornerShape(26.dp))
                            .border(1.dp, if (isPlaying) Color.Transparent else GlassBorder, RoundedCornerShape(26.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isPlaying) "⏸" else "▶", fontSize = 22.sp, color = Color.White)
                    }
                    Text("↺", fontSize = 22.sp, color = CyanNeon, modifier = Modifier.clickable { vm.replayCurrentTrack() })
                    Text("⏭", fontSize = 22.sp, color = Text2, modifier = Modifier.clickable { vm.playNextTrack() })
                }
                val isLooping by vm.isLoopingState.collectAsState()
                val loopIconColor = if (isLooping) CyanNeon else Text2
                Text("🔁", fontSize = 18.sp, color = loopIconColor, modifier = Modifier.clickable { vm.toggleLoop() })
            }
            Spacer(modifier = Modifier.height(18.dp))
            var muteOnSpeech by remember { mutableStateOf(AppPreferences.muteMusicOnSpeech) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        muteOnSpeech = !muteOnSpeech
                        AppPreferences.muteMusicOnSpeech = muteOnSpeech
                    }
                    .background(GlassHi.copy(alpha=0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔇", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tự động tắt nhạc khi AI nói", style = Typography.bodyMedium.copy(color = Text2))
                }
                Text(if (muteOnSpeech) "BẬT" else "TẮT", style = Typography.labelMedium.copy(color = if (muteOnSpeech) CyanNeon else Text3))
            }
        }
    }
}

@Composable
fun MoodCategories(selectedCategory: String, onSelect: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { CatChip("Bình thường", "😌 Bình thường", selectedCategory == "Bình thường") { onSelect("Bình thường") } }
        item { CatChip("Stress", "😫 Stress", selectedCategory == "Stress") { onSelect("Stress") } }
        item { CatChip("Lo âu", "😟 Lo âu", selectedCategory == "Lo âu") { onSelect("Lo âu") } }
        item { CatChip("ADHD", "🧠 ADHD", selectedCategory == "ADHD") { onSelect("ADHD") } }
        item { CatChip("Giấc ngủ", "😴 Giấc ngủ", selectedCategory == "Giấc ngủ") { onSelect("Giấc ngủ") } }
    }
}

@Composable
fun CatChip(id: String, label: String, active: Boolean, onClick: () -> Unit) {
    val bgBrush: Brush = if (active) Brush.linearGradient(listOf(Pink.copy(0.2f), Purple.copy(0.2f))) else androidx.compose.ui.graphics.SolidColor(Glass)
    Text(
        label,
        style = Typography.labelMedium.copy(color = if(active) PinkNeon else Text2),
        modifier = Modifier
            .clickable { onClick() }
            .background(bgBrush, RoundedCornerShape(50))
            .border(1.dp, if(active) Pink.copy(0.35f) else GlassBorder, RoundedCornerShape(50))
            .padding(horizontal = 15.dp, vertical = 8.dp)
    )
}

@Composable
fun Playlist(list: List<MusicTrack>, listFavoriteTracks: Set<String>, onToggleFavorite: (MusicTrack) -> Unit, onTrackClick: (MusicTrack) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        list.forEach { track ->
            val isFav = listFavoriteTracks.contains(track.title)
            PlaylistItem(track.emoji, track.title, track.desc, track.time, isFav, { onToggleFavorite(track) }) {
                onTrackClick(track)
            }
        }
    }
}

@Composable
fun PlaylistItem(emoji: String, title: String, desc: String, time: String, isFav: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(46.dp).background(Cyan.copy(0.12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = Typography.labelMedium.copy(color = Text1))
            Text(desc, style = Typography.bodySmall.copy(color = Text3))
        }
        Text(time, style = Typography.bodySmall.copy(color = Text3), modifier = Modifier.padding(end = 8.dp))
        val heartColor = if (isFav) PinkNeon else GlassBorder
        Text("♥", fontSize = 24.sp, color = heartColor, modifier = Modifier.clickable { onToggleFavorite() })
    }
}
