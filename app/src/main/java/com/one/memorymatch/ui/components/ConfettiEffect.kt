package com.one.memorymatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import kotlin.math.sin
import kotlin.random.Random

private val ConfettiColors = listOf(
    Color(0xFFFFD700), // Gold
    Color(0xFFFF5722), // Coral / Orange
    Color(0xFF4CAF50), // Green
    Color(0xFF2196F3), // Blue
    Color(0xFFE91E63), // Pink
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4)  // Cyan
)

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var width: Float,
    var height: Float,
    var rotation: Float,
    var vRot: Float,
    var color: Color,
    var isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 45,
    durationMs: Long = 3500L
) {
    var animationTime by remember { mutableStateOf(0L) }
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }

    LaunchedEffect(Unit) {
        val startTime = withFrameMillis { it }
        while (true) {
            val frameTime = withFrameMillis { it }
            val elapsed = frameTime - startTime
            animationTime = elapsed
            if (elapsed > durationMs) break
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("confetti_canvas")
    ) {
        if (particles.isEmpty() && size.width > 0 && size.height > 0) {
            val random = Random(42)
            particles = List(particleCount) {
                ConfettiParticle(
                    x = random.nextFloat() * size.width,
                    y = -random.nextFloat() * size.height * 0.5f,
                    vx = (random.nextFloat() - 0.5f) * 4f,
                    vy = 3f + random.nextFloat() * 6f,
                    width = 12f + random.nextFloat() * 12f,
                    height = 8f + random.nextFloat() * 10f,
                    rotation = random.nextFloat() * 360f,
                    vRot = (random.nextFloat() - 0.5f) * 12f,
                    color = ConfettiColors[it % ConfettiColors.size],
                    isCircle = random.nextBoolean()
                )
            }
        }

        // Draw and update particles
        for (p in particles) {
            p.y += p.vy
            p.x += p.vx + sin(p.y * 0.05f) * 1.2f
            p.rotation += p.vRot

            if (p.isCircle) {
                drawCircle(
                    color = p.color,
                    radius = p.width / 2f,
                    center = Offset(p.x, p.y)
                )
            } else {
                rotate(degrees = p.rotation, pivot = Offset(p.x + p.width / 2f, p.y + p.height / 2f)) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.width, p.height)
                    )
                }
            }
        }
    }
}
