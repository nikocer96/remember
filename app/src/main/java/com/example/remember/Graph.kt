package com.example.remember

import android.content.Context
import androidx.room.Room
import com.example.remember.data.NotaDatabase
import com.example.remember.data.NotaRepository

object Graph {
    lateinit var database: NotaDatabase

    val notaRepository by lazy {
        NotaRepository(notaDao = database.notaDao())
    }

    fun provide(context: Context) {
        database = Room.databaseBuilder(context, NotaDatabase::class.java, "note_table.db").build()
    }
}