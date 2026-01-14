package com.cacutler.cardgamepointtracker.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cacutler.cardgamepointtracker.ui.viewmodels.MainViewModel
import com.cacutler.cardgamepointtracker.data.GameWithPlayers
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onNavigateToGame: (String) -> Unit) {
    val activeGames by viewModel.activeGames.collectAsState()
    val completedGames by viewModel.completedGames.collectAsState()
    var showNewGameDialog by remember {mutableStateOf(false)}
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Card Games")},
                actions = {
                    IconButton(onClick = {showNewGameDialog = true}) {
                        Icon(Icons.Default.Add, contentDescription = "New Game")
                    }
                }
            )
        }
    ) {padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (activeGames.isNotEmpty()) {
                item {
                    Text("Active Games", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }
                items(activeGames, key = {it.game.id}) {gameWithPlayers ->
                    GameRow(gameWithPlayers = gameWithPlayers, onClick = {gameWithPlayers.game.id})
                }
            }
            if (completedGames.isNotEmpty()) {
                item {
                    Text("Completed Games", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }
                items(completedGames, key = {it.game.id}) {gameWithPlayers ->
                    GameRow(gameWithPlayers = gameWithPlayers, onClick = {onNavigateToGame(gameWithPlayers.game.id)})
                }
            }
            if (activeGames.isEmpty() && completedGames.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize().padding(23.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Games Yet", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap + to start tracking a new game", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (showNewGameDialog) {
        NewGameDialog(
            onDismiss = {showNewGameDialog = false},
            onCreateGame = {name, playerNames ->
                viewModel.createGame(name, playerNames)
                showNewGameDialog = false
            }
        )
    }
}
@Composable
fun GameRow(gameWithPlayers: GameWithPlayers, onClick: () -> Unit) {
    val game = gameWithPlayers.game
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val winner = gameWithPlayers.players.maxByOrNull {it.score}
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = game.name, style = MaterialTheme.typography.titleMedium)
                if (!game.isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Completed", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))//Use appropriate checkmark icon
                }
            }
            Row {
                Text(text = dateFormat.format(Date(game.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = " • ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = if (game.isActive) "Round ${game.currentRound}" else "${game.currentRound} rounds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (winner != null && !game.isActive) {
                Text(text = "Winner: ${winner.name} (${winner.score} pts)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}