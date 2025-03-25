package com.example.remember.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface NotaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun inserisciNota(nota: Nota)

    @Query("SELECT * FROM note_table")
    abstract fun getAllNotes(): Flow<List<Nota>>

    @Update
    abstract fun updateNote(note: Nota)

    @Delete
    abstract fun deleteNote(note: Nota)

    @Query("SELECT * FROM note_table WHERE fase = :fase")
    abstract fun getNotePerFase(fase: String): Flow<List<Nota>>
}