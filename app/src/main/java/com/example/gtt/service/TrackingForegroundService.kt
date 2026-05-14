package com.example.gtt.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gtt.MainActivity
import com.example.gtt.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TrackingForegroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_PUNCH_OUT) {
            handlePunchOut()
            return START_NOT_STICKY
        }

        val text = intent?.getStringExtra(EXTRA_TEXT) ?: "On-site"
        createNotificationChannel()

        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val punchOutIntent = Intent(this, TrackingForegroundService::class.java).apply {
            action = ACTION_PUNCH_OUT
        }
        val punchOutPendingIntent = PendingIntent.getService(
            this, 1, punchOutIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GTT Active Tracking")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Punch Out", punchOutPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun handlePunchOut() {
        serviceScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@TrackingForegroundService).gttDao()
            val active = db.getActiveVisit()
            if (active != null) {
                db.updateVisit(active.copy(exitTime = System.currentTimeMillis(), isManualPunchOut = true))
            }
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "TrackingServiceChannel"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_TEXT = "EXTRA_TEXT"
        private const val EXTRA_LOCATION_ID = "EXTRA_LOCATION_ID"
        const val ACTION_PUNCH_OUT = "ACTION_PUNCH_OUT"

        fun startService(context: Context, text: String, locationId: Int) {
            val intent = Intent(context, TrackingForegroundService::class.java).apply {
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_LOCATION_ID, locationId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrackingForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
