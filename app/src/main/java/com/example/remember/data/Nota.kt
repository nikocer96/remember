package com.example.remember.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class Nota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fase: String,
    val contenuto: String,
    val data: String,
    val orario: String,
    val dataNotifica: String? = null,
    val oraNotifica: String? = null
)