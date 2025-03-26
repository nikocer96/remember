package com.example.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(
    navController: NavController,
    isDarkTheme: MutableState<Boolean>,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    // Controlla la rotta attuale
    val currentRoute = navController.currentDestination?.route

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme.value) Color.DarkGray else Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isDarkTheme.value) Color.White else Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Divider(color = if (isDarkTheme.value) Color.LightGray else Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        NavigationDrawerItem(
            modifier = Modifier.padding(bottom = 8.dp),
            label = { Text(text = "Home") },
            selected = currentRoute == "home",
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            onClick = {
                if (currentRoute != "home") {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
                scope.launch { drawerState.close() }
            }
        )

        NavigationDrawerItem(
            modifier = Modifier.padding(bottom = 8.dp),
            label = { Text(text = "Riepilogo") },
            selected = currentRoute == "riepilogo",
            icon = { Icon(Icons.Filled.List, contentDescription = "Riepilogo") },
            onClick = {
                if (currentRoute != "riepilogo") {
                    navController.navigate("riepilogo") {
                        popUpTo("home") { inclusive = false }
                    }
                }
                scope.launch { drawerState.close() }
            }
        )

        NavigationDrawerItem(
            label = { Text(text = if (isDarkTheme.value) "Modalità chiara" else "Modalità scura") },
            selected = false,
            icon = { Icon(
                if (isDarkTheme.value) Icons.Filled.FavoriteBorder else Icons.Filled.Favorite,
                contentDescription = "Tema"
            ) },
            onClick = {
                isDarkTheme.value = !isDarkTheme.value
                scope.launch { drawerState.close() }
            }
        )
    }
}
