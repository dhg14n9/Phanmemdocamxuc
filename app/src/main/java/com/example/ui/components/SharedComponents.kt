package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Bg0
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.Glass
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassHi
import com.example.ui.theme.Pink
import com.example.ui.theme.PinkNeon
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.Text1
import com.example.ui.theme.Text3
import com.example.ui.theme.Typography
import com.example.R

@Composable
fun TopBar(titleNormal: String, titleGradient: String, subtitle: String, onSettingsClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subtitle.uppercase(),
                style = Typography.labelSmall.copy(color = Color(0xFF94A3B8), letterSpacing = 1.sp)
            )
            Text(
                text = buildAnnotatedString {
                    append(titleNormal)
                    append(" ")
                    withStyle(SpanStyle(brush = Brush.horizontalGradient(listOf(PurpleNeon, CyanNeon)))) {
                        append(titleGradient)
                    }
                },
                style = Typography.titleLarge.copy(color = Text1)
            )
        }
        Row {
            if (onSettingsClick != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙️", fontSize = 16.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Glass)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔔", fontSize = 16.sp) // replace with simpler bell icon text
                }
            }
        }
    }
}

@Composable
fun AppCopyrightFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App logo reflecting the custom monogram
        Image(
            painter = painterResource(id = R.drawable.ic_copyright_logo),
            contentDescription = "DK HG Logo",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // App branding name
        Text(
            text = "AURA · EMOTION AI",
            style = Typography.labelSmall.copy(
                color = CyanNeon.copy(alpha = 0.85f),
                letterSpacing = 2.sp
            )
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Copyright description requested by user
        Text(
            text = "Copyright © 2026 Do Hoang Giang & Nguyen Dac Kien.\nAll rights reserved.",
            style = Typography.bodySmall.copy(
                color = Text3,
                fontSize = 11.sp,
                lineHeight = 15.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
