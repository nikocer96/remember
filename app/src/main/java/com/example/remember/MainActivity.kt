package com.example.remember

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.remember.data.Nota
import com.example.remember.ui.theme.RememberTheme
import com.example.remember.viewModel.NotesViewModel
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = remember { mutableStateOf(false) }
            RememberTheme(darkTheme = isDarkTheme.value) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(isDarkTheme)
                    createNotificationsChannel()
                }
            }
        }
    }
    fun createNotificationsChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH).apply {
                    lightColor = Color.Green.toArgb()
                    enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
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

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ModalNavigationDrawer(
        drawerState = drawerState,
        // Evita che il menu' resti aperto
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController, isDarkTheme, drawerState, scope)
            }
        },
        // Sfondo scuro quando il menu è aperto
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
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
                        containerColor = Color(0xFF5227D8),
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Apri menù",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
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
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("medicine") {
                        MedicineScreen(navController, viewModel, isDarkTheme)
                    }
                    composable("day/{giorno}") { backStackEntry ->
                        val giorno = backStackEntry.arguments?.getString("giorno") ?: "Giorno"
                        DayScreen(navController, giorno, viewModel, isDarkTheme)
                    }
                    composable("riepilogo") {
                        RiepilogoScreen(navController, viewModel, isDarkTheme)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            text = "Scegli la categoria",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row (
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { navController.navigate("medicine") }
                    .background(Color.LightGray)
                    .padding(20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icons8_medicine_64),
                    contentDescription = "Medicine",
                    modifier = Modifier.matchParentSize(),
                    //contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { navController.navigate("medicine") }
                    .background(Color.Blue)
                    .padding(20.dp)
            ) {

            }
        }
    }
}

@Composable
fun MedicineScreen(navaController: NavController, viewModel: NotesViewModel, isDarkTheme: MutableState<Boolean>) {

    val giorni = listOf<String>("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 8.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = "Aggiungi tutte le tue medicine",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(16.dp)
        )
        Divider(color = if (isDarkTheme.value) Color.LightGray else Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        for(giorno in giorni) {
            Button(onClick = {
                    navaController.navigate("day/$giorno")
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = giorno,
                    fontSize = 20.sp
                )
            }
        }
       /* Button(onClick = {
            navaController.navigate("riepilogo")
        }) {
            Text(text = "Riepilogo")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            isDarkTheme.value = !isDarkTheme.value
        }) {
            Text(if (isDarkTheme.value) "passa a chiaro" else "passa a scuro")
        }*/
    }
}

@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayScreen(
    navController: NavController,
    giorno: String,
    viewModel: NotesViewModel,
    isDarkTheme: MutableState<Boolean>
    ) {
    val snackbarHostState = remember { SnackbarHostState() }
    val caroutineScope = rememberCoroutineScope()
    val fasi = listOf<String>("Mattina", "Pomeriggio", "Sera", "Notte")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
        Text(
            text = giorno,
            fontSize = 30.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Divider(color = if (isDarkTheme.value) Color.LightGray else Color.Gray)
        for (fase in fasi) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                Text(text = fase, fontSize = 18.sp, fontWeight = FontWeight.Medium)

                OutlinedTextField(
                    // Recupero la nota per quella fase
                    value = viewModel.noteContentMap[fase] ?: "",
                    onValueChange = {nuovaNota ->
                        // Salvo la nuova nota
                        viewModel.onNoteContentChange(fase, nuovaNota)},
                    label = { Text("Inserisci nota per $fase") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 25.dp)
                )
            }
        }
        val (data, orario) = getCurrentDateAndTime()
        Button(onClick = {
            // Ciclo su ogni fase e creo una nota separata per ogni fase
            fasi.forEach { fase ->
                val contenuto = viewModel.noteContentMap[fase] ?: ""
                if (contenuto.isNotEmpty()) {
                    // Aggiungi la nota per questa fase
                    viewModel.addNote(
                        Nota(
                            contenuto = contenuto,
                            fase = fase,
                            data = data,
                            orario = orario
                        )
                    )
                    viewModel.noteContentMap = viewModel.noteContentMap.toMutableMap().apply {
                        this[fase] = ""
                    }
                    caroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Nota aggiunta con successo",
                            actionLabel = "ok"
                        )
                    }
                }
            }
        }) {
            Text(
                text = "Salva Nota",
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}