package com.one.memorymatch

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.one.memorymatch.data.model.Card
import com.one.memorymatch.data.model.CardItem
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.ui.game.GameScreen
import com.one.memorymatch.ui.game.GameViewModel
import com.one.memorymatch.ui.navigation.AppNavHost
import com.one.memorymatch.ui.navigation.NavRoutes
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.captureToImage
import com.one.memorymatch.ui.theme.KidsMemoryTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class GameUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var packRepository: PackRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        packRepository = PackRepository(context)
    }

    @Test
    fun game_rendersHudAndTwelveCardsForEasyLevel() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                AppNavHost(startDestination = NavRoutes.game("zoo", "easy"))
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("card_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // HUD elements
        composeTestRule.onNodeWithTag("hud_home_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hud_restart_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hud_pause_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertIsDisplayed()
            .assertTextContains("0 Moves", substring = true)
        composeTestRule.onNodeWithTag("hud_timer_text")
            .assertIsDisplayed()

        // 12 cards for Easy
        for (i in 0 until 12) {
            composeTestRule.onNodeWithTag("memory_card_$i").assertIsDisplayed()
        }
    }

    @Test
    fun game_flipTwoCardsIncrementsMovesAndMismatchFlipsBack() {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository)

        composeTestRule.setContent {
            KidsMemoryTheme {
                GameScreen(
                    packId = "zoo",
                    level = "easy",
                    onNavigateBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("card_grid").fetchSemanticsNodes().isNotEmpty()
        }

        val cards = viewModel.uiState.value.cards
        val firstCard = cards[0]
        // Pick second card that does NOT match the first
        val mismatchCard = cards.drop(1).first { it.itemId != firstCard.itemId }

        // Flip card 1
        composeTestRule.onNodeWithTag("memory_card_${firstCard.uid}").performClick()
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.cards.first { it.uid == firstCard.uid }.isFaceUp)
        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertTextContains("0 Moves", substring = true)

        // Flip card 2 (mismatch)
        composeTestRule.onNodeWithTag("memory_card_${mismatchCard.uid}").performClick()
        composeTestRule.waitForIdle()

        // Moves should increment to 1
        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertTextContains("1 Moves", substring = true)

        // Both are temporarily face up
        assertTrue(viewModel.uiState.value.cards.first { it.uid == firstCard.uid }.isFaceUp)
        assertTrue(viewModel.uiState.value.cards.first { it.uid == mismatchCard.uid }.isFaceUp)

        // Wait past the 900ms mismatch delay
        composeTestRule.waitUntil(timeoutMillis = 4000) {
            val updated = viewModel.uiState.value.cards
            !updated.first { it.uid == firstCard.uid }.isFaceUp &&
                    !updated.first { it.uid == mismatchCard.uid }.isFaceUp
        }

        // Both cards flipped back
        val postDelayCards = viewModel.uiState.value.cards
        assertTrue(!postDelayCards.first { it.uid == firstCard.uid }.isFaceUp)
        assertTrue(!postDelayCards.first { it.uid == mismatchCard.uid }.isFaceUp)
    }

    @Test
    fun game_pauseAndResumeToggleOverlay() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                AppNavHost(startDestination = NavRoutes.game("zoo", "easy"))
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("hud_pause_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Tap pause
        composeTestRule.onNodeWithTag("hud_pause_button").performClick()
        composeTestRule.waitForIdle()

        // Overlay should appear with all 3 big buttons (>= 64dp)
        composeTestRule.onNodeWithTag("game_pause_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_btn_resume").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_btn_restart").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pause_btn_home").assertIsDisplayed()

        // Tap resume button
        composeTestRule.onNodeWithTag("pause_btn_resume").performClick()
        composeTestRule.waitForIdle()

        // Overlay should be gone
        composeTestRule.onAllNodesWithTag("game_pause_overlay").fetchSemanticsNodes().isEmpty()
    }

    @Test
    fun game_pauseOverlay_restartResetsMovesAndUnpauses() {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository)

        composeTestRule.setContent {
            KidsMemoryTheme {
                GameScreen(
                    packId = "zoo",
                    level = "easy",
                    onNavigateBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("card_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // Flip two cards to get moves = 1
        val c0 = viewModel.uiState.value.cards[0]
        val c1 = viewModel.uiState.value.cards[1]
        composeTestRule.onNodeWithTag("memory_card_${c0.uid}").performClick()
        composeTestRule.onNodeWithTag("memory_card_${c1.uid}").performClick()
        composeTestRule.waitForIdle()

        // Pause
        composeTestRule.onNodeWithTag("hud_pause_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("game_pause_overlay").assertIsDisplayed()

        // Tap Restart from Pause Overlay
        composeTestRule.onNodeWithTag("pause_btn_restart").performClick()
        composeTestRule.waitForIdle()

        // Overlay should be gone, moves = 0, paused = false
        composeTestRule.onAllNodesWithTag("game_pause_overlay").fetchSemanticsNodes().isEmpty()
        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertTextContains("0 Moves", substring = true)
        assertTrue(!viewModel.uiState.value.isPaused)
    }

    @Test
    fun game_pauseOverlay_homeNavigatesBack() {
        var navigatedBack = false
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository)

        composeTestRule.setContent {
            KidsMemoryTheme {
                GameScreen(
                    packId = "zoo",
                    level = "easy",
                    onNavigateBack = { navigatedBack = true },
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("hud_pause_button").fetchSemanticsNodes().isNotEmpty()
        }

        // Pause
        composeTestRule.onNodeWithTag("hud_pause_button").performClick()
        composeTestRule.waitForIdle()

        // Tap Home from Pause Overlay
        composeTestRule.onNodeWithTag("pause_btn_home").performClick()
        composeTestRule.waitForIdle()

        assertTrue(navigatedBack)
    }

    @Test
    fun game_rotation_retainsGameStateMidGame() {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository)

        // Start in game and flip a card
        val c0 = viewModel.uiState.value.cards[0]
        viewModel.onCardClicked(c0.uid)
        assertTrue(viewModel.uiState.value.cards.first { it.uid == c0.uid }.isFaceUp)

        var orientation by mutableStateOf(Configuration.ORIENTATION_PORTRAIT)

        composeTestRule.setContent {
            val config = Configuration(LocalConfiguration.current).apply {
                this.orientation = orientation
            }
            CompositionLocalProvider(LocalConfiguration provides config) {
                KidsMemoryTheme {
                    GameScreen(
                        packId = "zoo",
                        level = "easy",
                        onNavigateBack = {},
                        viewModel = viewModel
                    )
                }
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("card_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // Flip second card to make a move
        val c1 = viewModel.uiState.value.cards[1]
        composeTestRule.onNodeWithTag("memory_card_${c1.uid}").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, viewModel.uiState.value.moves)

        // Pause the game
        viewModel.setPaused(true)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("game_pause_overlay").assertIsDisplayed()

        // Capture pre-rotation state
        val preState = viewModel.uiState.value
        assertEquals(1, preState.moves)
        assertTrue(preState.isPaused)
        assertEquals(12, preState.cards.size)

        // Rotate to landscape by updating orientation state
        orientation = Configuration.ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("game_pause_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertTextContains("1 Moves", substring = true)

        val postState = viewModel.uiState.value
        assertEquals(preState.moves, postState.moves)
        assertEquals(preState.isPaused, postState.isPaused)
        assertEquals(preState.cards, postState.cards)
    }

    @Test
    fun game_restartButtonResetsMoves() {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository)

        composeTestRule.setContent {
            KidsMemoryTheme {
                GameScreen(
                    packId = "zoo",
                    level = "easy",
                    onNavigateBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("card_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // Flip two cards to get moves = 1
        val c0 = viewModel.uiState.value.cards[0]
        val c1 = viewModel.uiState.value.cards[1]
        composeTestRule.onNodeWithTag("memory_card_${c0.uid}").performClick()
        composeTestRule.onNodeWithTag("memory_card_${c1.uid}").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertTextContains("1 Moves", substring = true)

        // Click restart
        composeTestRule.onNodeWithTag("hud_restart_button").performClick()
        composeTestRule.waitForIdle()

        // Moves should be 0
        composeTestRule.onNodeWithTag("hud_moves_text")
            .assertTextContains("0 Moves", substring = true)
    }

    @Test
    fun game_winFlow_showsWinDialogAndStarsAndCanPlayAgain() {
        val customDeck = listOf(
            Card(uid = 0, itemId = "lion"),
            Card(uid = 1, itemId = "lion")
        )
        val viewModel = GameViewModel(
            packId = "zoo",
            level = GameLevel.EASY,
            packRepository = packRepository,
            customDeck = customDeck,
            startTimerImmediately = false
        )

        composeTestRule.setContent {
            KidsMemoryTheme {
                GameScreen(
                    packId = "zoo",
                    level = "easy",
                    onNavigateBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("card_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // Tap both cards of the single pair
        composeTestRule.onNodeWithTag("memory_card_0").performClick()
        composeTestRule.onNodeWithTag("memory_card_1").performClick()
        composeTestRule.waitForIdle()

        // Verify WinDialog and components are displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("win_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("win_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("win_stars_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("win_stats_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_play_again").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_choose_pack").assertIsDisplayed()
        composeTestRule.onNodeWithTag("win_close_button").assertIsDisplayed()
        // Capture screenshot of WinDialog
        try {
            val bitmap = composeTestRule.onNodeWithTag("game_screen").captureToImage().asAndroidBitmap()
            val extFile = File(context.getExternalFilesDir(null), "m10_win_dialog.png")
            FileOutputStream(extFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
            val sdFile = File("/sdcard/m10_win_dialog.png")
            FileOutputStream(sdFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Throwable) {
            println("Screenshot capture: ${e.message}")
        }

        // Tap Play Again
        composeTestRule.onNodeWithTag("btn_play_again").performClick()
        composeTestRule.waitForIdle()

        // Win dialog should dismiss and cards should be face down
        composeTestRule.onAllNodesWithTag("win_dialog").fetchSemanticsNodes().isEmpty()
        val cards = viewModel.uiState.value.cards
        assertTrue(cards.all { !it.isFaceUp && !it.isMatched })
        assertEquals(0, viewModel.uiState.value.moves)
    }
}
