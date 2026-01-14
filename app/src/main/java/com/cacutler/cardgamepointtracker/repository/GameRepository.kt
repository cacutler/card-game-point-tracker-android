package com.cacutler.cardgamepointtracker.repository
import com.cacutler.cardgamepointtracker.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
class GameRepository(private val database: AppDatabase) {
    private val gameDao = database.gameDao()
    private val playerDao = database.playerDao()
    private val scoreEntryDao = database.scoreEntryDao()
    fun getActiveGames(): Flow<List<GameWithPlayers>> = gameDao.getActiveGames()//Game operations
    fun getCompletedGames(): Flow<List<GameWithPlayers>> = gameDao.getCompletedGames()
    fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers?> = gameDao.getGameWithPlayers(gameId)
    suspend fun createGame(name: String, playerNames: List<String>) {
        val game = Game(name = name)
        gameDao.insertGame(game)
        val players = playerNames.map {playerName ->
            Player(gameId = game.id, name = playerName)
        }
        playerDao.insertPlayers(players)
    }
    suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game)
    }
    suspend fun nextRound(gameId: String) {
        gameDao.nextRound(gameId)
    }
    suspend fun endGame(gameId: String) {
        gameDao.endGame(gameId)
    }
    suspend fun resetGame(gameId: String) {
        scoreEntryDao.deleteAllScoresForGame(gameId)
        playerDao.resetScores(gameId)
        gameDao.resetGame(gameId)
    }
    fun getPlayersForGame(gameId: String): Flow<List<Player>> = playerDao.getPlayersForGame(gameId)//Player operations
    suspend fun addPlayer(gameId: String, name: String) {
        val player = Player(gameId = gameId, name = name)
        playerDao.insertPlayer(player)
    }
    suspend fun removePlayer(player: Player) {
        playerDao.deletePlayer(player)
    }
    suspend fun addPoints(playerId: String, points: Int, round: Int) {//Score operations
        val scoreEntry = ScoreEntry(playerId = playerId, points = points, round = round)
        scoreEntryDao.insertScoreEntry(scoreEntry)
        playerDao.addPoints(playerId, points)
    }
    suspend fun undoLastScore(playerId: String) {
        val lastEntry = scoreEntryDao.getLastScoreEntry(playerId) ?: return
        scoreEntryDao.deleteScoreEntry(lastEntry)
        playerDao.addPoints(playerId, -lastEntry.points)
    }
    fun getScoreHistory(playerId: String): Flow<List<ScoreEntry>> = scoreEntryDao.getScoreHistory(playerId)
    fun getScoresForRound(playerId: String, round: Int): Flow<List<ScoreEntry>> = scoreEntryDao.getScoresForRound(playerId, round)
    suspend fun getWinner(gameId: String): Player? {//Helper functions
        val players = playerDao.getPlayersForGame(gameId).first()
        return players.maxByOrNull {it.score}
    }
    suspend fun getTotalForRound(playerId: String, round: Int): Int {
        val scores = scoreEntryDao.getScoresForRound(playerId, round).first()
        return scores.sumOf {it.points}
    }
}