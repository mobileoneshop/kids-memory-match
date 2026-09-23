package com.one.memorymatch.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.ui.components.BigButton
import com.one.memorymatch.ui.components.StarIcon
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.ButtonSuccess
import com.one.memorymatch.ui.theme.StarGold
import com.one.memorymatch.ui.theme.StarGoldDark

@Composable
fun WinDialog(
    starsEarned: Int,
    moves: Int,
    elapsedSec: Int,
    isNewBest: Boolean,
    onPlayAgain: () -> Unit,
    onChoosePack: () -> Unit,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = onChoosePack
) {
    val dialogScale = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        dialogScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val minutes = elapsedSec / 60
    val seconds = elapsedSec % 60
    val statsString = stringResource(R.string.win_stats_format, moves, minutes, seconds)
    val dialogCd = stringResource(R.string.cd_win_dialog)

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = dialogCd }
            .testTag("win_dialog"),
        contentAlignment = Alignment.Center
    ) {
        // Scrim background with dismiss on outside click
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose
                )
        )

        Card(
            modifier = Modifier
                .scale(dialogScale.value)
                .fillMaxWidth(0.90f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Top-Right Close Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                ) {
                    WinCloseButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("win_close_button")
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Celebration Title
                    Text(
                        text = stringResource(R.string.win_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32),
                            fontSize = 32.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.win_subtitle),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF546E7A),
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    // Animated 3-star row with staggered pop
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .testTag("win_stars_row"),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..3) {
                            StarIcon(
                                isEarned = i <= starsEarned,
                                size = 56.dp,
                                animated = true,
                                delayMs = (i - 1) * 260L
                            )
                        }
                    }

                    // "New Best!" badge
                    if (isNewBest) {
                        Box(
                            modifier = Modifier
                                .testTag("win_new_best_badge")
                                .background(
                                    brush = Brush.horizontalGradient(listOf(StarGold, StarGoldDark)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.win_new_best),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Moves & Time Stats Pill
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = statsString,
                            modifier = Modifier
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                                .testTag("win_stats_text"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF33691E),
                                fontSize = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BigButton(
                            text = stringResource(R.string.btn_play_again),
                            backgroundColor = ButtonSuccess,
                            onClick = onPlayAgain,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_play_again")
                        )

                        BigButton(
                            text = stringResource(R.string.btn_choose_pack),
                            backgroundColor = ButtonPrimary,
                            onClick = onChoosePack,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_choose_pack")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WinCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "win_close_scale"
    )

    Card(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .testTag("win_close_button")
            .testTag("win_btn_close"),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Close" }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_win_close),
                tint = Color(0xFF546E7A),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
