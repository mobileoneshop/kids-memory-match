package com.one.memorymatch.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.one.memorymatch.ui.theme.StarEmpty
import com.one.memorymatch.ui.theme.StarEmptyDark
import com.one.memorymatch.ui.theme.StarGold
import com.one.memorymatch.ui.theme.StarGoldDark
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StarIcon(
    isEarned: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    animated: Boolean = true,
    delayMs: Long = 0L
) {
    val scale = remember { Animatable(if (animated && isEarned) 0f else 1f) }

    LaunchedEffect(isEarned) {
        if (animated && isEarned) {
            delay(delayMs)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scale.snapTo(1f)
        }
    }

    val fillColor = if (isEarned) StarGold else StarEmpty
    val strokeColor = if (isEarned) StarGoldDark else StarEmptyDark

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        val width = this.size.width
        val height = this.size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val outerRadius = width.coerceAtMost(height) / 2f * 0.95f
        val innerRadius = outerRadius * 0.42f

        val path = Path()
        val numPoints = 5
        val angleStep = (2 * PI / (numPoints * 2)).toFloat()
        var currentAngle = (-PI / 2).toFloat()

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = centerX + r * cos(currentAngle)
            val y = centerY + r * sin(currentAngle)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            currentAngle += angleStep
        }
        path.close()

        // Fill
        drawPath(path = path, color = fillColor, style = Fill)
        // Outline
        drawPath(path = path, color = strokeColor, style = Stroke(width = outerRadius * 0.08f))
    }
}

@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    maxStars: Int = 3,
    starSize: Dp = 48.dp,
    spacing: Dp = 8.dp,
    animated: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isEarned = i <= stars
            StarIcon(
                isEarned = isEarned,
                size = starSize,
                animated = animated,
                delayMs = (i - 1) * 150L
            )
        }
    }
}
