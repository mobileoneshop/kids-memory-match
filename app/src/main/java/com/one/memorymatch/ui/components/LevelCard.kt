package com.one.memorymatch.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.PackProgress
import com.one.memorymatch.ui.theme.CardShape
import com.one.memorymatch.ui.theme.PillShape
import com.one.memorymatch.ui.theme.TextDark
import com.one.memorymatch.ui.theme.TextMuted

@Composable
fun LevelCard(
    level: GameLevel,
    progress: PackProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF4CAF50)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "level_card_bounce"
    )

    val (titleRes, subtitleRes, badgeColor) = when (level) {
        GameLevel.EASY -> Triple(R.string.level_easy, R.string.level_easy_subtitle, Color(0xFF4CAF50))
        GameLevel.MEDIUM -> Triple(R.string.level_medium, R.string.level_medium_subtitle, Color(0xFFFF9800))
        GameLevel.HARD -> Triple(R.string.level_hard, R.string.level_hard_subtitle, Color(0xFF9C27B0))
    }

    val levelTag = level.name.lowercase()

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 80.dp)
            .scale(scale)
            .clip(CardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("level_card_$levelTag")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Level Title, Grid/Pairs Subtitle, Best Stats
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Difficulty color dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )

                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 19.sp
                        )
                    )

                    // Subtitle pill badge
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(subtitleRes),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stats row
                val hasPlayed = progress.bestMoves != Int.MAX_VALUE
                Text(
                    text = if (hasPlayed) {
                        stringResource(R.string.level_best_stats, progress.bestMoves, progress.bestTimeSec)
                    } else {
                        stringResource(R.string.level_not_played)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (hasPlayed) TextDark.copy(alpha = 0.8f) else TextMuted,
                        fontWeight = if (hasPlayed) FontWeight.Medium else FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.testTag("level_stats_$levelTag")
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Stars & Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StarRow(
                    stars = progress.stars,
                    starSize = 24.dp,
                    spacing = 3.dp,
                    animated = false,
                    modifier = Modifier.testTag("level_stars_$levelTag")
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
