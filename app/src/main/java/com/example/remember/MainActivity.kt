package com.example.remember

import AlertDetails
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "channelID"
    private val CHANNEL_NAME = "Notifiche Importanti"
    private val CHANNEL_DESCRIPTION = "Ricevi notifiche relative a eventi importanti della tua app."

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permesso notifiche concesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permesso per le notifiche negato!", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission() // Richiesta permesso appena si apre l'app

        setContent {
            val isDarkTheme = remember { mutableStateOf(false) }
            RememberTheme(darkTheme = isDarkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(isDarkTheme)
                    createNotificationChannel() // Creazione canale notifiche
                }
            }
        }

    }

    // Funzione per chiedere il permesso all'avvio
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Crea il canale di notifica per Android 8.0+
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    //  Invio notifica manuale
    fun sendNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission()
                return
            }
        }

        val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notifica di esempio")
            .setContentText("Questo è il corpo della notifica")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    // Permesso manuale
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}


@Composable
fun LoginScreen(navController: NavController) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (email.value.isNotEmpty() && password.value.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email.value, password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login riuscito!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Errore login: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Inserisci email e password!", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("Non hai un account? Registrati")
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

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showUI = currentRoute != "login" && currentRoute != "register"

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "home" else "login"

    val appContent: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                if (showUI) {
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
                            IconButton(onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF5227D8),
                            titleContentColor = Color.White
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("register") {
                        RegisterScreen(navController)
                    }
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

    if (showUI) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(navController, isDarkTheme, drawerState, scope)
                }
            },
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            appContent()
        }
    } else {
        appContent()
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