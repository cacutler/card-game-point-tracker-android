package com.cacutler.cardgamepointtracker.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface PlayerDao {
    @Transaction
    @Query("SELECT * FROM players WHERE id = :playerId")
    fun getPlayerWithScores(playerId: String): Flow<PlayerWithScores?>
    @Query("SELECT * FROM players WHERE gameId = :gameId ORDER BY score DESC")
    fun getPlayersForGame(gameId: String): Flow<List<Player>>
    @Insert
    suspend fun insertPlayer(player: Player)
    @Insert
    suspend fun insertPlayers(players: List<Player>)
    @Update
    suspend fun updatePlayer(player: Player)
    @Delete
    suspend fun deletePlayer(player: Player)
    @Query("UPDATE players SET score = score + :points WHERE id = :playerId")
    suspend fun addPoints(playerId: String, points: Int)
    @Query("UPDATE players SET score = 0 WHERE gameId = :gameId")
    suspend fun resetScores(gameId: String)
}