package com.cacutler.cardgamepointtracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cacutler.cardgamepointtracker.data.Player
import com.cacutler.cardgamepointtracker.data.ScoreEntry
import com.cacutler.cardgamepointtracker.repository.GameRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundHistoryScreen(repository: GameRepository, gameId: String, onNavigateBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var gameWithPlayers by remember { mutableStateOf<com.cacutler.cardgamepointtracker.data.GameWithPlayers?>(null) }
    var playerScores by remember { mutableStateOf<Map<String, List<ScoreEntry>>>(emptyMap()) }
    LaunchedEffect(gameId) {
        scope.launch {
            gameWithPlayers = repository.getGameWithPlayers(gameId).first()
            val scores = mutableMapOf<String, List<ScoreEntry>>()// Load score history for all players
            gameWithPlayers?.players?.forEach { player ->
                scores[player.id] = repository.getScoreHistory(player.id).first()
            }
            playerScores = scores
        }
    }
    val game = gameWithPlayers?.game
    val players = gameWithPlayers?.players ?: emptyList()
    val allRounds = (1..(game?.currentRound ?: 1)).toList().reversed()// Generate list of rounds in reverse order
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Round History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(allRounds) { round ->
                RoundSection(round = round, players = players, playerScores = playerScores)
            }
        }
    }
}
@Composable
fun RoundSection(round: Int, players: List<Player>, playerScores: Map<String, List<ScoreEntry>>) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Round $round", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            players.forEach {player ->
                val scores = playerScores[player.id]?.filter { it.round == round } ?: emptyList()
                if (scores.isNotEmpty()) {
                    PlayerRoundRow(player = player, scores = scores)
                    if (player != players.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun PlayerRoundRow(player: Player, scores: List<ScoreEntry>) {
    val total = scores.sumOf { it.points }
    Column {
        Text(player.name, style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(scores.joinToString(", ") { "${if (it.points > 0) "+" else ""}${it.points}" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("Total: ${if (total > 0) "+" else ""}$total", style = MaterialTheme.typography.bodySmall, color = if (total >= 0) Color.Blue else Color.Red, modifier = Modifier.padding(start = 8.dp))
        }
    }
}