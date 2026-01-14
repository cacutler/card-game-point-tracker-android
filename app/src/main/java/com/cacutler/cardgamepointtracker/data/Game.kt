package com.cacutler.cardgamepointtracker.data
import androidx.room.*
import java.util.*
@Entity(tableName = "games")
data class Game(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val currentRound: Int = 1
)
data class GameWithPlayers(
    @Embedded val game: Game,
    @Relation(parentColumn = "id", entityColumn = "gameId")
    val players: List<Player>
)