package com.example.remember.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Nota::class], version = 1, exportSchema = false)
abstract class NotaDatabase: RoomDatabase() {

    abstract fun notaDao(): NotaDao


}