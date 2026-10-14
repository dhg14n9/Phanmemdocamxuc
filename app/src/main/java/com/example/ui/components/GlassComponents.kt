package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Glass
import com.example.ui.theme.GlassBorder

fun Modifier.glow(
    color: Color,
    radius: Dp = 30.dp,
    alpha: Float = 0.08f
) = this.drawBehind {
    val paint = Paint()
    paint.color = color.copy(alpha = alpha)
    
    drawIntoCanvas { canvas ->
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.setShadowLayer(radius.toPx(), 0f, 0f, frameworkPaint.color)
        canvas.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, 28.dp.toPx(), 28.dp.toPx(), frameworkPaint)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color? = null,
    cornerRadius: Dp = 22.dp,
    content: @Composable () -> Unit
) {
    var mod = modifier
    if (glowColor != null) {
        mod = mod.glow(color = glowColor)
    }
    Box(
        modifier = mod
            .clip(RoundedCornerShape(cornerRadius))
            .background(Glass)
            .border(1.dp, glowColor?.copy(alpha = 0.25f) ?: GlassBorder, RoundedCornerShape(cornerRadius))
    ) {
        content()
    }
}
