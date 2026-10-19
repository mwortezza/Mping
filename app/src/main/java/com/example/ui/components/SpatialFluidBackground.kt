package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.ConnectionStatus
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.SpaceBlack
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpatialFluidBackground(
    status: ConnectionStatus,
    throughputMultiplier: Float = 1f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_orbs")

    val durationBase = when (status) {
        ConnectionStatus.CONNECTED -> (8000 / throughputMultiplier.coerceIn(1f, 3f)).toInt()
        ConnectionStatus.CONNECTING -> 4000
        else -> 14000
    }

    val progress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationBase, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p1"
    )

    val progress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (durationBase * 1.35).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p2"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Base deep obsidian space void
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    SpaceBlack,
                    Color(0xFF080C14),
                    Color(0xFF05070B),
                    Color(0xFF020306)
                )
            )
        )

        val angle1 = (progress1 * 2 * Math.PI).toFloat()
        val angle2 = (progress2 * 2 * Math.PI).toFloat()

        // Dynamic intensities based on VPN status
        val alphaMultiplier = when (status) {
            ConnectionStatus.CONNECTED -> 0.45f * pulseGlow
            ConnectionStatus.CONNECTING -> 0.55f * pulseGlow
            else -> 0.28f
        }

        // Orb 1: Liquid Cyan - drifting top-left to center
        val orb1X = width * 0.35f + cos(angle1) * (width * 0.25f)
        val orb1Y = height * 0.28f + sin(angle1 * 1.2f) * (height * 0.15f)
        val orb1Radius = (width * 0.55f) * (if (status == ConnectionStatus.CONNECTED) 1.15f else 1f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidCyan.copy(alpha = alphaMultiplier * 0.8f),
                    LiquidCyan.copy(alpha = alphaMultiplier * 0.3f),
                    Color.Transparent
                ),
                center = Offset(orb1X, orb1Y),
                radius = orb1Radius
            ),
            radius = orb1Radius,
            center = Offset(orb1X, orb1Y)
        )

        // Orb 2: Electric Indigo - undulating bottom-right
        val orb2X = width * 0.7f + sin(angle2) * (width * 0.22f)
        val orb2Y = height * 0.65f + cos(angle2) * (height * 0.18f)
        val orb2Radius = width * 0.65f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ElectricIndigo.copy(alpha = alphaMultiplier * 0.85f),
                    ElectricIndigo.copy(alpha = alphaMultiplier * 0.25f),
                    Color.Transparent
                ),
                center = Offset(orb2X, orb2Y),
                radius = orb2Radius
            ),
            radius = orb2Radius,
            center = Offset(orb2X, orb2Y)
        )

        // Orb 3: Neon Rose - floating accent near core
        val orb3X = width * 0.5f + cos(angle2 * 1.4f) * (width * 0.3f)
        val orb3Y = height * 0.45f + sin(angle1 * 0.9f) * (height * 0.12f)
        val orb3Radius = width * 0.45f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonRose.copy(alpha = alphaMultiplier * 0.65f),
                    NeonRose.copy(alpha = alphaMultiplier * 0.18f),
                    Color.Transparent
                ),
                center = Offset(orb3X, orb3Y),
                radius = orb3Radius
            ),
            radius = orb3Radius,
            center = Offset(orb3X, orb3Y)
        )

        // Orb 4: Plasma Sky / Violet caustic highlight
        val orb4X = width * 0.2f + sin(angle1 * 1.5f) * (width * 0.15f)
        val orb4Y = height * 0.8f + cos(angle2 * 0.8f) * (height * 0.1f)
        val orb4Radius = width * 0.5f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF7209B7).copy(alpha = alphaMultiplier * 0.5f),
                    Color(0xFF4CC9F0).copy(alpha = alphaMultiplier * 0.15f),
                    Color.Transparent
                ),
                center = Offset(orb4X, orb4Y),
                radius = orb4Radius
            ),
            radius = orb4Radius,
            center = Offset(orb4X, orb4Y)
        )
    }
}
