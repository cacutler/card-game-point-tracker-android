package com.cacutler.cardgamepointtracker.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cacutler.cardgamepointtracker.data.Player
import com.cacutler.cardgamepointtracker.repository.GameRepository
import com.cacutler.cardgamepointtracker.ui.viewmodels.ScoreEntryViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreEntrySheet(player: Player, currentRound: Int, repository: GameRepository, onDismiss: () -> Unit) {
    val viewModel = remember { ScoreEntryViewModel(repository, player.id) }
    var points by remember { mutableStateOf("") }
    var isNegative by remember { mutableStateOf(false) }
    val scoreHistory by viewModel.scoreHistory.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(player.name, style = MaterialTheme.typography.titleLarge)
            Text("Round $currentRound", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {// Point type picker
                FilterChip(selected = !isNegative, onClick = { isNegative = false }, label = { Text("Add Points") }, modifier = Modifier.weight(1f))
                FilterChip(selected = isNegative, onClick = { isNegative = true }, label = { Text("Subtract Points") }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {// Points input
                Text(text = if (isNegative) "-" else "+", fontSize = 48.sp, color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.width(50.dp))
                OutlinedTextField(value = points, onValueChange = { points = it.filter { char -> char.isDigit() } }, placeholder = { Text("Points", textAlign = TextAlign.Center) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = LocalTextStyle.current.copy(fontSize = 48.sp, textAlign = TextAlign.Center), modifier = Modifier.weight(1f), singleLine = true)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Quick Add", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)// Quick add buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 5, 10, 25, 50).forEach { value ->
                    Button(
                        onClick = {
                            val finalValue = if (isNegative) -value else value
                            viewModel.addPoints(finalValue, currentRound)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isNegative) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer, contentColor = if (isNegative) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text("${if (isNegative) "-" else "+"}$value")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (scoreHistory.isNotEmpty()) {// Undo button
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                val lastEntry = scoreHistory.first()
                OutlinedButton(
                    onClick = {
                        viewModel.undoLastScore()
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Undo Last (${if (lastEntry.points > 0) "+" else ""}${lastEntry.points})")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {// Action buttons
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        points.toIntOrNull()?.let {value ->
                            val finalValue = if (isNegative) -value else value
                            viewModel.addPoints(finalValue, currentRound)
                            onDismiss()
                        }
                    },
                    enabled = points.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}