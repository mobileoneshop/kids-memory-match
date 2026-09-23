package com.one.memorymatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FloatingCloudsBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cloudColor = Color.White.copy(alpha = 0.45f)

        // Top cloud 1
        drawCircle(cloudColor, radius = 55.dp.toPx(), center = Offset(60.dp.toPx(), 40.dp.toPx()))
        drawCircle(cloudColor, radius = 40.dp.toPx(), center = Offset(110.dp.toPx(), 45.dp.toPx()))

        // Middle cloud 2
        val rightX = size.width - 50.dp.toPx()
        drawCircle(cloudColor, radius = 60.dp.toPx(), center = Offset(rightX, 220.dp.toPx()))
        drawCircle(cloudColor, radius = 45.dp.toPx(), center = Offset(rightX - 50.dp.toPx(), 230.dp.toPx()))

        // Bottom cloud 3
        drawCircle(cloudColor, radius = 70.dp.toPx(), center = Offset(80.dp.toPx(), size.height - 120.dp.toPx()))
        drawCircle(cloudColor, radius = 50.dp.toPx(), center = Offset(140.dp.toPx(), size.height - 110.dp.toPx()))
    }
}
