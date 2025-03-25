package com.example.remember

import android.app.Application

class NotaListApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
    }
}