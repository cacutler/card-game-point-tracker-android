package com.cacutler.cardgamepointtracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameDialog(onDismiss: () -> Unit, onCreateGame: (String, List<String>) -> Unit) {
    var gameName by remember {mutableStateOf("")}
    var playerNames by remember {mutableStateOf(listOf("", ""))}
    var showError by remember {mutableStateOf(false)}
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("New Game")},
        text = {
            Column {
                OutlinedTextField(value = gameName, onValueChange = {gameName = it}, label = {Text("Game Name")}, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Text("Players (minimum 2)", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                playerNames.forEachIndexed {index, name ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {newName ->
                                playerNames = playerNames.toMutableList().apply {
                                    this[index] = newName
                                }
                            },
                            label = { Text("Player ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (playerNames.size > 2) {
                            IconButton(onClick = {playerNames = playerNames.toMutableList().apply {removeAt(index)}}) {
                                Icon(Icons.Default.Delete, "Remove player", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                TextButton(onClick = {playerNames = playerNames + ""}) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Player")
                }
                if (showError) {
                    Text("Please enter a game name and at least 2 unique player names", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validPlayers = playerNames.map {it.trim()}.filter {it.isNotBlank()}.distinct()
                    if (gameName.isNotBlank() && validPlayers.size >= 2) {
                        onCreateGame(gameName.trim(), validPlayers)
                        showError = false
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}