package com.one.memorymatch

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.level.LevelSelectScreen
import com.one.memorymatch.ui.navigation.AppNavHost
import com.one.memorymatch.ui.navigation.NavRoutes
import com.one.memorymatch.ui.theme.KidsMemoryTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LevelSelectUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var progressRepository: ProgressRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        progressRepository = ProgressRepository(context)
        progressRepository.resetAllProgress()
    }

    @Test
    fun levelCardsRenderWithTitlesAndSubtitles() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                LevelSelectScreen(
                    packId = "zoo",
                    onLevelSelected = {},
                    onBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("level_card_easy").fetchSemanticsNodes().isNotEmpty()
        }

        // Header and back button
        composeTestRule.onNodeWithTag("level_select_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_select_pack_stars").assertIsDisplayed()

        // 3 Level Cards
        composeTestRule.onNodeWithTag("level_card_easy").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_card_medium").assertIsDisplayed()
        composeTestRule.onNodeWithTag("level_card_hard").assertIsDisplayed()

        // Default stats when not played
        composeTestRule.onNodeWithTag("level_stats_easy", useUnmergedTree = true)
            .assertTextContains("Not played yet", substring = true)
        composeTestRule.onNodeWithTag("level_stats_medium", useUnmergedTree = true)
            .assertTextContains("Not played yet", substring = true)
        composeTestRule.onNodeWithTag("level_stats_hard", useUnmergedTree = true)
            .assertTextContains("Not played yet", substring = true)
    }

    @Test
    fun storedBestsRenderCorrectly() {
        runBlocking {
            progressRepository.saveGameResult(
                packId = "zoo",
                level = GameLevel.EASY,
                starsEarned = 3,
                moves = 6,
                timeSec = 15
            )
        }

        composeTestRule.setContent {
            KidsMemoryTheme {
                LevelSelectScreen(
                    packId = "zoo",
                    onLevelSelected = {},
                    onBack = {},
                    progressRepository = progressRepository
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("level_card_easy").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify stored best stats are displayed
        composeTestRule.onNodeWithTag("level_stats_easy", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("6 moves • 15s", substring = true)

        // Verify pack stars pill shows 3 / 9
        composeTestRule.onNodeWithTag("level_select_pack_stars")
            .assertIsDisplayed()
            .assertTextContains("3 / 9", substring = true)
    }

    @Test
    fun tappingLevelCardNavigatesToGameRoute() {
        var selectedLevel: GameLevel? = null

        composeTestRule.setContent {
            KidsMemoryTheme {
                LevelSelectScreen(
                    packId = "zoo",
                    onLevelSelected = { level -> selectedLevel = level },
                    onBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("level_card_easy").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("level_card_easy").performClick()
        assertEquals(GameLevel.EASY, selectedLevel)
    }

    @Test
    fun fullNavHost_navigatesToGameAndBack() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                AppNavHost(startDestination = NavRoutes.levelSelect("farm"))
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("level_card_medium").fetchSemanticsNodes().isNotEmpty()
        }

        // Tap medium level card
        composeTestRule.onNodeWithTag("level_card_medium").performClick()

        // Real Game screen should be displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("game_screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("game_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hud_home_button").assertIsDisplayed()

        // Tap back/home button from game HUD
        composeTestRule.onNodeWithTag("hud_home_button").performClick()

        // Should be back on level select screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("level_select_screen").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("level_select_screen").assertIsDisplayed()
    }
}
