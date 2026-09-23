package com.one.memorymatch.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.ButtonSecondary
import com.one.memorymatch.ui.theme.ButtonSuccess
import com.one.memorymatch.ui.theme.TextDark
import com.one.memorymatch.ui.theme.TextMuted

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
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

    val overlayCd = stringResource(R.string.cd_pause_overlay)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .semantics { contentDescription = overlayCd }
            .testTag("game_pause_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .scale(dialogScale.value)
                .padding(24.dp)
                .widthIn(min = 280.dp, max = 380.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Text(
                    text = "⏸️ " + stringResource(R.string.pause_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 28.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.pause_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        fontSize = 17.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons (all >= 64dp touch height per MASTER_RULES)
                BigButton(
                    text = stringResource(R.string.pause_resume),
                    backgroundColor = ButtonSuccess,
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_btn_resume")
                )

                BigButton(
                    text = stringResource(R.string.pause_restart),
                    backgroundColor = ButtonSecondary,
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_btn_restart")
                )

                BigButton(
                    text = stringResource(R.string.pause_home),
                    backgroundColor = ButtonPrimary,
                    onClick = onHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_btn_home")
                )
            }
        }
    }
}
