package com.example.remember

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.remember.data.Nota
import com.example.remember.viewModel.NotesViewModel
import kotlinx.coroutines.launch

@Composable
fun RiepilogoScreen(
    navController: NavHostController,
    viewModel: NotesViewModel,
    isDarkTheme: MutableState<Boolean>
) {
    val allNotes by viewModel.getAllNotes.collectAsState(initial = emptyList())
    var showDialog = remember { mutableStateOf(false) }
    var showDeleteDialog = remember { mutableStateOf(false) }
    var selectedNote = remember { mutableStateOf<Nota?>(null) }

    val snackbarHostState = remember {SnackbarHostState()}
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn() {
            items(allNotes) { nota ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), // Distanziamento interno
                        horizontalArrangement = Arrangement.SpaceBetween, // Spazio tra testo e icona
                        verticalAlignment = Alignment.CenterVertically // Allinea il testo e l'icona verticalmente
                    ) {
                        // Testo della nota
                        Column(modifier = Modifier.width(300.dp)) {
                            Text(
                                text = "Fase: ${nota.fase} \n Contenuto: ${nota.contenuto} \n Data: ${nota.data} \n Ora: ${nota.orario}",
                                style = MaterialTheme.typography.bodyMedium

                            )
                        }

                        // Icona per eliminare la nota
                        IconButton(
                            onClick = {
                                selectedNote.value = nota
                                showDeleteDialog.value = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Elimina",
                                tint = Color.Red
                            )
                        }
                        IconButton(
                            onClick = {
                                selectedNote.value = nota
                                showDialog.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Modifica",
                                tint = if (isDarkTheme.value) Color.White else Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        if (showDialog.value && selectedNote.value != null) {
            ModficaNota(
                nota = selectedNote.value!!,
                onDismiss = { showDialog.value = false },
                onSave = {updateNote ->
                    viewModel.updateNote(updateNote)
                    showDialog.value = false
                }
            )
        }
        if (showDeleteDialog.value && selectedNote.value != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = {Text(text = "Conferma eliminazione")},
                text = {Text(text = "Sei sicuro di voler eliminare questa nota?")},
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
                        Text("Si",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                            )
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteDialog.value = false
                    }) {
                        Text("No",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                            )
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



