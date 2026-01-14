package com.cacutler.cardgamepointtracker.data
import androidx.room.*
import java.util.*
@Entity(tableName = "score_entries", foreignKeys = [ForeignKey(entity = Player::class, parentColumns = ["id"], childColumns = ["playerId"], onDelete = ForeignKey.CASCADE)], indices = [Index("playerId"), Index("round")])
data class ScoreEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val points: Int,
    val round: Int,
    val timestamp: Long = System.currentTimeMillis()
)