package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.VpnState
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NeonRose
import com.example.ui.theme.PingFair
import com.example.ui.theme.PlasmaSky
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextHigh
import com.example.ui.theme.TextMedium
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ActiveFluidRipple(
    val id: Long,
    val primaryColor: Color,
    val secondaryColor: Color,
    val radiusAnim: Animatable<Float, *>,
    val alphaAnim: Animatable<Float, *>,
    val maxRadius: Float
)

private data class MicroBubble(
    val initialXRatio: Float,
    val speedY: Float,
    val size: Float,
    val phaseOffset: Float
)

@Composable
fun MpingLiquidCore(
    vpnState: VpnState,
    onToggleConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val isConnected = vpnState.status == ConnectionStatus.CONNECTED
    val isConnecting = vpnState.status == ConnectionStatus.CONNECTING

    // 1. Spring Physics Animatable Controllers
    val coreScaleAnim = remember { Animatable(1f) }
    val squashXAnim = remember { Animatable(1f) }
    val squashYAnim = remember { Animatable(1f) }
    val liquidLevelSpring = remember { Animatable(0.24f) }

    // Multi-ring cascading fluid ripples reacting to state & taps
    val activeRipples = remember { mutableStateListOf<ActiveFluidRipple>() }

    // Seed floating buoyancy micro-bubbles
    val microBubbles = remember {
        List(14) {
            MicroBubble(
                initialXRatio = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.6f + 0.4f,
                size = Random.nextFloat() * 4f + 2f,
                phaseOffset = Random.nextFloat() * (2 * PI).toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mping_core_infinite")

    // Ambient breathing motion
    val breathingFloat by infiniteTransition.animateFloat(
        initialValue = 0.975f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Orbital ring rotation
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnected) 4000 else 11000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    // Fluid wave interference phase
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isConnected) 1600 else 3600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    // Bubble vertical flow
    val bubbleFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_flow"
    )

    fun spawnFluidRipple(
        primary: Color,
        secondary: Color,
        maxRadius: Float = 220f,
        duration: Int = 900
    ) {
        val rippleId = System.nanoTime()
        val rAnim = Animatable(0f)
        val aAnim = Animatable(0.9f)
        val ripple = ActiveFluidRipple(
            id = rippleId,
            primaryColor = primary,
            secondaryColor = secondary,
            radiusAnim = rAnim,
            alphaAnim = aAnim,
            maxRadius = maxRadius
        )
        activeRipples.add(ripple)

        coroutineScope.launch {
            launch {
                rAnim.animateTo(
                    maxRadius,
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
            }
            launch {
                aAnim.animateTo(
                    0f,
                    animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
                )
                activeRipples.remove(ripple)
            }
        }
    }

    // 2. React Dynamically to Connection State Changes with Fluid Ripples & Springs
    var previousStatus by remember { mutableStateOf<ConnectionStatus?>(null) }
    LaunchedEffect(vpnState.status) {
        val prev = previousStatus
        previousStatus = vpnState.status

        if (prev != null && prev != vpnState.status) {
            when (vpnState.status) {
                ConnectionStatus.CONNECTING -> {
                    // Spring bounce down into preparation
                    launch {
                        coreScaleAnim.animateTo(
                            0.93f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        coreScaleAnim.animateTo(
                            1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    // Liquid level rises with spring overshoot
                    launch {
                        liquidLevelSpring.animateTo(
                            0.42f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    // Amber / Cyan handshake ripple
                    spawnFluidRipple(
                        primary = PingFair,
                        secondary = LiquidCyan,
                        maxRadius = 180f,
                        duration = 1000
                    )
                }

                ConnectionStatus.CONNECTED -> {
                    // Triumphant spring pop & expansion
                    launch {
                        squashXAnim.animateTo(
                            1.08f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                        squashXAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f))
                    }
                    launch {
                        squashYAnim.animateTo(
                            0.94f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                        squashYAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f))
                    }
                    // Liquid rises to 56% full with fluid spring bounce
                    launch {
                        liquidLevelSpring.animateTo(
                            0.56f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    // Dual bioluminescent shockwave ripples
                    spawnFluidRipple(
                        primary = LiquidCyan,
                        secondary = NeonRose,
                        maxRadius = 240f,
                        duration = 1100
                    )
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(180)
                        spawnFluidRipple(
                            primary = NeonRose,
                            secondary = ElectricIndigo,
                            maxRadius = 210f,
                            duration = 950
                        )
                    }
                }

                ConnectionStatus.DISCONNECTING -> {
                    launch {
                        liquidLevelSpring.animateTo(
                            0.30f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
                        )
                    }
                    spawnFluidRipple(
                        primary = NeonRose,
                        secondary = TextMuted,
                        maxRadius = 160f,
                        duration = 750
                    )
                }

                ConnectionStatus.DISCONNECTED -> {
                    launch {
                        liquidLevelSpring.animateTo(
                            0.22f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    spawnFluidRipple(
                        primary = Color.White.copy(alpha = 0.5f),
                        secondary = SpaceBlack,
                        maxRadius = 170f,
                        duration = 800
                    )
                }
            }
        }
    }

    fun handleCoreTap() {
        // Tactile Spring Squash & Bounce
        coroutineScope.launch {
            launch {
                coreScaleAnim.animateTo(
                    0.88f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
                coreScaleAnim.animateTo(
                    1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                squashXAnim.animateTo(
                    1.06f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessHigh)
                )
                squashXAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f))
            }
            launch {
                squashYAnim.animateTo(
                    0.92f,
                    animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessHigh)
                )
                squashYAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = 0.6f))
            }
        }

        // Tap Fluid Ripple Emission
        val rippleColorPrimary = if (isConnected) NeonRose else LiquidCyan
        val rippleColorSecondary = if (isConnected) ElectricIndigo else NeonRose
        spawnFluidRipple(
            primary = rippleColorPrimary,
            secondary = rippleColorSecondary,
            maxRadius = 220f,
            duration = 900
        )

        onToggleConnection()
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .graphicsLayer {
                scaleX = coreScaleAnim.value * squashXAnim.value * breathingFloat
                scaleY = coreScaleAnim.value * squashYAnim.value * breathingFloat
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { handleCoreTap() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.38f

            // 0. Active Fluid Ripple Shockwaves expanding across Canvas
            activeRipples.forEach { ripple ->
                val currentRadius = radius + ripple.radiusAnim.value
                val alpha = ripple.alphaAnim.value
                if (alpha > 0.01f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ripple.primaryColor.copy(alpha = alpha * 0.75f),
                                ripple.secondaryColor.copy(alpha = alpha * 0.35f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = currentRadius
                        ),
                        radius = currentRadius,
                        center = center
                    )
                    // Outer fine refraction shockwave line
                    drawCircle(
                        color = ripple.primaryColor.copy(alpha = alpha * 0.9f),
                        radius = currentRadius,
                        center = center,
                        style = Stroke(width = (2.5f * (1f - ripple.radiusAnim.value / ripple.maxRadius)).coerceAtLeast(1f))
                    )
                }
            }

            // 1. Ambient Outer Bioluminescent Glow Halo
            val glowColor = when {
                isConnected -> LiquidCyan
                isConnecting -> PingFair
                else -> ElectricIndigo
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isConnected) 0.38f else 0.16f),
                        NeonRose.copy(alpha = if (isConnected) 0.22f else 0.06f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.55f
                ),
                radius = radius * 1.55f,
                center = center
            )

            // 2. Iridescent Tilted Orbital Refraction Rings
            drawOval(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        LiquidCyan.copy(alpha = 0.85f),
                        ElectricIndigo.copy(alpha = 0.55f),
                        NeonRose.copy(alpha = 0.85f),
                        PlasmaSky.copy(alpha = 0.65f),
                        LiquidCyan.copy(alpha = 0.85f)
                    ),
                    center = center
                ),
                topLeft = Offset(center.x - radius * 1.38f, center.y - radius * 0.46f),
                size = Size(radius * 2.76f, radius * 0.92f),
                style = Stroke(width = 2.5f)
            )

            // 3. 3D Glass Sphere Backing & Obsidian Void Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141E34).copy(alpha = 0.90f),
                        SpaceBlack.copy(alpha = 0.98f)
                    ),
                    center = Offset(center.x - radius * 0.22f, center.y - radius * 0.28f),
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 4. Fluid Waves (Dual-Layer: Deep Background Wave + Foreground Active Wave)
            val fillLevel = liquidLevelSpring.value
            val baseWaterY = center.y + radius * (1f - 2f * fillLevel)

            drawContext.canvas.save()
            val sphereClip = Path().apply {
                addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
            }
            drawContext.canvas.clipPath(sphereClip)

            // --- Layer A: Secondary Deep Background Wave (Phase Shifted) ---
            val deepWavePath = Path()
            deepWavePath.moveTo(center.x - radius, center.y + radius)
            val waveSegments = 40
            val waveWidth = radius * 2f
            for (i in 0..waveSegments) {
                val x = (center.x - radius) + (i.toFloat() / waveSegments) * waveWidth
                val waveHeight = if (isConnected) 9f else 4.5f
                val y = baseWaterY + sin(waveOffset + PI.toFloat() * 0.5f + i * 0.22f) * waveHeight
                deepWavePath.lineTo(x, y)
            }
            deepWavePath.lineTo(center.x + radius, center.y + radius)
            deepWavePath.close()

            drawPath(
                path = deepWavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElectricIndigo.copy(alpha = 0.50f),
                        Color(0xFF0F172A).copy(alpha = 0.80f)
                    ),
                    startY = baseWaterY - 15f,
                    endY = center.y + radius
                )
            )

            // --- Layer B: Primary Foreground Churning Fluid Wave ---
            val foreWavePath = Path()
            foreWavePath.moveTo(center.x - radius, center.y + radius)
            for (i in 0..waveSegments) {
                val x = (center.x - radius) + (i.toFloat() / waveSegments) * waveWidth
                val waveHeight = if (isConnected) 13f else 6.5f
                val y = baseWaterY + sin(waveOffset + i * 0.25f) * waveHeight
                foreWavePath.lineTo(x, y)
            }
            foreWavePath.lineTo(center.x + radius, center.y + radius)
            foreWavePath.close()

            drawPath(
                path = foreWavePath,
                brush = Brush.verticalGradient(
                    colors = if (isConnected) {
                        listOf(
                            LiquidCyan.copy(alpha = 0.60f),
                            ElectricIndigo.copy(alpha = 0.75f),
                            NeonRose.copy(alpha = 0.65f)
                        )
                    } else if (isConnecting) {
                        listOf(
                            PingFair.copy(alpha = 0.55f),
                            ElectricIndigo.copy(alpha = 0.65f)
                        )
                    } else {
                        listOf(
                            ElectricIndigo.copy(alpha = 0.35f),
                            Color(0xFF0F172A).copy(alpha = 0.60f)
                        )
                    },
                    startY = baseWaterY - 20f,
                    endY = center.y + radius
                )
            )

            // --- Layer C: Rising Buoyancy Micro-Bubbles ---
            if (isConnected || isConnecting) {
                microBubbles.forEach { bubble ->
                    val bx = (center.x - radius * 0.8f) + bubble.initialXRatio * (radius * 1.6f)
                    val progressY = (bubbleFlow * bubble.speedY + bubble.phaseOffset) % 1f
                    val by = (center.y + radius * 0.8f) - progressY * (radius * 1.6f * fillLevel)
                    if (by in baseWaterY..(center.y + radius)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.45f * (1f - progressY)),
                            radius = bubble.size,
                            center = Offset(bx, by)
                        )
                    }
                }
            }

            drawContext.canvas.restore()

            // 5. iOS 27 Liquid Glass Specular Reflections & Fresnel Rim
            // Primary specular crescent (top-left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                    radius = radius * 0.65f
                ),
                radius = radius * 0.65f,
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
            )

            // Secondary subtle bottom refraction glare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LiquidCyan.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(center.x + radius * 0.25f, center.y + radius * 0.45f),
                    radius = radius * 0.45f
                ),
                radius = radius * 0.45f,
                center = Offset(center.x + radius * 0.25f, center.y + radius * 0.45f)
            )

            // Outer 1px Angled Specular Refraction Rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        LiquidCyan.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.22f),
                        NeonRose.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.90f)
                    ),
                    center = center
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 2.2f)
            )
        }

        // Center Content Overlay (Tactile Power Icon, Status, Uptime Duration)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconTint = when {
                isConnected -> LiquidCyan
                isConnecting -> PingFair
                else -> Color.White.copy(alpha = 0.80f)
            }

            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = "Toggle Mping Liquid Core VPN",
                    tint = iconTint,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (vpnState.status) {
                    ConnectionStatus.CONNECTED -> "CONNECTED"
                    ConnectionStatus.CONNECTING -> "SECURING..."
                    ConnectionStatus.DISCONNECTING -> "DISCONNECTING"
                    ConnectionStatus.DISCONNECTED -> "TAP TO CONNECT"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isConnected -> LiquidCyan
                    isConnecting -> PingFair
                    else -> TextHigh
                },
                letterSpacing = 1.2.sp
            )

            if (isConnected) {
                Spacer(modifier = Modifier.height(4.dp))
                val hours = vpnState.connectedDurationSeconds / 3600
                val minutes = (vpnState.connectedDurationSeconds % 3600) / 60
                val seconds = vpnState.connectedDurationSeconds % 60
                val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMedium
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "iOS 27 Liquid Glass Core",
                    fontSize = 9.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
