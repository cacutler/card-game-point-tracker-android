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
    val isValid = gameName.trim().isNotEmpty() && playerNames.count {it.trim().isNotEmpty()} >= 2
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text("New Game")},
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Game Details", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(value = gameName, onValueChange = {gameName = it}, label = {Text("Game Name")}, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Players", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    itemsIndexed(playerNames) { index, name ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = {newName ->
                                    playerNames = playerNames.toMutableList().apply {
                                        this[index] = newName
                                    }
                                },
                                label = { Text("Player ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            if (playerNames.size > 2) {
                                IconButton(onClick = {playerNames = playerNames.toMutableList().apply {removeAt(index)}}) {
                                    Icon(Icons.Default.Delete, "Remove player")
                                }
                            }
                        }
                    }
                    item {
                        TextButton(onClick = {playerNames = playerNames + ""}, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, "Add player")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Player")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val validNames = playerNames.map {it.trim()}.filter {it.isNotEmpty()}
                    onCreateGame(gameName, validNames)
                },
                enabled = isValid
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}