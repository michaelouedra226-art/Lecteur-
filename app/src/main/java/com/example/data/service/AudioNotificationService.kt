package com.example.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AudioNotificationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            "ACTION_TOGGLE" -> {
                sendLocalBroadcast("com.example.PLAY_PAUSE")
                return START_STICKY
            }
            "ACTION_CLOSE" -> {
                sendLocalBroadcast("com.example.CLOSE")
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val title = intent?.getStringExtra("EXTRA_TITLE") ?: "Premium Media Player"
        val isAudio = intent?.getBooleanExtra("EXTRA_IS_AUDIO", true) ?: true
        val isPlaying = intent?.getBooleanExtra("EXTRA_IS_PLAYING", false) ?: false

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Play/Pause Action
        val toggleIntent = Intent(this, AudioNotificationService::class.java).apply {
            setAction("ACTION_TOGGLE")
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Close Action
        val closeIntent = Intent(this, AudioNotificationService::class.java).apply {
            setAction("ACTION_CLOSE")
        }
        val closePendingIntent = PendingIntent.getService(
            this, 2, closeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseLabel = if (isPlaying) "Pause" else "Play"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (isAudio) "Lecture Audio" else "Lecture Vidéo")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(playPauseIcon, playPauseLabel, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Fermer", closePendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    private fun sendLocalBroadcast(actionStr: String) {
        val intent = Intent(actionStr)
        intent.setPackage(packageName) // Send only internally to our app package
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Media Playback Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            serviceChannel.description = "Canal pour l'affichage de la notification de lecture audio et de contrôle."
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "media_playback_channel"
        const val NOTIFICATION_ID = 888
    }
}
