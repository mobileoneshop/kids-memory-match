package com.one.memorymatch.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.ui.theme.ButtonPrimary

@Composable
fun GameHud(
    moves: Int,
    elapsedSec: Int,
    isPaused: Boolean,
    onHomeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onPauseToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Home / Back Button (64dp target)
        HudIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_hud_home),
            tint = ButtonPrimary,
            testTag = "hud_home_button",
            onClick = onHomeClick
        )

        // Stats Badges (Moves + Timer)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Moves Pill
            HudPill(
                iconEmoji = "🐾",
                label = stringResource(R.string.game_moves_format, moves),
                testTag = "hud_moves_text"
            )

            // Timer Pill
            val minutes = elapsedSec / 60
            val seconds = elapsedSec % 60
            val timeString = stringResource(R.string.game_time_format, minutes, seconds)
            HudPill(
                iconEmoji = "⏱️",
                label = timeString,
                testTag = "hud_timer_text"
            )
        }

        // Action Buttons: Restart + Pause/Resume
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HudIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.cd_hud_restart),
                tint = Color(0xFF1E88E5), // Cheerful blue
                testTag = "hud_restart_button",
                onClick = onRestartClick
            )

            HudIconButton(
                icon = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Refresh, // Pause or Play
                contentDescription = if (isPaused) stringResource(R.string.cd_hud_resume) else stringResource(R.string.cd_hud_pause),
                tint = if (isPaused) Color(0xFF43A047) else Color(0xFF7E57C2),
                isPauseIcon = !isPaused,
                testTag = "hud_pause_button",
                onClick = onPauseToggleClick
            )
        }
    }
}

@Composable
fun HudPill(
    iconEmoji: String,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = iconEmoji,
                fontSize = 18.sp
            )
            Text(
                text = label,
                modifier = Modifier.testTag(testTag),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238),
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun HudIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPauseIcon: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "hud_btn_scale"
    )

    Card(
        modifier = modifier
            .size(56.dp) // Touch area expands to 64dp with surrounding padding
            .scale(scale)
            .testTag(testTag),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .semantics { this.contentDescription = contentDescription }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isPauseIcon) {
                // Two vertical pause bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(tint)
                    )
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(tint)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
