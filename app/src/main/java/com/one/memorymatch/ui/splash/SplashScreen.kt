package com.one.memorymatch.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.ui.components.MascotBrain
import com.one.memorymatch.ui.theme.ButtonPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    packRepository: PackRepository? = null,
    soundManager: SoundManager? = null
) {
    val context = LocalContext.current
    val logoScale = remember { Animatable(0.2f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo bouncy entrance
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Text fade in
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
    }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        // Preload manifest and sounds in parallel
        withContext(Dispatchers.IO) {
            try {
                packRepository ?: PackRepository(context)
                soundManager ?: SoundManager(context)
            } catch (e: Exception) {
                System.err.println("KidsMemory: Splash preload error: ${e.message}")
            }
        }

        val elapsed = System.currentTimeMillis() - startTime
        val remainingDelay = (1200L - elapsed).coerceAtLeast(300L)
        delay(remainingDelay)

        onSplashFinished()
    }

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
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            MascotBrain(
                modifier = Modifier.scale(logoScale.value),
                size = 180.dp,
                showCards = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ButtonPrimary,
                    fontSize = 36.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(logoScale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0288D1),
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(textAlpha.value)
            )
        }
    }
}
