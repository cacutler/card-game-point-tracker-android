package com.cacutler.cardgamepointtracker
import com.cacutler.cardgamepointtracker.data.*
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class GameDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var gameDao: GameDao
    private lateinit var playerDao: PlayerDao
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        gameDao = database.gameDao()
        playerDao = database.playerDao()
    }
    @After
    fun tearDown() {
        database.close()
    }
    @Test
    fun insertAndRetrieveGame() = runTest {
        val game = Game(id = "game1", name = "Test Game", isActive = true)// Arrange
        gameDao.insertGame(game)// Act
        val games = gameDao.getActiveGames().first()
        assertEquals(1, games.size)// Assert
        assertEquals("Test Game", games[0].game.name)
    }
    @Test
    fun getGameWithPlayers() = runTest {
        val game = Game(id = "game1", name = "Test Game")// Arrange
        val players = listOf(Player(id = "p1", gameId = "game1", name = "Alice"), Player(id = "p2", gameId = "game1", name = "Bob"))
        gameDao.insertGame(game)// Act
        playerDao.insertPlayers(players)
        val result = gameDao.getGameWithPlayers("game1").first()
        assertNotNull(result)// Assert
        assertEquals(2, result?.players?.size)
        assertEquals("Alice", result?.players?.get(0)?.name)
    }
    @Test
    fun nextRoundIncrementsRound() = runTest {
        val game = Game(id = "game1", name = "Test", currentRound = 1)// Arrange
        gameDao.insertGame(game)
        gameDao.nextRound("game1")// Act
        val updatedGame = gameDao.getGameWithPlayers("game1").first()
        assertEquals(2, updatedGame?.game?.currentRound)// Assert
    }
    @Test
    fun endGameSetsInactive() = runTest {
        val game = Game(id = "game1", name = "Test", isActive = true)// Arrange
        gameDao.insertGame(game)
        gameDao.endGame("game1")// Act
        val activeGames = gameDao.getActiveGames().first()
        val completedGames = gameDao.getCompletedGames().first()
        assertEquals(0, activeGames.size)// Assert
        assertEquals(1, completedGames.size)
    }
}