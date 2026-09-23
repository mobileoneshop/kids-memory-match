package com.one.memorymatch.ui.level

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.one.memorymatch.R
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.data.model.CardPack
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.PackProgress
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.components.FloatingCloudsBackground
import com.one.memorymatch.ui.components.LevelCard
import com.one.memorymatch.ui.components.StarIcon
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.PillShape
import com.one.memorymatch.ui.theme.TextDark
import java.io.InputStream

@Composable
fun LevelSelectScreen(
    packId: String,
    onLevelSelected: (GameLevel) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    packRepository: PackRepository? = null,
    progressRepository: ProgressRepository? = null,
    soundManager: SoundManager? = null
) {
    val context = LocalContext.current
    val actualPackRepo = packRepository ?: remember(context) { PackRepository(context) }
    val actualProgressRepo = progressRepository ?: remember(context) { ProgressRepository(context) }

    val viewModel: LevelSelectViewModel = viewModel(
        factory = LevelSelectViewModel.Factory(packId, actualPackRepo, actualProgressRepo)
    )

    val uiState by viewModel.uiState.collectAsState()
    val activeSoundManager = remember { soundManager ?: SoundManager(context) }

    val skyGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE1F5FE),
                Color(0xFFB3E5FC),
                Color(0xFF81D4FA)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(skyGradient)
            .statusBarsPadding()
            .testTag("level_select_screen")
    ) {
        // Decorative background clouds
        FloatingCloudsBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Back Button, Pack Info, and Star Pill
            LevelSelectTopBar(
                pack = uiState.pack,
                totalStars = uiState.totalPackStars,
                onBack = {
                    activeSoundManager.playClick()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ButtonPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else {
                // 3 Level Cards
                val packColor = uiState.pack?.let { Color(it.primaryColor) } ?: ButtonPrimary

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val levels = listOf(GameLevel.EASY, GameLevel.MEDIUM, GameLevel.HARD)
                    items(levels.size) { index ->
                        val level = levels[index]
                        val progress = uiState.progress[level] ?: PackProgress(
                            packId = packId,
                            level = level,
                            stars = 0,
                            bestMoves = Int.MAX_VALUE,
                            bestTimeSec = Int.MAX_VALUE
                        )

                        LevelCard(
                            level = level,
                            progress = progress,
                            accentColor = packColor,
                            onClick = {
                                activeSoundManager.playClick()
                                onLevelSelected(level)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelSelectTopBar(
    pack: CardPack?,
    totalStars: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val packName = when {
        pack == null -> ""
        pack.nameRes != 0 -> stringResource(pack.nameRes)
        else -> pack.name
    }

    val bitmap = remember(pack?.iconAsset) {
        pack?.iconAsset?.let { path ->
            try {
                val stream: InputStream = context.assets.open(path)
                stream.use { BitmapFactory.decodeStream(it) }
            } catch (_: Exception) {
                null
            }
        }
    }

    val packBrush = remember(pack?.id, pack?.primaryColor, pack?.darkColor) {
        when {
            pack == null -> Brush.linearGradient(listOf(ButtonPrimary, ButtonPrimary))
            pack.isVirtual -> Brush.linearGradient(
                listOf(
                    Color(0xFFFF5252),
                    Color(0xFFFFB300),
                    Color(0xFF4CAF50),
                    Color(0xFF29B6F6),
                    Color(0xFFAB47BC)
                )
            )
            else -> Brush.verticalGradient(
                listOf(Color(pack.primaryColor), Color(pack.darkColor))
            )
        }
    }

    Row(
        modifier = modifier.testTag("level_select_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back button (64dp touch target)
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                )
                .testTag("btn_back")
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = ButtonPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center: Pack icon + Pack Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(packBrush),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_pack_icon),
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    val emoji = when (pack?.id) {
                        "zoo" -> "🦁"
                        "farm" -> "🐮"
                        "sea" -> "🐬"
                        "birds" -> "🦜"
                        "fruits" -> "🍎"
                        "vegetables" -> "🥕"
                        "vehicles" -> "🚗"
                        "shapes_colors" -> "🎨"
                        "allmix" -> "🎲"
                        else -> "✨"
                    }
                    Text(text = emoji, fontSize = 26.sp)
                }
            }

            Text(
                text = packName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 24.sp
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right: Stars Pill (Total stars earned for this pack / 9)
        Card(
            shape = PillShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .semantics(mergeDescendants = true) {}
                .testTag("level_select_pack_stars")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StarIcon(
                    isEarned = totalStars > 0,
                    size = 20.dp,
                    animated = false
                )
                Text(
                    text = stringResource(R.string.pack_stars_format, totalStars),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
            }
        }
    }
}
