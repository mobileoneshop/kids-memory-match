package com.one.memorymatch

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.components.MathChallenge
import com.one.memorymatch.ui.settings.SettingsScreen
import com.one.memorymatch.ui.settings.SettingsViewModel
import com.one.memorymatch.ui.theme.KidsMemoryTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settings_rendersAllSectionsAndBackNavigates() {
        var backClicked = false

        composeTestRule.setContent {
            KidsMemoryTheme {
                SettingsScreen(
                    onBack = { backClicked = true }
                )
            }
        }

        // Title and Back button
        composeTestRule.onNodeWithTag("settings_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_back").assertIsDisplayed()

        // Audio controls
        composeTestRule.onNodeWithTag("settings_sound_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_music_toggle").assertIsDisplayed()

        // Reset progress button
        composeTestRule.onNodeWithTag("settings_reset_button").assertIsDisplayed()

        // Back button action
        composeTestRule.onNodeWithTag("btn_back").performClick()
        assertTrue("Back click should trigger callback", backClicked)
    }

    @Test
    fun settings_audioTogglesSwitchState() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                SettingsScreen(
                    onBack = {}
                )
            }
        }

        // Initially both are On
        composeTestRule.onNodeWithTag("settings_sound_toggle").assertIsOn()
        composeTestRule.onNodeWithTag("settings_music_toggle").assertIsOn()

        // Toggle sound Off
        composeTestRule.onNodeWithTag("settings_sound_toggle").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("settings_sound_toggle").assertIsOff()
            }.isSuccess
        }

        // Toggle sound On
        composeTestRule.onNodeWithTag("settings_sound_toggle").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("settings_sound_toggle").assertIsOn()
            }.isSuccess
        }

        // Toggle music Off
        composeTestRule.onNodeWithTag("settings_music_toggle").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            runCatching {
                composeTestRule.onNodeWithTag("settings_music_toggle").assertIsOff()
            }.isSuccess
        }
    }

    @Test
    fun settings_resetProgress_dialogCancelDoesNotOpenGate() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                SettingsScreen(
                    onBack = {}
                )
            }
        }

        // Click Reset
        composeTestRule.onNodeWithTag("settings_reset_button").performClick()

        // Dialog should be displayed
        composeTestRule.onNodeWithTag("settings_dialog_confirm").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_dialog_cancel").assertIsDisplayed()

        // Cancel
        composeTestRule.onNodeWithTag("settings_dialog_cancel").performClick()

        // Dialog dismissed and gate not shown
        composeTestRule.onNodeWithTag("settings_dialog_confirm").assertDoesNotExist()
        composeTestRule.onNodeWithTag("parent_gate_dialog").assertDoesNotExist()
    }

    @Test
    fun settings_resetProgress_proceedAndPassGateResetsProgress() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = ProgressRepository(context)

        // Seed some progress
        runBlocking {
            repo.saveGameResult("zoo", GameLevel.EASY, starsEarned = 3, moves = 6, timeSec = 15)
        }

        val viewModel = SettingsViewModel(repo)
        val fixedChallenge = MathChallenge(
            num1 = 5,
            num2 = 7,
            options = listOf(12, 8, 16),
            answer = 12
        )

        composeTestRule.setContent {
            KidsMemoryTheme {
                SettingsScreen(
                    onBack = {},
                    viewModel = viewModel,
                    initialParentGateChallenge = fixedChallenge
                )
            }
        }

        // Step 1: Click Reset All Progress
        composeTestRule.onNodeWithTag("settings_reset_button").performClick()

        // Step 2: Confirm in AlertDialog
        composeTestRule.onNodeWithTag("settings_dialog_confirm").performClick()

        // Step 3: ParentGate appears
        composeTestRule.onNodeWithTag("parent_gate_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parent_gate_question").assertIsDisplayed()

        // Step 4: Click correct answer 12
        composeTestRule.onNodeWithTag("parent_gate_option_12").performClick()

        // Gate should be closed and success banner displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("parent_gate_dialog").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag("settings_reset_success_banner").assertIsDisplayed()

        // Verify repository progress is reset
        runBlocking {
            val progress = repo.getProgress("zoo", GameLevel.EASY)
            assertEquals(0, progress.stars)
        }
    }
}
