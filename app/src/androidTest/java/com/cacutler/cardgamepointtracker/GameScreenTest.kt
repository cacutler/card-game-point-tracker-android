package com.cacutler.cardgamepointtracker
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cacutler.cardgamepointtracker.data.AppDatabase
import com.cacutler.cardgamepointtracker.repository.GameRepository
import com.cacutler.cardgamepointtracker.ui.screens.GameScreen
import com.cacutler.cardgamepointtracker.ui.viewmodels.GameViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: GameViewModel
    private lateinit var repository: GameRepository
    private lateinit var database: AppDatabase
    private lateinit var gameId: String
    @Before
    fun setup() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        try {// Close any existing database first
            AppDatabase.getDatabase(context).close()
        } catch (e: Exception) {}// Ignore if database wasn't open
        context.deleteDatabase("point_tracker_database")// Clear the database
        AppDatabase.clearInstance()
        kotlinx.coroutines.delay(100)// Small delay to ensure deletion completes
        database = AppDatabase.getDatabase(context)// Get fresh database instance
        repository = GameRepository(database)
        repository.createGame("Test Game", listOf("Alice", "Bob"))// Create a test game
        kotlinx.coroutines.delay(500)
        val games = repository.getActiveGames().first()// Get the MOST RECENT game to ensure we get the one we just created
        if (games.isEmpty()) {
            throw IllegalStateException("No games found after creation")
        }
        gameId = games.maxByOrNull { it.game.id }?.game?.id ?: throw IllegalStateException("Could not get game ID")
        val players = repository.getPlayersForGame(gameId).first()
        if (players.isEmpty()) {
            throw IllegalStateException("No players found for game")
        }
        viewModel = GameViewModel(repository, gameId)
        kotlinx.coroutines.delay(500)
    }
    @After
    fun tearDown() = runBlocking {// IMPORTANT: Cancel ViewModel jobs before closing database
        try {// Give any pending operations a moment to complete
            kotlinx.coroutines.delay(100)
            // If ViewModel has an onCleared method, it will be called
            // when the test finishes, but we need to ensure it happens
            // The ViewModel's viewModelScope will be cancelled automatically
            // when the ViewModel is garbage collected, but we can force it
            database.close()// Close the database
            AppDatabase.clearInstance()// Clear the singleton
            val context = InstrumentationRegistry.getInstrumentation().targetContext// Delete the database file
            context.deleteDatabase("game_database")
            kotlinx.coroutines.delay(100)// Small delay to ensure cleanup completes
        } catch (e: Exception) {
            println("Error during teardown: ${e.message}")
        }
    }
    @Test
    fun gameScreen_displaysPlayers() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        // Give compose time to render
        composeTestRule.waitForIdle()

        // Debug: Print the UI tree
        composeTestRule.onRoot().printToLog("UI_TREE")

        // First, wait for any content to appear (the game name in the top bar)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onAllNodesWithText("Test Game", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                println("TEST: Waiting for Test Game title... ${e.message}")
                false
            }
        }

        // Now wait for Alice
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                val nodes = composeTestRule.onAllNodesWithText("Alice", useUnmergedTree = true)
                    .fetchSemanticsNodes()
                println("TEST: Found ${nodes.size} nodes with 'Alice'")
                nodes.isNotEmpty()
            } catch (e: Exception) {
                println("TEST: Error finding Alice: ${e.message}")
                false
            }
        }

        composeTestRule.onNodeWithText("Alice", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun gameScreen_displaysRound() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Round 1", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Round 1", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun gameScreen_opensScoreSheet_whenPlayerClicked() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        // Wait for players to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Alice", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click on Alice's row
        composeTestRule.onAllNodesWithText("Alice", useUnmergedTree = true)[0].performClick()

        // Verify score sheet is shown
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Quick Add", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Quick Add", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun gameScreen_addsScore_usingQuickAdd() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        // Wait for players to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Alice", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Open score sheet for Alice
        composeTestRule.onAllNodesWithText("Alice", useUnmergedTree = true)[0].performClick()

        // Wait for score sheet to open
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("+10", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click quick add button for 10 points
        composeTestRule.onNodeWithText("+10", useUnmergedTree = true).performClick()

        // Wait for sheet to close and score to update
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("10", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun gameScreen_showsNextRoundDialog() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        // Wait for screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Next Round", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Next Round", useUnmergedTree = true).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Start Next Round?", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Start Next Round?", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Round 1 will be complete and Round 2 will begin.", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun gameScreen_advancesRound_whenConfirmed() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        // Wait for screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Next Round", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Open next round dialog
        composeTestRule.onNodeWithText("Next Round", useUnmergedTree = true).performClick()

        // Wait for dialog
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Next Round", useUnmergedTree = true)
                .fetchSemanticsNodes().size > 1
        }

        // Confirm (use index 1 for the button in the dialog)
        composeTestRule.onAllNodesWithText("Next Round", useUnmergedTree = true)[1].performClick()

        // Verify round changed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Round 2", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun gameScreen_canAddPlayer() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        // Wait for screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("More")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Open menu
        composeTestRule.onNodeWithContentDescription("More").performClick()

        // Wait for menu and click Add Player
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Add Player", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Add Player", useUnmergedTree = true).performClick()

        // Wait for the dialog's Cancel button to appear (unique to dialog)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Cancel", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.waitForIdle()

        // Find and interact with the text field
        val textFields = composeTestRule.onAllNodes(
            hasSetTextAction(),
            useUnmergedTree = true
        )

        // Click and type in the text field
        textFields[0].performClick()
        textFields[0].performTextInput("Charlie")

        composeTestRule.waitForIdle()

        // Click the Add button in the dialog
        // Get all buttons with "Add" text, filter to find the one that's enabled
        val addButtons = composeTestRule.onAllNodesWithText("Add", useUnmergedTree = true)
        val addButtonCount = addButtons.fetchSemanticsNodes().size

        // The dialog Add button should be the last one
        addButtons[addButtonCount - 1].performClick()

        // Verify player appears
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Charlie", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun gameScreen_preventsDeletingLastTwoPlayers() {
        composeTestRule.setContent {
            GameScreen(
                viewModel = viewModel,
                repository = repository,
                onNavigateBack = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitForIdle()

        // Wait for players to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithContentDescription("Remove player")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Try to delete a player (should show warning since only 2 players)
        composeTestRule.onAllNodesWithContentDescription("Remove player")[0].performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Cannot Remove Player", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Cannot Remove Player", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("A game must have at least 2 players. Add more players before removing Alice.", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}