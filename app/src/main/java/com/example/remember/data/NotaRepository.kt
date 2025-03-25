package com.example.remember.data

import kotlinx.coroutines.flow.Flow

class NotaRepository(private val notaDao: NotaDao) {

    suspend fun addNota(nota: Nota) {
        notaDao.inserisciNota(nota)
    }

    fun getAllNotes(): Flow<List<Nota>> = notaDao.getAllNotes()

    suspend fun updateNota(nota: Nota) {
        notaDao.updateNote(nota)
    }

    suspend fun removeNota(nota: Nota) {
        notaDao.deleteNote(nota)
    }

    fun getNoteByFase(fase: String): Flow<List<Nota>> {
        return notaDao.getNotePerFase(fase)
    }
}