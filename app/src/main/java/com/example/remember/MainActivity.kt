package com.example.remember

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.remember.data.Nota
import com.example.remember.ui.theme.RememberTheme
import com.example.remember.viewModel.NotesViewModel


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = remember { mutableStateOf(false) }
            RememberTheme(darkTheme = isDarkTheme.value) {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(isDarkTheme)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(isDarkTheme: MutableState<Boolean>) {
    val viewModel: NotesViewModel = viewModel()
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val canNavigateBack = currentBackStackEntry?.destination?.route != "home"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Remember",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Torna indietro",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) {paddingValues ->
        NavHost(navController = navController, startDestination = "home" ) {
            composable("home") {
                HomeScreen(navController, viewModel, isDarkTheme)
            }
            composable("day/{giorno}") { backStackEntry ->
                val giorno = backStackEntry.arguments?.getString("giorno") ?: "Giorno"
                DayScreen(navController = navController, giorno = giorno, viewModel = viewModel)
            }
            composable("riepilogo") {
                RiepilogoScreen(navController, viewModel)
            }
        }
    }


}

@Composable
fun HomeScreen(navaController: NavController, viewModel: NotesViewModel, isDarkTheme: MutableState<Boolean>) {

    val giorni = listOf<String>("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        for(giorno in giorni) {
            Button(onClick = {
                    navaController.navigate("day/$giorno")
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)) {
                Text(text = giorno)
            }
        }
        Button(onClick = {
            navaController.navigate("riepilogo")
        }) {
            Text(text = "Riepilogo")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            isDarkTheme.value = !isDarkTheme.value
        }) {
            Text(if (isDarkTheme.value) "passa a chiaro" else "passa a scuro")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayScreen(navController: NavController, giorno: String, viewModel: NotesViewModel) {

    val fasi = listOf<String>("Mattina", "Pomeriggio", "Sera", "Notte")
    var notaScritta by remember { mutableStateOf("") }
    val noteMap = remember { mutableStateMapOf<String, String>() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp)
    ) {
        //Text(text = "Hai selezionato il giorno: $giorno")
        for (fase in fasi) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)) {
                Text(text = fase, fontSize = 18.sp, fontWeight = FontWeight.Medium)

                OutlinedTextField(
                    value = viewModel.noteContentMap[fase] ?: "", // Recupera la nota per quella fase
                    onValueChange = {nuovaNota ->
                        viewModel.onNoteContentChange(fase, nuovaNota)}, // Salva la nuova nota
                    label = { Text("Inserisci nota per $fase") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        val (data, orario) = getCurrentDateAndTime()
        Button(onClick = {
            // Cicla su ogni fase e crea una nota separata per ogni fase
            fasi.forEach { fase ->
                val contenuto = viewModel.noteContentMap[fase] ?: ""
                if (contenuto.isNotEmpty()) {
                    // Aggiungi la nota per questa fase
                    viewModel.addNote(
                        Nota(
                            contenuto = contenuto,  // Il contenuto della fase
                            fase = fase,            // La fase (Mattina, Pomeriggio, ecc.)
                            data = data,           // La data corrente
                            orario = orario        // L'orario corrente
                        )
                    )
                    viewModel.noteContentMap = viewModel.noteContentMap.toMutableMap().apply {
                        this[fase] = ""
                    }
                }
            }
        }) {
            Text(text = "Salva Note")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text(text = "Torna Indietro")
        }
    }
}