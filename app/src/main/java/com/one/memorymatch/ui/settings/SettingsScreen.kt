package com.one.memorymatch.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.one.memorymatch.R
import com.one.memorymatch.audio.MusicManager
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.components.BigButton
import com.one.memorymatch.ui.components.FloatingCloudsBackground
import com.one.memorymatch.ui.components.MathChallenge
import com.one.memorymatch.ui.components.ParentGateDialog
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.ButtonSecondary
import com.one.memorymatch.ui.theme.ButtonSuccess
import com.one.memorymatch.ui.theme.WarningRed

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel? = null,
    soundManager: SoundManager? = null,
    musicManager: MusicManager? = null,
    initialParentGateChallenge: MathChallenge? = null
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel ?: run {
        val repo = remember { ProgressRepository(context) }
        val sm = remember { soundManager ?: SoundManager(context) }
        val mm = remember { musicManager ?: MusicManager(context) }
        val factory = remember { SettingsViewModel.Factory(repo, sm, mm) }
        viewModel(factory = factory)
    }

    val uiState by settingsViewModel.uiState.collectAsState()

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
            .testTag("settings_screen")
    ) {
        FloatingCloudsBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val backBtnInteraction = remember { MutableInteractionSource() }
                val backBtnPressed by backBtnInteraction.collectIsPressedAsState()
                val backBtnScale by animateFloatAsState(
                    targetValue = if (backBtnPressed) 0.90f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "back_btn_scale"
                )
                val backCd = stringResource(R.string.cd_settings_back)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(backBtnScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .semantics { contentDescription = backCd }
                        .clickable(
                            interactionSource = backBtnInteraction,
                            indication = null,
                            onClick = onBack
                        )
                        .testTag("btn_back"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ButtonPrimary
                    )
                }

                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ButtonPrimary,
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.testTag("settings_title")
                )

                Spacer(modifier = Modifier.size(52.dp))
            }

            // Success feedback banner
            AnimatedVisibility(
                visible = uiState.resetSuccess,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ButtonSuccess),
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("settings_reset_success_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✅ " + stringResource(R.string.settings_reset_success),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Section 1: Audio Controls
            SettingsCard(
                title = stringResource(R.string.settings_audio_title),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                // Sound Effects Toggle
                SettingsToggleRow(
                    icon = "🔊",
                    title = stringResource(R.string.settings_sound),
                    subtitle = stringResource(R.string.settings_sound_desc),
                    checked = uiState.soundEnabled,
                    onCheckedChange = { settingsViewModel.toggleSound(it) },
                    switchTag = "settings_sound_toggle"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Background Music Toggle
                SettingsToggleRow(
                    icon = "🎵",
                    title = stringResource(R.string.settings_music),
                    subtitle = stringResource(R.string.settings_music_desc),
                    checked = uiState.musicEnabled,
                    onCheckedChange = { settingsViewModel.toggleMusic(it) },
                    switchTag = "settings_music_toggle"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Language
            SettingsCard(
                title = stringResource(R.string.settings_language_title),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F8E9))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🌐", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = stringResource(R.string.settings_language_en),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = stringResource(R.string.settings_language_coming_soon),
                                fontSize = 11.sp,
                                color = Color(0xFF757575),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ButtonSuccess)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_language_active),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Reset Progress
            SettingsCard(
                title = stringResource(R.string.settings_reset_title),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_desc),
                    fontSize = 15.sp,
                    color = Color(0xFF616161)
                )

                Spacer(modifier = Modifier.height(16.dp))

                BigButton(
                    text = stringResource(R.string.settings_reset_button),
                    onClick = { settingsViewModel.requestResetProgress() },
                    backgroundColor = WarningRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_reset_button")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: About
            SettingsCard(
                title = stringResource(R.string.settings_about_title),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.settings_about_version),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF37474F)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.settings_about_tagline),
                        fontSize = 14.sp,
                        color = Color(0xFF546E7A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.settings_about_safety),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF78909C),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Confirmation Dialog (Double confirm step 1)
        if (uiState.showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { settingsViewModel.dismissResetConfirm() },
                title = {
                    Text(
                        text = stringResource(R.string.settings_reset_confirm_title),
                        fontWeight = FontWeight.Bold,
                        color = WarningRed,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.settings_reset_confirm_message),
                        fontSize = 16.sp,
                        color = Color(0xFF424242)
                    )
                },
                confirmButton = {
                    BigButton(
                        text = stringResource(R.string.settings_reset_confirm_proceed),
                        onClick = { settingsViewModel.confirmResetRequiresGate() },
                        backgroundColor = WarningRed,
                        modifier = Modifier.testTag("settings_dialog_confirm")
                    )
                },
                dismissButton = {
                    BigButton(
                        text = stringResource(R.string.settings_reset_confirm_cancel),
                        onClick = { settingsViewModel.dismissResetConfirm() },
                        backgroundColor = Color(0xFF9E9E9E),
                        modifier = Modifier.testTag("settings_dialog_cancel")
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }

        // ParentGate Dialog (Double confirm step 2)
        if (uiState.showResetParentGate) {
            ParentGateDialog(
                initialChallenge = initialParentGateChallenge ?: remember { MathChallenge.generate() },
                onPass = {
                    settingsViewModel.onResetGatePassed()
                },
                onDismiss = {
                    settingsViewModel.dismissResetGate()
                }
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238),
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF263238)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ButtonSuccess,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFBDBDBD)
            ),
            modifier = Modifier.testTag(switchTag)
        )
    }
}
