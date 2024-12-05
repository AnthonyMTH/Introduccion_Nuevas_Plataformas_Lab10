package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class MusicPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = "music_service_channel"  // ID para el canal de notificación
    private val NOTIFICATION_ID = 1  // ID para la notificación

    private var isPlaying = false  // Variable para verificar si la música está en reproducción

    // Esta clase permite la comunicación entre el servicio y el cliente (MainActivity)
    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.music)
    }

    // Método llamado cuando se inicia el servicio. Maneja las diferentes acciones de los Intents
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Dependiendo de la acción del Intent, se maneja la música
        when (intent?.action) {
            ACTION_PLAY -> startMusic()
            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
            ACTION_STOP -> stopMusic()
        }
        // Iniciar el servicio en primer plano con la notificación
        startForeground(NOTIFICATION_ID, createNotification(intent?.action))
        return START_STICKY  // Mantener el servicio corriendo incluso si la actividad se cierra
    }

    private fun startMusic() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    private fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    private fun resumeMusic() {
        if (!mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()  // Preparar el MediaPlayer para poder reproducir nuevamente
        isPlaying = false
    }

    // Crear la notificación que muestra el estado de la música
    private fun createNotification(action: String?): Notification {
        // Verificar si es necesario crear un canal de notificación en dispositivos con Android O o superior
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)  // Crear el canal de notificación
            CHANNEL_ID
        } else {
            ""  // Para dispositivos antiguos sin necesidad de canal
        }

        // Crear un Intent para cuando se toque la notificación
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE  // Usar FLAG_IMMUTABLE para asegurar que el PendingIntent no cambie
        )

        // Determinar el texto de la notificación basado en la acción de la música
        val actionText = when (action) {
            ACTION_PLAY -> "Reproduciendo música"
            ACTION_PAUSE -> "Pausa la música"
            ACTION_RESUME -> "Reanudando música"
            else -> "Música detenida"
        }

        // Construir la notificación
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Servicio de Música")
            .setContentText(actionText)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)  // Acción que ocurre cuando se toca la notificación
            .build()
    }

    // Método de vinculación para el servicio
    override fun onBind(intent: Intent?): IBinder? {
        return MusicPlayerBinder()  // Retorna el binder para permitir la interacción con la actividad
    }

    // Método para liberar recursos cuando el servicio es destruido
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    companion object {
        const val ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.musicplayer.ACTION_RESUME"
        const val ACTION_STOP = "com.example.musicplayer.ACTION_STOP"
    }
}
