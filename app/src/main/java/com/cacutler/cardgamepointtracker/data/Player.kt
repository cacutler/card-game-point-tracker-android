package com.cacutler.cardgamepointtracker.data
import androidx.room.*
import java.util.*
@Entity(tableName = "players", foreignKeys = [ForeignKey(entity = Game::class, parentColumns = ["id"], childColumns = ["gameId"], onDelete = ForeignKey.CASCADE)], indices = [Index("gameId")])
data class Player(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val name: String,
    val score: Int = 0
)
data class PlayerWithScores(
    @Embedded val player: Player,
    @Relation(parentColumn = "id", entityColumn = "playerId")
    val scoreHistory: List<ScoreEntry>
)