package com.one.memorymatch.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(32.dp)
val SmallRoundedShape = RoundedCornerShape(16.dp)

val AppShapes = Shapes(
    small = SmallRoundedShape,
    medium = CardShape,
    large = PillShape
)
