package com.cacutler.cardgamepointtracker
import com.cacutler.cardgamepointtracker.data.*
import com.cacutler.cardgamepointtracker.repository.GameRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class GameRepositoryTest {
    private lateinit var repository: GameRepository
    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao
    private lateinit var playerDao: PlayerDao
    private lateinit var scoreEntryDao: ScoreEntryDao
    @Before
    fun setup() {
        database = mockk()//Create mocks
        gameDao = mockk()
        playerDao = mockk()
        scoreEntryDao = mockk()
        every {database.gameDao()} returns gameDao//Setup database to return DAOs
        every {database.playerDao()} returns playerDao
        every {database.scoreEntryDao()} returns scoreEntryDao
        repository = GameRepository(database)
    }
    @After
    fun tearDown() {
        unmockkAll()
    }
    @Test
    fun `createGame should insert game and players`() = runTest {
        val gameName = "Test Game"//Arrange
        val playerNames = listOf("Alice", "Bob", "Charlie")
        coEvery {gameDao.insertGame(any())} just Runs
        coEvery {playerDao.insertPlayers(any())} just Runs
        repository.createGame(gameName, playerNames)//Act
        coVerify(exactly = 1) {gameDao.insertGame(match {it.name == gameName})}//Assert
        coVerify(exactly = 1) {playerDao.insertPlayers(match {it.size == 3})}
    }
    @Test
    fun `addPoints should create score entry and update player score`() = runTest {
        val playerId = "player123"//Arrange
        val points = 50
        val round = 2
        coEvery {scoreEntryDao.insertScoreEntry(any())} just Runs
        coEvery {playerDao.addPoints(playerId, points)} just Runs
        repository.addPoints(playerId, points, round)//Act
        coVerify {scoreEntryDao.insertScoreEntry(match {it.playerId == playerId && it.points == points && it.round == round})}//Assert
        coVerify {playerDao.addPoints(playerId, points)}
    }
    @Test
    fun `undoLastScore should delete entry and subtract points`() = runTest {
        val playerId = "player123"//Arrange
        val lastEntry = ScoreEntry(id = "entry1", playerId = playerId, points = 30, round = 1)
        coEvery {scoreEntryDao.getLastScoreEntry(playerId)} returns lastEntry
        coEvery {scoreEntryDao.deleteScoreEntry(any())} just Runs
        coEvery {playerDao.addPoints(any(), any())} just Runs
        repository.undoLastScore(playerId)//Act
        coVerify {scoreEntryDao.deleteScoreEntry(lastEntry)}//Assert
        coVerify {playerDao.addPoints(playerId, -30)}
    }
    @Test
    fun `undoLastScore should do nothing when no last entry`() = runTest {
        val playerId = "player123"//Arrange
        coEvery {scoreEntryDao.getLastScoreEntry(playerId)} returns null
        repository.undoLastScore(playerId)//Act
        coVerify(exactly = 0) {scoreEntryDao.deleteScoreEntry(any())}//Assert
        coVerify(exactly = 0) {playerDao.addPoints(any(), any())}
    }
    @Test
    fun `getWinner should return player with highest score`() = runTest {
        val gameId = "game123"//Arrange
        val players = listOf(Player(id = "p1", gameId = gameId, name = "Alice", score = 100), Player(id = "p2", gameId = gameId, name = "Bob", score = 150), Player(id = "p3", gameId = gameId, name = "Charlie", score = 75))
        every {playerDao.getPlayersForGame(gameId)} returns flowOf(players)
        val winner = repository.getWinner(gameId)//Act
        assertNotNull(winner)//Assert
        assertEquals("Bob", winner?.name)
        assertEquals(150, winner?.score)
    }
    @Test
    fun `resetGame should clear scores and reset game state`() = runTest {
        val gameId = "game123"//Arrange
        coEvery {scoreEntryDao.deleteAllScoresForGame(gameId)} just Runs
        coEvery {playerDao.resetScores(gameId)} just Runs
        coEvery {gameDao.resetGame(gameId)} just Runs
        repository.resetGame(gameId)//Act
        coVerifyOrder {//Assert
            scoreEntryDao.deleteAllScoresForGame(gameId)
            playerDao.resetScores(gameId)
            gameDao.resetGame(gameId)
        }
    }
}