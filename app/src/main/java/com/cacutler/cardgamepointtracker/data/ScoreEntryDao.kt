package com.cacutler.cardgamepointtracker.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface ScoreEntryDao {
    @Query("SELECT * FROM score_entries WHERE playerId = :playerId ORDER BY timestamp DESC")
    fun getScoreHistory(playerId: String): Flow<List<ScoreEntry>>
    @Query("SELECT * FROM score_entries WHERE playerId = :playerId AND round = :round")
    fun getScoresForRound(playerId: String, round: Int): Flow<List<ScoreEntry>>
    @Insert
    suspend fun insertScoreEntry(scoreEntry: ScoreEntry)
    @Delete
    suspend fun deleteScoreEntry(scoreEntry: ScoreEntry)
    @Query("DELETE FROM score_entries WHERE playerId = :playerId")
    suspend fun deleteAllScoresForPlayer(playerId: String)
    @Query("DELETE FROM score_entries WHERE playerId IN (SELECT id FROM players WHERE gameId = :gameId)")
    suspend fun deleteAllScoresForGame(gameId: String)
    @Query("SELECT * FROM score_entries WHERE playerId = :playerId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastScoreEntry(playerId: String): ScoreEntry?
}