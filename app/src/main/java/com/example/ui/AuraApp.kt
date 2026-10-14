package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.*

@Composable
fun AuraApp(vm: AuraViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val navController = rememberNavController()
    val isPlaying by vm.isPlaying.collectAsState()
    val currentTrack by vm.currentTrack.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize().background(Bg0)) {
        // Ambient orbs
        Canvas(modifier = Modifier.fillMaxSize().blur(60.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)) {
            val w = size.width
            val h = size.height
            // orb-1
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(PurpleNeon.copy(alpha = 0.2f), Color.Transparent), center = Offset(-80f, -60f), radius = 300f),
                center = Offset(-80f, -60f), radius = 300f
            )
            // orb-2
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(CyanNeon.copy(alpha = 0.2f), Color.Transparent), center = Offset(w+70f, 400f), radius = 250f),
                center = Offset(w+70f, 400f), radius = 250f
            )
            // orb-3
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(Pink.copy(alpha = 0.12f), Color.Transparent), center = Offset(-60f, h - 200f), radius = 250f),
                center = Offset(-60f, h - 200f), radius = 250f
            )
            // orb-4
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(Blue.copy(alpha = 0.14f), Color.Transparent), center = Offset(w+40f, h), radius = 200f),
                center = Offset(w+40f, h), radius = 200f
            )
        }
        
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { 
                Column {
                    if (isPlaying) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Brush.horizontalGradient(listOf(PurpleNeon.copy(0.3f), PinkNeon.copy(0.3f))))
                                .border(1.dp, GlassBorder)
                                .clickable { navController.navigate("music") }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentTrack.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(currentTrack.title, style = Typography.labelMedium.copy(color = Color.White))
                                    Text("Đang phát Healing", style = Typography.bodySmall.copy(color = Text2))
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("⏸", fontSize = 24.sp, color = Color.White, modifier = Modifier.clickable { vm.togglePlay() })
                                Text("⏹", fontSize = 24.sp, color = Color.White, modifier = Modifier.clickable { vm.stopAudio() })
                            }
                        }
                    }
                    BottomNav(navController = navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(paddingValues).fillMaxSize()
            ) {
                composable("dashboard") { DashboardScreen(navController, vm) }
                composable("voice") { VoiceScreen(navController, vm) }
                composable("music") { MusicScreen(navController, vm) }
                composable("charts") { ChartsScreen(navController, vm) }
                composable("chat") { ChatScreen(navController, vm) }
                composable("history") { HistoryScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("breathing") { BreathingScreen(navController) }
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xE6050522))
                .border(
                    1.dp, 
                    Brush.horizontalGradient(listOf(PurpleNeon.copy(alpha = 0.25f), CyanNeon.copy(alpha = 0.25f), PinkNeon.copy(alpha = 0.25f))), 
                    RoundedCornerShape(24.dp)
                )
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("🏠", "Dashboard", currentRoute == "dashboard" || currentRoute == "history") { navController.navigate("dashboard") { launchSingleTop = true } }
            NavItem("🎙", "Giọng nói", currentRoute == "voice") { navController.navigate("voice") { launchSingleTop = true } }
            NavItem("🎵", "Healing", currentRoute == "music") { navController.navigate("music") { launchSingleTop = true } }
            NavItem("🗓", "Nhật ký", currentRoute == "charts") { navController.navigate("charts") { launchSingleTop = true } }
            NavItem("🤖", "AURA AI", currentRoute == "chat") { navController.navigate("chat") { launchSingleTop = true } }
        }
    }
}

@Composable
fun NavItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val activeColor = CyanNeon
    val inactiveColor = Text3
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) PurpleNeon.copy(alpha = 0.16f) else Color.Transparent)
                .then(
                    if (selected) Modifier.border(1.dp, PurpleNeon.copy(alpha = 0.35f), RoundedCornerShape(14.dp)) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon, 
                fontSize = if (selected) 22.sp else 20.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = Typography.labelSmall.copy(
                color = if (selected) activeColor else inactiveColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp
            )
        )
    }
}
