package com.cacutler.cardgamepointtracker.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacutler.cardgamepointtracker.data.GameWithPlayers
import com.cacutler.cardgamepointtracker.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.cacutler.cardgamepointtracker.data.Player
class GameViewModel(private val repository: GameRepository, private val gameId: String): ViewModel() {
    val gameWithPlayers: StateFlow<GameWithPlayers?> = repository.getGameWithPlayers(gameId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val players: StateFlow<List<Player>> = repository.getPlayersForGame(gameId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun nextRound() {
        viewModelScope.launch {
            repository.nextRound(gameId)
        }
    }
    fun endGame() {
        viewModelScope.launch {
            repository.endGame(gameId)
        }
    }
    fun resetGame() {
        viewModelScope.launch {
            repository.resetGame(gameId)
        }
    }
    fun addPlayer(name: String) {
        viewModelScope.launch {
            repository.addPlayer(gameId, name)
        }
    }
    fun removePlayer(player: Player) {
        viewModelScope.launch {
            repository.removePlayer(player)
        }
    }
    suspend fun getTotalForRound(playerId: String, round: Int): Int {
        return repository.getTotalForRound(playerId, round)
    }
    suspend fun getWinner(): Player? {
        return repository.getWinner(gameId)
    }
}