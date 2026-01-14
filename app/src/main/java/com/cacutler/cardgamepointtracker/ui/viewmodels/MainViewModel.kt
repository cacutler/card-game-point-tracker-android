package com.cacutler.cardgamepointtracker.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacutler.cardgamepointtracker.data.Game
import com.cacutler.cardgamepointtracker.data.GameWithPlayers
import com.cacutler.cardgamepointtracker.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class MainViewModel(private val repository: GameRepository): ViewModel() {
    val activeGames: StateFlow<List<GameWithPlayers>> = repository.getActiveGames().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedGames: StateFlow<List<GameWithPlayers>> = repository.getCompletedGames().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun createGame(name: String, playersNames: List<String>) {
        viewModelScope.launch {
            repository.createGame(name, playersNames)
        }
    }
    fun deleteGame(game: Game) {
        viewModelScope.launch {
            repository.deleteGame(game)
        }
    }
}