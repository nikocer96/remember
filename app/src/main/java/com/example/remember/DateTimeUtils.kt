package com.example.remember

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateAndTime(): Pair<String, String> {
    val now = LocalDateTime.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss")
    val date = now.format(dateFormatter)
    val time = now.format(timeFormatter)
    return Pair(date, time)
}