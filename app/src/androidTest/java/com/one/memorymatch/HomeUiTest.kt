package com.one.memorymatch

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.one.memorymatch.ui.components.MathChallenge
import com.one.memorymatch.ui.components.ParentGate
import com.one.memorymatch.ui.components.ParentGateDialog
import com.one.memorymatch.ui.home.HomeScreen
import com.one.memorymatch.ui.theme.KidsMemoryTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun packsRenderFromManifest() {
        composeTestRule.setContent {
            KidsMemoryTheme {
                HomeScreen(
                    onPackSelected = {},
                    onOpenSettings = {}
                )
            }
        }

        // Wait until packs have finished loading from manifest
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("pack_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify top bar items
        composeTestRule.onNodeWithTag("home_stars_summary").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_gear_button").assertIsDisplayed()

        // Verify manifest packs and All Mix exist in grid
        val expectedPacks = listOf(
            "zoo", "farm", "sea", "birds", "fruits", "vegetables", "vehicles", "shapes_colors", "allmix"
        )

        for (packId in expectedPacks) {
            assertTrue(
                "Pack tile for $packId must exist in the grid",
                composeTestRule.onAllNodesWithTag("pack_tile_$packId").fetchSemanticsNodes().isNotEmpty()
            )
        }

        // First pack is at the top and clearly displayed
        composeTestRule.onNodeWithTag("pack_tile_zoo").assertIsDisplayed()
    }

    @Test
    fun gearWithoutPassingGateStaysOnHome() {
        var settingsOpened = false

        composeTestRule.setContent {
            KidsMemoryTheme {
                HomeScreen(
                    onPackSelected = {},
                    onOpenSettings = { settingsOpened = true }
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("pack_grid").fetchSemanticsNodes().isNotEmpty()
        }

        // Tap gear button
        composeTestRule.onNodeWithTag("home_gear_button").performClick()

        // Verify parent gate dialog appears
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("parent_gate_dialog").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("parent_gate_dialog").assertIsDisplayed()

        // Dismiss gate without passing (tap Back to Game)
        composeTestRule.onNodeWithTag("parent_gate_close").performClick()

        // Verify dialog is gone and settings was NOT opened (stayed on Home)
        composeTestRule.waitForIdle()
        assertTrue(
            "Parent gate dialog must be dismissed",
            composeTestRule.onAllNodesWithTag("parent_gate_dialog").fetchSemanticsNodes().isEmpty()
        )
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        assertFalse("Settings must not open without passing gate", settingsOpened)
    }

    @Test
    fun parentGate_wrongAnswerDoesNotPass() {
        var passed = false

        val challenge = MathChallenge(
            num1 = 3,
            num2 = 4,
            options = listOf(7, 12, 15),
            answer = 7
        )

        composeTestRule.setContent {
            KidsMemoryTheme {
                ParentGate(
                    initialChallenge = challenge,
                    onPass = { passed = true },
                    onDismiss = {}
                )
            }
        }

        // Click wrong answer 12
        composeTestRule.onNodeWithTag("parent_gate_option_12").performClick()
        assertFalse("Wrong answer must not pass gate", passed)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("parent_gate_error").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("parent_gate_error").assertIsDisplayed()
    }

    @Test
    fun parentGate_correctAnswerPasses() {
        var passed = false

        val challenge = MathChallenge(
            num1 = 3,
            num2 = 4,
            options = listOf(7, 12, 15),
            answer = 7
        )

        composeTestRule.setContent {
            KidsMemoryTheme {
                ParentGate(
                    initialChallenge = challenge,
                    onPass = { passed = true },
                    onDismiss = {}
                )
            }
        }

        // Click correct answer 7
        composeTestRule.onNodeWithTag("parent_gate_option_7").performClick()
        assertTrue("Correct answer must pass gate", passed)
    }
}
