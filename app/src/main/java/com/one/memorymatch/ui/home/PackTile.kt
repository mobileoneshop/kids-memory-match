package com.one.memorymatch.ui.home

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.data.model.CardPack
import com.one.memorymatch.ui.components.StarIcon
import com.one.memorymatch.ui.theme.StarGold
import java.io.InputStream

@Composable
fun PackTile(
    pack: CardPack,
    totalStars: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "packTileScale"
    )

    val backgroundBrush = remember(pack.id, pack.primaryColor, pack.darkColor) {
        if (pack.isVirtual) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF5252),
                    Color(0xFFFFB300),
                    Color(0xFF4CAF50),
                    Color(0xFF29B6F6),
                    Color(0xFFAB47BC)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(pack.primaryColor),
                    Color(pack.darkColor)
                )
            )
        }
    }

    // Missing asset = graceful fallback (ERROR_HANDLING §1)
    val bitmap = remember(pack.iconAsset) {
        try {
            val stream: InputStream = context.assets.open(pack.iconAsset)
            stream.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    val packName = if (pack.nameRes != 0) {
        stringResource(pack.nameRes)
    } else {
        pack.name
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("pack_tile_${pack.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon / Art container
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_pack_icon),
                            modifier = Modifier.size(56.dp)
                        )
                    } else {
                        // Graceful emoji / badge fallback
                        val emoji = when (pack.id) {
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
                        Text(
                            text = emoji,
                            fontSize = 38.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Pack Name
                Text(
                    text = packName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Stars badge: "⭐ 0 / 9"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                    StarIcon(
                        isEarned = totalStars > 0,
                        size = 14.dp,
                        animated = false
                    )
                        Text(
                            text = stringResource(R.string.pack_stars_format, totalStars),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
