package com.cacutler.cardgamepointtracker.data
import android.content.Context
import androidx.room.*
@Database(entities = [Game::class, Player::class, ScoreEntry::class, Round::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun scoreEntryDao(): ScoreEntryDao
    abstract fun roundDao(): RoundDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "point_tracker_database").build()
                INSTANCE = instance
                instance
            }
        }
    }
}