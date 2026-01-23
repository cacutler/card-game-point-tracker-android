package com.cacutler.cardgamepointtracker
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cacutler.cardgamepointtracker.data.AppDatabase
import com.cacutler.cardgamepointtracker.data.Player
import com.cacutler.cardgamepointtracker.repository.GameRepository
import com.cacutler.cardgamepointtracker.ui.screens.ScoreEntrySheet
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
@RunWith(AndroidJUnit4::class)
class ScoreEntrySheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var repository: GameRepository
    private lateinit var testPlayer: Player
    private lateinit var database: AppDatabase
    @Before
    fun setup() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(context)
        repository = GameRepository(database)
        repository.createGame("Test Game", listOf("Alice"))// Create the game (returns Unit, not the ID)
        val game = repository.getActiveGames().first().firstOrNull() ?: throw IllegalStateException("Game was not created")// Get the game that was just created
        val players = repository.getPlayersForGame(game.game.id).first()// Now get the players using the actual game ID
        testPlayer = players.first()
    }
    @Test
    fun scoreEntrySheet_displaysPlayerInfo() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Round 1").assertIsDisplayed()
    }
    @Test
    fun scoreEntrySheet_hasAddAndSubtractOptions() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onNodeWithText("Add Points").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subtract Points").assertIsDisplayed()
    }
    @Test
    fun scoreEntrySheet_switchesToSubtract() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onNodeWithText("Subtract Points").performClick()// Click subtract
        composeTestRule.onNodeWithText("-10").assertIsDisplayed()// Verify quick add buttons show negative
    }
    @Test
    fun scoreEntrySheet_hasQuickAddButtons() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onNodeWithText("+1").assertIsDisplayed()
        composeTestRule.onNodeWithText("+5").assertIsDisplayed()
        composeTestRule.onNodeWithText("+10").assertIsDisplayed()
        composeTestRule.onNodeWithText("+25").assertIsDisplayed()
        composeTestRule.onNodeWithText("+50").assertIsDisplayed()
    }
    @Test
    fun scoreEntrySheet_acceptsManualInput() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onNodeWithText("Points").performTextInput("15")// Enter points
        composeTestRule.onAllNodesWithText("Add")[0].assertIsEnabled()// Verify Add button is enabled
    }
    @Test
    fun scoreEntrySheet_addButtonDisabled_whenEmpty() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onAllNodesWithText("Add")[0].assertIsNotEnabled()
    }
    @Test
    fun scoreEntrySheet_showsUndoButton_afterAddingScore() = runBlocking {
        repository.addPoints(testPlayer.id, 10, 1)// Add a score first
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Undo Last (+10)").fetchSemanticsNodes().isNotEmpty()
        }
    }
    @Test
    fun scoreEntrySheet_filtersNonNumericInput() {
        composeTestRule.setContent {
            ScoreEntrySheet(player = testPlayer, currentRound = 1, repository = repository, onDismiss = {})
        }
        composeTestRule.onNodeWithText("Points").performTextInput("abc123def")// Try to enter non-numeric text
        composeTestRule.onAllNodesWithText("Add")[0].assertIsEnabled()// Only numeric characters should remain.  This is harder to test directly, but the button should be enabled if "123" was accepted
    }
}