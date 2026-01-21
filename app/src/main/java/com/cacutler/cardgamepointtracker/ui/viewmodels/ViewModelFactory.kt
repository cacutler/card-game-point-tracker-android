package com.cacutler.cardgamepointtracker.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cacutler.cardgamepointtracker.repository.GameRepository
class MainViewModelFactory(private  val repository: GameRepository): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class GameViewModelFactory(private val repository: GameRepository, private val gameId: String): ViewModelProvider.Factory {
    override  fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository, gameId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}