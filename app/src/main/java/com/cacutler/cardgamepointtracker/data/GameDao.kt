package com.cacutler.cardgamepointtracker.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface GameDao {
    @Transaction
    @Query("SELECT * FROM games WHERE isActive = 1 ORDER BY date DESC")
    fun getActiveGames(): Flow<List<GameWithPlayers>>
    @Transaction
    @Query("SELECT * FROM games WHERE isActive = 0 ORDER BY date DESC")
    fun getCompletedGames(): Flow<List<GameWithPlayers>>
    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameWithPlayers(gameId: String): Flow<GameWithPlayers?>
    @Insert
    suspend fun insertGame(game: Game)
    @Update
    suspend fun updateGame(game: Game)
    @Delete
    suspend fun deleteGame(game: Game)
    @Query("UPDATE games SET currentRound = currentRound + 1 WHERE id = :gameId")
    suspend fun nextRound(gameId: String)
    @Query("UPDATE games SET isActive = 0 WHERE id = :gameId")
    suspend fun endGame(gameId: String)
    @Query("UPDATE games SET currentRound = 1, isActive = 1 WHERE id = :gameId")
    suspend fun resetGame(gameId: String)
}