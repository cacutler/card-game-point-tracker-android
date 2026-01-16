package com.cacutler.cardgamepointtracker.ui.screens
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cacutler.cardgamepointtracker.data.Player
import com.cacutler.cardgamepointtracker.repository.GameRepository
import com.cacutler.cardgamepointtracker.ui.viewmodels.GameViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, repository: GameRepository, onNavigateBack: () -> Unit, onNavigateToHistory: () -> Unit) {
    val gameWithPlayers by viewModel.gameWithPlayers.collectAsState()
    val players by viewModel.players.collectAsState()
    var showScoreSheet by remember {mutableStateOf(false)}
    var selectedPlayer by remember {mutableStateOf<Player?>(null)}
    var showNextRoundDialog by remember {mutableStateOf(false)}
    var showResetDialog by remember {mutableStateOf(false)}
    var showAddPlayerDialog by remember {mutableStateOf(false)}
    var showMenu by remember {mutableStateOf(false)}
    val game = gameWithPlayers?.game
    val sortedPlayers = players.sortedByDescending {it.score}
    var winner by remember {mutableStateOf<Player?>(null)}// Calculate winner once
    LaunchedEffect(game?.isActive, players) {
        if (game?.isActive == false && players.isNotEmpty()) {
            winner = viewModel.getWinner()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(game?.name ?: "")},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.Info, "History")
                    }
                    IconButton(onClick = {showMenu = true}) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = {showMenu = false}) {
                        if (game?.isActive == true) {
                            DropdownMenuItem(
                                text = {Text("Add Player")},
                                onClick = {
                                    showAddPlayerDialog = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {Text("End Game")},
                                onClick = {
                                    viewModel.endGame()
                                    showMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {Text("Reset Game")},
                            onClick = {
                                showResetDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) {padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Round ${game?.currentRound ?: 1}", style = MaterialTheme.typography.titleMedium)
                            if (game?.isActive == true) {
                                Text("Tap players to add scores", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (game?.isActive == true) {
                            Button(onClick = {showNextRoundDialog = true}) {
                                Icon(Icons.Default.ArrowForward, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Next Round")
                            }
                        }
                    }
                }
            }
            item {
                Text("Scores", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp, 8.dp))
            }
            items(sortedPlayers, key = {it.id}) {player ->
                PlayerRow(
                    player = player,
                    isActive = game?.isActive ?: false,
                    currentRound = game?.currentRound ?: 1,
                    isWinner = player == winner && game?.isActive == false,
                    onPlayerClick = {
                        if (game?.isActive == true) {
                            selectedPlayer = player
                            showScoreSheet = true
                        }
                    },
                    onRemovePlayer = {
                        viewModel.removePlayer(player)
                    },
                    viewModel = viewModel,
                    repository = repository,
                    playerCount = players.size
                )
            }
        }
    }
    if (showScoreSheet && selectedPlayer != null) {// Dialogs and sheets
        ScoreEntrySheet(
            player = selectedPlayer!!,
            currentRound = game?.currentRound ?: 1,
            repository = repository,
            onDismiss = {
                showScoreSheet = false
                selectedPlayer = null
            }
        )
    }
    if (showNextRoundDialog) {
        AlertDialog(
            onDismissRequest = {showNextRoundDialog = false},
            title = {Text("Start Next Round?")},
            text = {Text("Round ${game?.currentRound} will be complete and Round ${(game?.currentRound ?: 1) + 1} will begin.")},
            confirmButton = {
                TextButton(onClick = {
                    viewModel.nextRound()
                    showNextRoundDialog = false
                }) {
                    Text("Next Round")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNextRoundDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = {showResetDialog = false},
            title = {Text("Reset Game?")},
            text = {Text("This will delete all scores and reset the game to Round 1. This action cannot be undone.")},
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetGame()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = {showResetDialog = false}) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showAddPlayerDialog) {
        AddPlayerDialog(
            onDismiss = {showAddPlayerDialog = false},
            onAddPlayer = {name ->
                viewModel.addPlayer(name)
                showAddPlayerDialog = false
            }
        )
    }
}
@Composable
fun PlayerRow(player: Player, isActive: Boolean, currentRound: Int, isWinner: Boolean, onPlayerClick: () -> Unit, onRemovePlayer: () -> Unit, viewModel: GameViewModel, repository: GameRepository, playerCount: Int) {
    var roundTotal by remember {mutableIntStateOf(0)}
    var showRemoveDialog by remember {mutableStateOf(false)}
    var showRoundDetail by remember {mutableStateOf(false)}
    var showMinPlayerWarning by remember {mutableStateOf(false)}
    LaunchedEffect(player.id, currentRound) {
        if (isActive) {
            roundTotal = viewModel.getTotalForRound(player.id, currentRound)
        }
    }
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).combinedClickable(onClick = onPlayerClick, onLongClick = {showRoundDetail = true}, enabled = isActive)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    if (isWinner) {
                        Text("Winner!", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                    }
                    if (isActive && roundTotal != 0) {
                        if (isWinner) {Spacer(modifier = Modifier.width(8.dp))}
                        Text("This round: ${if (roundTotal > 0) "+" else ""}$roundTotal", style = MaterialTheme.typography.bodySmall, color = if (roundTotal > 0) Color.Blue else Color.Red)
                    }
                }
            }
            Text("${player.score}", style = MaterialTheme.typography.headlineMedium, color = if (player.score < 0) Color.Red else Color.Blue)
            if (isActive) {
                IconButton(onClick = onPlayerClick) {
                    Icon(Icons.Default.Add, "Add score")
                }
                IconButton(onClick = {if (playerCount > 2) {showRemoveDialog = true} else {showMinPlayerWarning = true}}) {
                    Icon(Icons.Default.Delete, "Remove player", tint = if (playerCount > 2) {MaterialTheme.colorScheme.error} else {MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)})
                }
            }
        }
    }
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = {showRemoveDialog = false},
            title = {Text("Remove Player?")},
            text = {Text("Remove ${player.name} from this game? Their scores will be deleted.")},
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemovePlayer()
                        showRemoveDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showMinPlayerWarning) {
        AlertDialog(onDismissRequest = {showMinPlayerWarning = false}, title = {Text("Cannot Remove Player")}, text = {Text("A game must have at least 2 players. Add more players before removing ${player.name}.")}, confirmButton = {TextButton(onClick = { showMinPlayerWarning = false }) {Text("OK")}})
    }
    if (showRoundDetail) {
        RoundDetailDialog(player = player, round = currentRound, repository = repository, onDismiss = {showRoundDetail = false})
    }
}
@Composable
fun AddPlayerDialog(onDismiss: () -> Unit, onAddPlayer: (String) -> Unit) {
    var playerName by remember {mutableStateOf("")}
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("Add Player")},
        text = {OutlinedTextField(value = playerName, onValueChange = {playerName = it}, label = {Text("Player Name")}, singleLine = true)},
        confirmButton = {
            TextButton(
                onClick = {
                    if (playerName.isNotBlank()) {
                        onAddPlayer(playerName.trim())
                    }
                },
                enabled = playerName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun RoundDetailDialog(player: Player, round: Int, repository: GameRepository, onDismiss: () -> Unit) {
    val scores by repository.getScoresForRound(player.id, round).collectAsState(initial = emptyList())
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("${player.name} - Round $round")},
        text = {
            Column {
                if (scores.isEmpty()) {
                    Text("No scores entered this round")
                } else {
                    scores.forEach {scoreEntry ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (scoreEntry.points >= 0) {"+${scoreEntry.points}"} else {"${scoreEntry.points}"}, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium)
                        Text("${scores.sumOf { it.points }}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        confirmButton = {TextButton(onClick = onDismiss) {Text("Close")} }
    )
}