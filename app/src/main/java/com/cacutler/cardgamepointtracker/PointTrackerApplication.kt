package com.cacutler.cardgamepointtracker
import android.app.Application
import com.cacutler.cardgamepointtracker.data.AppDatabase
import com.cacutler.cardgamepointtracker.repository.GameRepository
class PointTrackerApplication: Application() {
    lateinit var database: AppDatabase
    lateinit var repository: GameRepository
    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = GameRepository(database)
    }
}