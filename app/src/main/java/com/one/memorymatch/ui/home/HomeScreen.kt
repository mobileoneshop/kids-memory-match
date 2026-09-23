package com.one.memorymatch.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.one.memorymatch.R
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.components.FloatingCloudsBackground
import com.one.memorymatch.ui.components.MascotBrain
import com.one.memorymatch.ui.components.ParentGateDialog
import com.one.memorymatch.ui.components.StarIcon
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.StarGold

@Composable
fun HomeScreen(
    onPackSelected: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel? = null,
    soundManager: SoundManager? = null
) {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel ?: run {
        val packRepo = remember { PackRepository(context) }
        val progRepo = remember { ProgressRepository(context) }
        val factory = remember { HomeViewModel.Factory(packRepo, progRepo) }
        viewModel(factory = factory)
    }

    val uiState by homeViewModel.uiState.collectAsState()
    val activeSoundManager = remember { soundManager ?: SoundManager(context) }

    val skyGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB3E5FC),
            Color(0xFFE1F5FE)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(skyGradient)
            .statusBarsPadding()
            .testTag("home_screen")
    ) {
        // Floating decorative clouds
        FloatingCloudsBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            HomeTopBar(
                totalStars = uiState.totalStars,
                maxStars = uiState.maxPossibleStars,
                onGearClick = {
                    activeSoundManager.playClick()
                    homeViewModel.openParentGate()
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
                // Packs Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("pack_grid")
                ) {
                    items(uiState.packs, key = { it.id }) { pack ->
                        val stars = uiState.packStars[pack.id] ?: 0
                        PackTile(
                            pack = pack,
                            totalStars = stars,
                            onClick = {
                                activeSoundManager.playClick()
                                onPackSelected(pack.id)
                            }
                        )
                    }
                }
            }
        }

        // Parent Gate Modal Dialog
        if (uiState.showParentGate) {
            ParentGateDialog(
                onPass = {
                    homeViewModel.closeParentGate()
                    onOpenSettings()
                },
                onDismiss = {
                    homeViewModel.closeParentGate()
                }
            )
        }
    }
}

@Composable
fun HomeTopBar(
    totalStars: Int,
    maxStars: Int,
    onGearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top row: Mascot + Title on left, Settings Gear on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                MascotBrain(
                    size = 44.dp,
                    showCards = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ButtonPrimary,
                        fontSize = 24.sp
                    )
                )
            }

            // Gear icon button with 64dp minimum touch target
            val settingsCd = stringResource(R.string.cd_home_settings)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .semantics { contentDescription = settingsCd }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onGearClick
                    )
                    .testTag("home_gear_button"),
                contentAlignment = Alignment.Center
            ) {
                GearIcon(
                    size = 28.dp,
                    color = ButtonPrimary
                )
            }
        }

        // Sub row: Stars summary badge pill
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.testTag("home_stars_summary")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StarIcon(
                    isEarned = true,
                    size = 18.dp,
                    animated = false
                )
                Text(
                    text = stringResource(R.string.home_stars_label, totalStars, maxStars),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
fun GearIcon(
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val c = Offset(w / 2f, h / 2f)

        // Outer teeth (6 rounded cogs)
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60).toDouble())
            val cogRadius = w * 0.42f
            val toothCenter = Offset(
                (c.x + cogRadius * Math.cos(angle)).toFloat(),
                (c.y + cogRadius * Math.sin(angle)).toFloat()
            )
            drawCircle(
                color = color,
                radius = w * 0.14f,
                center = toothCenter
            )
        }

        // Inner main body
        drawCircle(
            color = color,
            radius = w * 0.38f,
            center = c
        )

        // Center hole
        drawCircle(
            color = Color.White,
            radius = w * 0.16f,
            center = c
        )
    }
}
