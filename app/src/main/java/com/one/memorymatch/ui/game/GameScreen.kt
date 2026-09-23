package com.one.memorymatch.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.one.memorymatch.R
import com.one.memorymatch.data.model.GamePhase
import com.one.memorymatch.ui.components.BigButton
import com.one.memorymatch.ui.components.ConfettiEffect
import com.one.memorymatch.ui.components.FloatingCloudsBackground
import com.one.memorymatch.ui.theme.ButtonSuccess
import com.one.memorymatch.ui.theme.SkyBottom
import com.one.memorymatch.ui.theme.SkyTop

@Composable
fun GameScreen(
    packId: String,
    level: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(LocalContext.current, packId, level)
    )
) {
    val state by viewModel.uiState.collectAsState()

    val backgroundBrush = if (state.pack != null) {
        Brush.verticalGradient(
            listOf(
                Color(state.pack!!.primaryColor),
                Color(state.pack!!.darkColor)
            )
        )
    } else {
        Brush.verticalGradient(listOf(SkyTop, SkyBottom))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .testTag("game_screen")
    ) {
        FloatingCloudsBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // HUD at top
            GameHud(
                moves = state.moves,
                elapsedSec = state.elapsedSec,
                isPaused = state.isPaused,
                onHomeClick = onNavigateBack,
                onRestartClick = { viewModel.restart() },
                onPauseToggleClick = { viewModel.togglePause() }
            )

            // Grid area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AdaptiveCardGrid(
                    state = state,
                    onCardClick = { cardUid -> viewModel.onCardClicked(cardUid) }
                )
            }
        }

        // Back handlers for overlays
        BackHandler(enabled = state.showWinDialog) {
            onNavigateBack()
        }
        BackHandler(enabled = state.isPaused && !state.showWinDialog) {
            viewModel.setPaused(false)
        }

        // Confetti Celebration
        if (state.phase == GamePhase.WON) {
            ConfettiEffect()
        }

        // Paused Overlay (full-screen)
        if (state.isPaused) {
            PauseOverlay(
                onResume = { viewModel.setPaused(false) },
                onRestart = { viewModel.restart() },
                onHome = onNavigateBack
            )
        }

        // Win Celebration Dialog (full-screen)
        if (state.showWinDialog) {
            WinDialog(
                starsEarned = state.starsEarned,
                moves = state.moves,
                elapsedSec = state.elapsedSec,
                isNewBest = state.isNewBest,
                onPlayAgain = { viewModel.restart() },
                onChoosePack = onNavigateBack,
                onClose = onNavigateBack
            )
        }
    }
}

@Composable
fun AdaptiveCardGrid(
    state: GameScreenState,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val cols = if (isLandscape) maxOf(state.level.cols, state.level.rows) else minOf(state.level.cols, state.level.rows)
    val rows = if (isLandscape) minOf(state.level.cols, state.level.rows) else maxOf(state.level.cols, state.level.rows)
    val spacing = if (rows >= 6 || cols >= 6) 6.dp else 8.dp

    BoxWithConstraints(
        modifier = modifier.testTag("card_grid"),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val totalSpacingWidth = spacing * (cols - 1)
        val totalSpacingHeight = spacing * (rows - 1)

        val maxCardWidth = (availableWidth - totalSpacingWidth) / cols
        val maxCardHeight = (availableHeight - totalSpacingHeight) / rows

        val cardSize = minOf(maxCardWidth, maxCardHeight)

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0 until cols) {
                        val index = row * cols + col
                        if (index < state.cards.size) {
                            val card = state.cards[index]
                            val item = state.itemsMap[card.itemId]

                            MemoryCard(
                                card = card,
                                item = item,
                                pack = state.pack,
                                onClick = { onCardClick(card.uid) },
                                modifier = Modifier.size(cardSize)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(cardSize))
                        }
                    }
                }
            }
        }
    }
}
