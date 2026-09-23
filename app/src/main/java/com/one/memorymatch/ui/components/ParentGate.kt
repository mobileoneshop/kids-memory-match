package com.one.memorymatch.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.ui.theme.ButtonPrimary
import com.one.memorymatch.ui.theme.ButtonSecondary
import com.one.memorymatch.ui.theme.WarningRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class MathChallenge(
    val num1: Int,
    val num2: Int,
    val options: List<Int>,
    val answer: Int
) {
    companion object {
        fun generate(random: Random = Random): MathChallenge {
            val n1 = random.nextInt(1, 10)
            val n2 = random.nextInt(1, 11 - n1.coerceAtMost(10)) // ensures sum <= 20
            val ans = n1 + n2

            val wrongAnswers = mutableSetOf<Int>()
            while (wrongAnswers.size < 2) {
                val candidate = random.nextInt(2, 21)
                if (candidate != ans) {
                    wrongAnswers.add(candidate)
                }
            }

            val opts = (wrongAnswers.toList() + ans).shuffled(random)
            return MathChallenge(num1 = n1, num2 = n2, options = opts, answer = ans)
        }
    }
}

@Composable
fun ParentGate(
    onPass: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialChallenge: MathChallenge = remember { MathChallenge.generate() }
) {
    var challenge by remember(initialChallenge) { mutableStateOf(initialChallenge) }
    var hasError by remember(initialChallenge) { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("parent_gate_dialog"),
        contentAlignment = Alignment.Center
    ) {
        // Scrim overlay to dim background and block clicks from passing through
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // block outside clicks
                )
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.parent_gate_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ButtonPrimary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.parent_gate_prompt),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF616161)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F4F8))
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.parent_gate_question,
                            challenge.num1,
                            challenge.num2
                        ),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238),
                            fontSize = 32.sp
                        ),
                        modifier = Modifier.testTag("parent_gate_question")
                    )
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.parent_gate_try_again),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = WarningRed,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.testTag("parent_gate_error")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    challenge.options.forEach { option ->
                        Box(modifier = Modifier.weight(1f)) {
                            BigButton(
                                text = option.toString(),
                                onClick = {
                                    if (option == challenge.answer) {
                                        hasError = false
                                        onPass()
                                    } else {
                                        hasError = true
                                        coroutineScope.launch {
                                            shakeOffset.snapTo(0f)
                                            shakeOffset.animateTo(
                                                targetValue = 0f,
                                                animationSpec = keyframes {
                                                    durationMillis = 400
                                                    -20f at 50
                                                    20f at 100
                                                    -15f at 150
                                                    15f at 200
                                                    -10f at 250
                                                    10f at 300
                                                    0f at 400
                                                }
                                            )
                                        }
                                        challenge = MathChallenge.generate()
                                    }
                                },
                                backgroundColor = ButtonSecondary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("parent_gate_option_$option")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BigButton(
                    text = stringResource(R.string.parent_gate_close),
                    onClick = onDismiss,
                    backgroundColor = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_gate_close")
                )
            }
        }
    }
}

// Backward compatibility alias
@Composable
fun ParentGateDialog(
    onPass: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialChallenge: MathChallenge = remember { MathChallenge.generate() }
) {
    ParentGate(
        onPass = onPass,
        onDismiss = onDismiss,
        modifier = modifier,
        initialChallenge = initialChallenge
    )
}
