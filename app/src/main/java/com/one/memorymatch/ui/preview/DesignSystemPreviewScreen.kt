package com.one.memorymatch.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.one.memorymatch.R
import com.one.memorymatch.audio.MusicManager
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.audio.Speaker
import com.one.memorymatch.ui.components.BigButton
import com.one.memorymatch.ui.components.StarRow
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.ButtonSecondary
import com.one.memorymatch.ui.theme.ButtonSuccess
import com.one.memorymatch.ui.theme.CardShape
import com.one.memorymatch.ui.theme.KidsMemoryTheme
import com.one.memorymatch.ui.theme.PackColors
import com.one.memorymatch.ui.theme.SkyBottom
import com.one.memorymatch.ui.theme.SkyTop
import com.one.memorymatch.ui.theme.TextLight

@Composable
fun DesignSystemPreviewScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val soundManager = remember { SoundManager(context) }
    val musicManager = remember { MusicManager(context, musicEnabled = false) }
    val speaker = remember { Speaker(context) }

    var isMusicPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
            musicManager.release()
            speaker.release()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyTop, SkyBottom)
                )
            )
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.design_system_preview_title),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Audio SFX & Music Test Section (M6)
        Text(
            text = stringResource(R.string.preview_audio_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BigButton(
                text = stringResource(R.string.preview_test_flip),
                onClick = { soundManager.playFlip() },
                backgroundColor = ButtonPrimary,
                modifier = Modifier.weight(1f)
            )
            BigButton(
                text = stringResource(R.string.preview_test_match),
                onClick = { soundManager.playMatch() },
                backgroundColor = ButtonSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BigButton(
                text = stringResource(R.string.preview_test_mismatch),
                onClick = { soundManager.playMismatch() },
                backgroundColor = ButtonSecondary,
                modifier = Modifier.weight(1f)
            )
            BigButton(
                text = stringResource(R.string.preview_test_win),
                onClick = { soundManager.playWin() },
                backgroundColor = PackColors.ZooPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BigButton(
                text = stringResource(R.string.preview_test_star),
                onClick = { soundManager.playStar() },
                backgroundColor = PackColors.FarmPrimary,
                modifier = Modifier.weight(1f)
            )
            BigButton(
                text = stringResource(R.string.preview_test_tts),
                onClick = { speaker.speakName("Lion!") },
                backgroundColor = PackColors.BirdsPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        BigButton(
            text = if (isMusicPlaying) "Pause Background Music" else stringResource(R.string.preview_toggle_music),
            onClick = {
                isMusicPlaying = !isMusicPlaying
                musicManager.setEnabled(isMusicPlaying)
            },
            backgroundColor = if (isMusicPlaying) ButtonPrimary else ButtonSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. StarRow Showcase
        Text(
            text = stringResource(R.string.preview_stars_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("1 Star", style = MaterialTheme.typography.bodyMedium)
                StarRow(stars = 1, starSize = 36.dp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("2 Stars", style = MaterialTheme.typography.bodyMedium)
                StarRow(stars = 2, starSize = 36.dp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("3 Stars", style = MaterialTheme.typography.bodyMedium)
                StarRow(stars = 3, starSize = 36.dp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Card Shapes & Pack Colors
        Text(
            text = stringResource(R.string.preview_pack_colors),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        val packs = listOf(
            Triple(stringResource(R.string.pack_zoo), PackColors.ZooPrimary, PackColors.ZooDark),
            Triple(stringResource(R.string.pack_farm), PackColors.FarmPrimary, PackColors.FarmDark),
            Triple(stringResource(R.string.pack_sea), PackColors.SeaPrimary, PackColors.SeaDark),
            Triple(stringResource(R.string.pack_birds), PackColors.BirdsPrimary, PackColors.BirdsDark),
            Triple(stringResource(R.string.pack_fruits), PackColors.FruitsPrimary, PackColors.FruitsDark),
            Triple(stringResource(R.string.pack_vegetables), PackColors.VegetablesPrimary, PackColors.VegetablesDark),
            Triple(stringResource(R.string.pack_vehicles), PackColors.VehiclesPrimary, PackColors.VehiclesDark),
            Triple(stringResource(R.string.pack_shapes_colors), PackColors.ShapesColorsPrimary, PackColors.ShapesColorsDark),
            Triple(stringResource(R.string.pack_allmix), PackColors.AllMixPrimary, PackColors.AllMixDark)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            packs.forEach { (name, primary, dark) ->
                PackCardSample(name = name, primaryColor = primary, darkColor = dark)
            }
        }
    }
}

@Composable
private fun PackCardSample(
    name: String,
    primaryColor: Color,
    darkColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor, darkColor)
                    )
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = TextLight
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 1000)
@Composable
fun DesignSystemPreviewScreenPreview() {
    KidsMemoryTheme {
        DesignSystemPreviewScreen()
    }
}
