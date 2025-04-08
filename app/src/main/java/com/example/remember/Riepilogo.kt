package com.example.remember

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.remember.data.Nota
import com.example.remember.viewModel.NotesViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.exp

@Composable
fun RiepilogoScreen(
    navController: NavHostController,
    viewModel: NotesViewModel,
    isDarkTheme: MutableState<Boolean>
) {
    val context = LocalContext.current
    val allNotes by viewModel.getAllNotes.collectAsState(initial = emptyList())
    var showDialog = remember { mutableStateOf(false) }
    var showDeleteDialog = remember { mutableStateOf(false) }
    var selectedNote = remember { mutableStateOf<Nota?>(null) }

    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    var isDateDialogOpen by remember { mutableStateOf(false) }
    var isTimeDialogOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn {
            items(allNotes) { nota ->
                var expanded = remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.width(310.dp)) {
                            Text(
                                text = buildString {
                                    append("Fase: ${nota.fase}\n")
                                    append("Contenuto: ${nota.contenuto}\n")
                                    append("Data: ${nota.data}\n")
                                    append("Ora: ${nota.orario}")
                                    if (!nota.dataNotifica.isNullOrBlank() && !nota.oraNotifica.isNullOrBlank()) {
                                        append("\nNotifica: ${nota.dataNotifica} alle ${nota.oraNotifica}")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )

                        }

                        Box {
                            IconButton(onClick = { expanded.value = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Opzioni riepilogo"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Elimina") },
                                    onClick = {
                                        selectedNote.value = nota
                                        showDeleteDialog.value = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Elimina"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Modifica") },
                                    onClick = {
                                        selectedNote.value = nota
                                        showDialog.value = true
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Modifica"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Notifica") },
                                    onClick = {
                                        isDateDialogOpen = true
                                        selectedNote.value = nota
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = "Programma notifica"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (isDateDialogOpen) {
            android.app.DatePickerDialog(
                context,
                { _, year, month, day ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                    isDateDialogOpen = false
                    isTimeDialogOpen = true
                },
                selectedYear,
                selectedMonth,
                selectedDay
            ).show()
        }

        if (isTimeDialogOpen) {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    isTimeDialogOpen = false

                    calendar.set(Calendar.YEAR, selectedYear)
                    calendar.set(Calendar.MONTH, selectedMonth)
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    calendar.set(Calendar.SECOND, 0)

                    val formattedDataNotifica = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                    val formattedOraNotifica = "%02d:%02d".format(selectedHour, selectedMinute)

                    viewModel.sendNotificationAt(
                        context,
                        calendar.timeInMillis,
                        selectedNote.value?.contenuto ?: "Nessun contenuto"
                    )

                    // Aggiorna la nota con data e ora notifica
                    selectedNote.value?.let { nota ->
                        val updatedNote = nota.copy(
                            dataNotifica = formattedDataNotifica,
                            oraNotifica = formattedOraNotifica
                        )
                        viewModel.updateNote(updatedNote)
                    }
                },
                selectedHour,
                selectedMinute,
                true
            ).show()
        }


        if (showDialog.value && selectedNote.value != null) {
            ModficaNota(
                nota = selectedNote.value!!,
                onDismiss = { showDialog.value = false },
                onSave = { updateNote ->
                    viewModel.updateNote(updateNote)
                    showDialog.value = false
                }
            )
        }

        if (showDeleteDialog.value && selectedNote.value != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text(text = "Conferma eliminazione") },
                text = { Text(text = "Sei sicuro di voler eliminare questa nota?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteNote(selectedNote.value!!)
                        showDeleteDialog.value = false

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Nota eliminata con successo",
                                actionLabel = "ok"
                            )
                        }
                    }) {
                        Text("Si", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog.value = false }) {
                        Text("No", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
            )
        }
    }
}

@Composable
fun ModficaNota(nota: Nota, onDismiss: () -> Unit, onSave: (Nota) -> Unit) {
    var updateContent by remember { mutableStateOf(nota.contenuto) }
    var updateFase by remember { mutableStateOf(nota.fase) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(text = "Modifica Nota")},
        text = {
               Column {
                   TextField(
                       value = updateFase,
                       onValueChange = {updateFase = it},
                       label = { Text(text = "Fase")}
                   )
                   TextField(
                       value = updateContent, 
                       onValueChange = {updateContent = it},
                       label = { Text(text = "Contenuto")}
                   )
               }
        },
        confirmButton = {
            Button(onClick = {
                onSave(nota.copy(fase = updateFase, contenuto = updateContent))
            }) {
                Text(text = "Salva")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Annulla")
            }
        }

    )
}





