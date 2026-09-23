package com.one.memorymatch.ui.game

import android.graphics.BitmapFactory
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.memorymatch.R
import com.one.memorymatch.data.model.Card
import com.one.memorymatch.data.model.CardItem
import com.one.memorymatch.data.model.CardPack

@Composable
fun MemoryCard(
    card: Card,
    item: CardItem?,
    pack: CardPack?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "card_press_scale"
    )

    // 3D flip animation between 0 (back) and 180 (front)
    val isFlipped = card.isFaceUp || card.isMatched
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "card_flip_rotation"
    )

    val density = LocalDensity.current.density
    val cardContentDescription = stringResource(R.string.cd_game_card, card.uid)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(pressScale)
            .semantics { contentDescription = cardContentDescription }
            .testTag("memory_card_${card.uid}")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !card.isFaceUp && !card.isMatched,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            CardBack(
                pack = pack,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
            )
        } else {
            CardFront(
                card = card,
                item = item,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation - 180f
                        cameraDistance = 12f * density
                    }
            )
        }
    }
}

@Composable
fun CardBack(
    pack: CardPack?,
    modifier: Modifier = Modifier
) {
    val primaryColor = if (pack != null) Color(pack.primaryColor) else Color(0xFF4CAF50)
    val darkColor = if (pack != null) Color(pack.darkColor) else Color(0xFF2E7D32)

    BoxWithConstraints(modifier = modifier.testTag("card_back")) {
        val cardSize = minOf(maxWidth, maxHeight)
        val cornerRadius = minOf(16.dp, maxOf(8.dp, cardSize * 0.16f))
        val borderWidth = if (cardSize < 70.dp) 1.5.dp else 2.5.dp
        val padding = if (cardSize < 70.dp) 4.dp else 7.dp
        val questionMarkFontSize = (cardSize * 0.40f).value.sp

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = primaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(borderWidth, Color.White.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(primaryColor, darkColor)))
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                // Inner decorative circle with Question Mark
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.72f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = questionMarkFontSize
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CardFront(
    card: Card,
    item: CardItem?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Attempt to load bitmap from assets if exists
    val bitmap = remember(item?.imageAsset) {
        val assetPath = item?.imageAsset
        if (assetPath != null) {
            try {
                context.assets.open(assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val borderColor = if (card.isMatched) {
        Color(0xFFFFD54F) // Warm gold for matched cards
    } else {
        Color(0xFFE0E0E0)
    }

    val matchScale by animateFloatAsState(
        targetValue = if (card.isMatched) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 400f),
        label = "card_match_scale"
    )

    BoxWithConstraints(
        modifier = modifier
            .scale(matchScale)
            .testTag("card_front")
    ) {
        val cardSize = minOf(maxWidth, maxHeight)
        val cornerRadius = minOf(16.dp, maxOf(8.dp, cardSize * 0.16f))
        val borderWidth = if (card.isMatched) (if (cardSize < 70.dp) 2.dp else 3.5.dp) else (if (cardSize < 70.dp) 1.dp else 2.dp)
        val innerPadding = if (cardSize < 70.dp) 3.dp else 5.dp
        val textSize = maxOf(8f, minOf(12f, (cardSize * 0.125f).value)).sp
        val emojiSize = (cardSize * 0.38f).value.sp

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = if (card.isMatched) 6.dp else 3.dp),
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (card.isMatched) Color(0xFFFFFDE7) else Color.White
                    )
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (card.isMatched) {
                    Text(
                        text = "✨",
                        fontSize = maxOf(9f, (cardSize * 0.16f).value).sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(1.dp)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = item?.name ?: card.itemId,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(1.dp)
                        )
                    } else {
                        // Graceful fallback: cute emoji + item name
                        val emoji = getItemEmoji(card.itemId)
                        Text(
                            text = emoji,
                            fontSize = emojiSize,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val displayName = item?.name ?: card.itemId.replaceFirstChar { it.uppercase() }
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F),
                            fontSize = textSize
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 1.dp)
                    )
                }
            }
        }
    }
}

