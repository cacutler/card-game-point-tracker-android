package com.cacutler.cardgamepointtracker
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cacutler.cardgamepointtracker.data.AppDatabase
import com.cacutler.cardgamepointtracker.repository.GameRepository
import com.cacutler.cardgamepointtracker.ui.screens.MainScreen
import com.cacutler.cardgamepointtracker.ui.viewmodels.MainViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: GameRepository
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(context)
        repository = GameRepository(database)
        viewModel = MainViewModel(repository)
    }
    @Test
    fun mainScreen_displaysEmptyState_whenNoGames() {
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToGame = {})
        }
        composeTestRule.onNodeWithText("No Games Yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap + to start tracking a new game").assertIsDisplayed()
    }
    @Test
    fun mainScreen_showsNewGameDialog_whenAddButtonClicked() {
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToGame = {})
        }
        composeTestRule.onNodeWithContentDescription("New Game").performClick()// Click the + button
        composeTestRule.onNodeWithText("New Game").assertIsDisplayed()// Verify dialog is shown
        composeTestRule.onNodeWithText("Game Name").assertIsDisplayed()
    }
    @Test
    fun newGameDialog_createsGame_withValidInput() {
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToGame = {})
        }
        composeTestRule.onNodeWithContentDescription("New Game").performClick()// Open dialog
        composeTestRule.onNodeWithText("Game Name").performTextInput("Poker Night")// Enter game name
        composeTestRule.onAllNodesWithText("Player 1")[0].performTextInput("Alice")// Enter player names
        composeTestRule.onAllNodesWithText("Player 2")[0].performTextInput("Bob")
        composeTestRule.onNodeWithText("Create").performClick()// Create game
        composeTestRule.waitUntil(timeoutMillis = 2000) {// Verify dialog is dismissed and game appears
            composeTestRule.onAllNodesWithText("Poker Night").fetchSemanticsNodes().isNotEmpty()
        }
    }
    @Test
    fun newGameDialog_showsError_withInvalidInput() {
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToGame = {})
        }
        composeTestRule.onNodeWithContentDescription("New Game").performClick()
        composeTestRule.onNodeWithText("Create").performClick()// Try to create without filling fields
        composeTestRule.onNodeWithText("Please enter a game name and at least 2 unique player names").assertIsDisplayed()// Verify error message
    }
    @Test
    fun newGameDialog_canAddMorePlayers() {
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onNavigateToGame = {})
        }
        composeTestRule.onNodeWithContentDescription("New Game").performClick()
        composeTestRule.onNodeWithText("Add Player").performClick()// Add a third player
        composeTestRule.onNodeWithText("Player 3").assertIsDisplayed()// Verify third player field exists
    }
}