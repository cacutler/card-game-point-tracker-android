package com.cacutler.cardgamepointtracker.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacutler.cardgamepointtracker.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.cacutler.cardgamepointtracker.data.ScoreEntry
class ScoreEntryViewModel(private val repository: GameRepository, private val playerId: String): ViewModel() {
    val scoreHistory: StateFlow<List<ScoreEntry>> = repository.getScoreHistory(playerId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun addPoints(points: Int, round: Int) {
        viewModelScope.launch {
            repository.addPoints(playerId, points, round)
        }
    }
    fun undoLastScore() {
        viewModelScope.launch {
            repository.undoLastScore(playerId)
        }
    }
}