/**
 * Friendly emoji fallback map for all 96 manifest items across 8 packs
 */
fun getItemEmoji(itemId: String): String {
    return when (itemId) {
        // Zoo
        "lion" -> "🦁"
        "elephant" -> "🐘"
        "monkey" -> "🐒"
        "zebra" -> "🦓"
        "giraffe" -> "🦒"
        "panda" -> "🐼"
        "tiger" -> "🐯"
        "hippo" -> "🦛"
        "kangaroo" -> "🦘"
        "bear" -> "🐻"
        "crocodile" -> "🐊"
        "penguin" -> "🐧"

        // Farm
        "cow" -> "🐮"
        "horse" -> "🐴"
        "sheep" -> "🐑"
        "pig" -> "🐷"
        "chicken" -> "🐔"
        "goat" -> "🐐"
        "duck" -> "🦆"
        "donkey" -> "🫏"
        "rabbit" -> "🐰"
        "dog" -> "🐶"
        "cat" -> "🐱"
        "rooster" -> "🐓"

        // Sea
        "shark" -> "🦈"
        "dolphin" -> "🐬"
        "octopus" -> "🐙"
        "turtle" -> "🐢"
        "crab" -> "🦀"
        "whale" -> "🐋"
        "seahorse" -> "🦄"
        "starfish" -> "⭐"
        "jellyfish" -> "🪼"
        "seal" -> "🦭"
        "lobster" -> "🦞"
        "clownfish" -> "🐠"

        // Birds
        "parrot" -> "🦜"
        "owl" -> "🦉"
        "flamingo" -> "🦩"
        "peacock" -> "🦚"
        "toucan" -> "🐦"
        "eagle" -> "🦅"
        "duck_bird" -> "🦆"
        "swan" -> "🦢"
        "sparrow" -> "🐤"
        "woodpecker" -> "🪵"
        "pelican" -> "🐦‍⬛"
        "robin" -> "🪶"

        // Fruits
        "apple" -> "🍎"
        "banana" -> "🍌"
        "orange" -> "🍊"
        "strawberry" -> "🍓"
        "grape" -> "🍇"
        "watermelon" -> "🍉"
        "pineapple" -> "🍍"
        "mango" -> "🥭"
        "cherry" -> "🍒"
        "pear" -> "🍐"
        "peach" -> "🍑"
        "lemon" -> "🍋"

        // Vegetables
        "carrot" -> "🥕"
        "broccoli" -> "🥦"
        "tomato" -> "🍅"
        "corn" -> "🌽"
        "cucumber" -> "🥒"
        "potato" -> "🥔"
        "eggplant" -> "🍆"
        "pepper" -> "🫑"
        "peas" -> "🫛"
        "onion" -> "🧅"
        "pumpkin" -> "🎃"
        "avocado" -> "🥑"

        // Vehicles
        "car" -> "🚗"
        "bus" -> "🚌"
        "train" -> "🚂"
        "airplane" -> "✈️"
        "boat" -> "⛵"
        "bicycle" -> "🚲"
        "fire_truck" -> "🚒"
        "police_car" -> "🚓"
        "ambulance" -> "🚑"
        "helicopter" -> "🚁"
        "rocket" -> "🚀"
        "tractor" -> "🚜"

        // Shapes & Colors
        "red_circle" -> "🔴"
        "blue_square" -> "🟦"
        "yellow_triangle" -> "🔺"
        "green_star" -> "⭐"
        "orange_heart" -> "🧡"
        "purple_diamond" -> "💜"
        "pink_oval" -> "🩷"
        "cyan_hexagon" -> "🩵"
        "brown_rectangle" -> "🟫"
        "white_crescent" -> "🌙"
        "gold_star" -> "🌟"
        "rainbow" -> "🌈"

        else -> "✨"
    }
}
