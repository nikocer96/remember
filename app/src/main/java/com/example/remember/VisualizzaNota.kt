package com.example.remember

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.remember.viewModel.SharedViewModel

@Composable
fun Visualizza(navController: NavController, sharedViewModel: SharedViewModel) {
    val nota = sharedViewModel.selectedNota.value
    if (nota != null) {
        Text(text = "Nota selezionata: ${nota.fase}, ${nota.contenuto}")
    }
}