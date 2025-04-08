package com.example.remember.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Nota::class], version = 2, exportSchema = false)
abstract class NotaDatabase: RoomDatabase() {

    abstract fun notaDao(): NotaDao
}

