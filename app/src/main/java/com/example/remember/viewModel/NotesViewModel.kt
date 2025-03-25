package com.example.remember.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remember.Graph
import com.example.remember.data.Nota
import com.example.remember.data.NotaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotesViewModel(private val notaRepository: NotaRepository = Graph.notaRepository): ViewModel() {

    var noteContentMap by mutableStateOf<Map<String, String>>(emptyMap())

    fun onNoteContentChange(fase:String, newString: String) {
        noteContentMap = noteContentMap.toMutableMap().apply {
            this[fase] = newString
        }
    }

    lateinit var getAllNotes: Flow<List<Nota>>

    init {
        viewModelScope.launch {
            getAllNotes = notaRepository.getAllNotes()
        }
    }

    fun addNote(nota: Nota) {
        viewModelScope.launch(Dispatchers.IO) {
            notaRepository.addNota(nota=nota)
        }
    }

    fun updateNote(nota: Nota) {
        viewModelScope.launch(Dispatchers.IO) {
            notaRepository.updateNota(nota=nota)
        }
    }

    fun deleteNote(nota: Nota) {
        viewModelScope.launch(Dispatchers.IO) {
            notaRepository.removeNota(nota=nota)
        }
    }

    fun getNoteByFase(fase: String): Flow<List<Nota>> {
        return notaRepository.getNoteByFase(fase)
    }
}