package com.example.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var musicService: MusicPlayerService? = null  // Variable para el servicio
    private var isServiceBound = false  // Indica si el servicio está vinculado

    // Conexión al servicio
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicPlayerBinder
            musicService = binder.getService()  // Obtener una instancia del servicio
            isServiceBound = true  // Marcar que el servicio está vinculado
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false  // Marcar que el servicio se ha desconectado
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Definir los botones para controlar la música
        val playButton = findViewById<Button>(R.id.playButton)
        val pauseButton = findViewById<Button>(R.id.pauseButton)
        val resumeButton = findViewById<Button>(R.id.resumeButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        // Acción para iniciar la música
        playButton.setOnClickListener {
            if (!isServiceBound) {
                val serviceIntent = Intent(this, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PLAY  // Establecer la acción de iniciar la música
                }
                startService(serviceIntent)
                bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
            }
        }

        pauseButton.setOnClickListener {
            if (isServiceBound) {
                val serviceIntent = Intent(this, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_PAUSE
                }
                startService(serviceIntent)
            }
        }

        resumeButton.setOnClickListener {
            if (isServiceBound) {
                val serviceIntent = Intent(this, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_RESUME
                }
                startService(serviceIntent)
            }
        }

        stopButton.setOnClickListener {
            if (isServiceBound) {
                val serviceIntent = Intent(this, MusicPlayerService::class.java).apply {
                    action = MusicPlayerService.ACTION_STOP
                }
                startService(serviceIntent)
                unbindService(serviceConnection)  // Desvincular el servicio
                isServiceBound = false  // Marcar que el servicio ya no está vinculado
            }
        }
    }

    // Liberar la conexión al servicio cuando la actividad se destruye
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}
