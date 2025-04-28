package com.example.remember

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.remember.data.Nota
import com.example.remember.viewModel.NotesViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("ServiceCast")
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
                            .padding(start= 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column() {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )) {
                                        append("Fase: ")
                                    }
                                    append("${nota.fase}\n")

                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    ) {
                                        append("Contenuto: ")
                                    }
                                    append("${nota.contenuto}\n")

                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    ) {
                                        append("Data: ")
                                    }
                                    append("${nota.data}\n")

                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    ) {
                                        append("Ora: ")
                                    }
                                    append("${nota.orario}")

                                    append("\n")

                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    ) {
                                        append("\nNotifica: ")
                                    }
                                    if (!nota.dataNotifica.isNullOrBlank() && !nota.oraNotifica.isNullOrBlank()) {
                                        append("${nota.dataNotifica} alle ${nota.oraNotifica}")
                                    } else {
                                        append("Nessuna Notifica programmata")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )

                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp) // un po' di spazio extra
                                .padding(16.dp)
                        ) {
                            // Icona MoreVert in alto a destra
                            IconButton(
                                onClick = { expanded.value = true },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Opzioni riepilogo"
                                )
                            }

                            // Bottoni modifica + elimina
                            if (!nota.dataNotifica.isNullOrBlank() && !nota.oraNotifica.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .align(Alignment.BottomEnd),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            isDateDialogOpen = true
                                            selectedNote.value = nota
                                            expanded.value = false
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Modifica notifica"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedNote.value?.let { nota ->
                                                val notaAggiornata = nota.copy(
                                                    dataNotifica = null,
                                                    oraNotifica = null
                                                )
                                                viewModel.updateNote(notaAggiornata)


                                                val cancelIntent = Intent(context, NotificationReceiver::class.java)

                                                val cancelPendingIntent = PendingIntent.getBroadcast(
                                                    context,
                                                    nota.id,
                                                    cancelIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                                )

                                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                                alarmManager.cancel(cancelPendingIntent)

                                                Toast.makeText(context, "Notifica cancellata", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Elimina notifica"
                                        )
                                    }

                                }
                            }

                            // Dropdown menu
                            DropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Elimina") },
                                    onClick = {
                                        selectedNote.value = nota
                                        showDeleteDialog.value = true
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Delete, contentDescription = "Elimina")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Modifica") },
                                    onClick = {
                                        selectedNote.value = nota
                                        showDialog.value = true
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Edit, contentDescription = "Modifica")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Notifica") },
                                    onClick = {
                                        isDateDialogOpen = true
                                        selectedNote.value = nota
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Notifications, contentDescription = "Programma notifica")
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

                    selectedNote.value?.let { nota ->
                        viewModel.sendNotificationAt(
                            context,
                            calendar.timeInMillis,
                            nota
                        )
                        
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






