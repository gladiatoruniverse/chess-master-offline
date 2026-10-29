package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appName_isChessMaster_and_homeScreenRendersAllModes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Chess Master", context.getString(R.string.app_name))

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_play_computer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_two_players").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_puzzles").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_learn_chess").assertIsDisplayed()
    }

    @Test
    fun navigateToComputerSetup_and_startMatch_and_puzzlesScreen() {
        composeTestRule.waitForIdle()

        // Navigate to Play vs Computer Setup
        composeTestRule.onNodeWithTag("nav_play_computer").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("computer_setup_screen").assertIsDisplayed()

        // Start a computer game
        composeTestRule.onNodeWithTag("start_computer_game_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("game_board_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chessboard_container").assertIsDisplayed()

        // Return to Home and open Puzzles
        composeTestRule.onNodeWithTag("game_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_puzzles").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("puzzles_screen").assertIsDisplayed()
    }
}
