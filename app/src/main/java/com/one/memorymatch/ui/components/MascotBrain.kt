package com.one.memorymatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MascotBrain(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showCards: Boolean = true
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Brain colors
            val brainPink = Color(0xFFFF80AB)
            val brainDarkPink = Color(0xFFF50057)
            val cheekPink = Color(0xFFFF4081).copy(alpha = 0.5f)
            val eyeColor = Color(0xFF263238)
            val cardColor1 = Color(0xFFFFD54F)
            val cardColor2 = Color(0xFF4FC3F7)

            if (showCards) {
                // Left tilted memory card
                drawRoundRect(
                    color = cardColor1,
                    topLeft = Offset(w * 0.08f, h * 0.28f),
                    size = Size(w * 0.28f, h * 0.36f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                )
                // Left card question mark / pattern
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = w * 0.05f,
                    center = Offset(w * 0.22f, h * 0.46f)
                )

                // Right tilted memory card
                drawRoundRect(
                    color = cardColor2,
                    topLeft = Offset(w * 0.64f, h * 0.28f),
                    size = Size(w * 0.28f, h * 0.36f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = w * 0.05f,
                    center = Offset(w * 0.78f, h * 0.46f)
                )
            }

            // Brain body lobes (cute stylized clouds/circles)
            // Center base
            drawRoundRect(
                color = brainPink,
                topLeft = Offset(w * 0.22f, h * 0.22f),
                size = Size(w * 0.56f, h * 0.52f),
                cornerRadius = CornerRadius(w * 0.26f, w * 0.26f)
            )

            // Top left lobe
            drawCircle(
                color = brainPink,
                radius = w * 0.18f,
                center = Offset(w * 0.38f, h * 0.34f)
            )
            // Top right lobe
            drawCircle(
                color = brainPink,
                radius = w * 0.18f,
                center = Offset(w * 0.62f, h * 0.34f)
            )
            // Middle-left lobe
            drawCircle(
                color = brainPink,
                radius = w * 0.15f,
                center = Offset(w * 0.27f, h * 0.48f)
            )
            // Middle-right lobe
            drawCircle(
                color = brainPink,
                radius = w * 0.15f,
                center = Offset(w * 0.73f, h * 0.48f)
            )

            // Brain crease line in top middle
            val creasePath = Path().apply {
                moveTo(w * 0.5f, h * 0.22f)
                cubicTo(w * 0.47f, h * 0.32f, w * 0.53f, h * 0.38f, w * 0.5f, h * 0.44f)
            }
            drawPath(
                path = creasePath,
                color = brainDarkPink.copy(alpha = 0.6f),
                style = Stroke(width = w * 0.025f, cap = StrokeCap.Round)
            )

            // Cute Happy Eyes
            // Left eye
            drawCircle(
                color = eyeColor,
                radius = w * 0.045f,
                center = Offset(w * 0.41f, h * 0.52f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.015f,
                center = Offset(w * 0.425f, h * 0.505f)
            )

            // Right eye
            drawCircle(
                color = eyeColor,
                radius = w * 0.045f,
                center = Offset(w * 0.59f, h * 0.52f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.015f,
                center = Offset(w * 0.605f, h * 0.505f)
            )

            // Cheeks (blush)
            drawCircle(
                color = cheekPink,
                radius = w * 0.05f,
                center = Offset(w * 0.33f, h * 0.57f)
            )
            drawCircle(
                color = cheekPink,
                radius = w * 0.05f,
                center = Offset(w * 0.67f, h * 0.57f)
            )

            // Big smile
            val smilePath = Path().apply {
                moveTo(w * 0.44f, h * 0.60f)
                cubicTo(w * 0.47f, h * 0.68f, w * 0.53f, h * 0.68f, w * 0.56f, h * 0.60f)
            }
            drawPath(
                path = smilePath,
                color = eyeColor,
                style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
            )
        }
    }
}
