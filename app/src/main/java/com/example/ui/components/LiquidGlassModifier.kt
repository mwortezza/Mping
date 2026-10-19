package com.example.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LiquidCyan

fun Modifier.mpingLiquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    blurRadius: Float = 25f,
    surfaceAlpha: Float = 0.10f,
    hasChromaticGlow: Boolean = false
): Modifier = this
    .graphicsLayer {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f) {
            renderEffect = RenderEffect
                .createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    }
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = surfaceAlpha + 0.04f),
                GlassSurface.copy(alpha = surfaceAlpha),
                Color(0xFF0F172A).copy(alpha = 0.35f)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = if (hasChromaticGlow) {
                listOf(
                    Color.White.copy(alpha = 0.7f),
                    LiquidCyan.copy(alpha = 0.4f),
                    Color.White.copy(alpha = 0.15f),
                    GlassBorderSubtle
                )
            } else {
                listOf(
                    GlassBorderHighlight,
                    Color.White.copy(alpha = 0.25f),
                    GlassBorderSubtle,
                    Color.Transparent
                )
            }
        ),
        shape = shape
    )

@Composable
fun MpingLiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    blurRadius: Float = 20f,
    surfaceAlpha: Float = 0.10f,
    hasChromaticGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.mpingLiquidGlass(
            shape = shape,
            blurRadius = blurRadius,
            surfaceAlpha = surfaceAlpha,
            hasChromaticGlow = hasChromaticGlow
        ),
        content = content
    )
}
