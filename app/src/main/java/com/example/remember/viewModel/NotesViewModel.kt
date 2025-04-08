package com.example.remember.viewModel

import AlertDetails
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remember.Graph
import com.example.remember.NotificationReceiver
import com.example.remember.R
import com.example.remember.data.Nota
import com.example.remember.data.NotaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

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

    fun sendNotification(context: Context) {
        val intent = Intent(context, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "channelID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Notifica di esempio")
            .setContentText("Questo è il corpo della notifica")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build()) // Usa un ID univoco per ogni notifica
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun sendNotificationAt(context: Context, timeInMillis: Long, notaContent: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("NOTA_CONTENT", notaContent)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

}

