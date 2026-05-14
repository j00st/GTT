package com.example.gtt.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.example.gtt.data.AppDatabase
import com.example.gtt.data.VisitEntity
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context).gttDao()
            val now = System.currentTimeMillis()
            val workManager = WorkManager.getInstance(context)

            for (geofence in triggeringGeofences) {
                val locationId = geofence.requestId.toIntOrNull() ?: continue
                val workName = "${ExitDebounceWorker.WORK_NAME_PREFIX}$locationId"

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    // Cancel any pending exit debounce for this location
                    workManager.cancelUniqueWork(workName)
                    
                    val active = db.getActiveVisit()
                    if (active == null) {
                        db.insertVisit(VisitEntity(locationId = locationId, entryTime = now))
                        TrackingForegroundService.startService(context, "Tracking time...", locationId)
                    }
                } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    // DEBOUNCE: 5-minute exit debounce via WorkManager
                    val data = Data.Builder()
                        .putInt(ExitDebounceWorker.KEY_LOCATION_ID, locationId)
                        .putLong(ExitDebounceWorker.KEY_EXIT_TIME, now)
                        .build()
                        
                    val exitWork = OneTimeWorkRequestBuilder<ExitDebounceWorker>()
                        .setInitialDelay(5, TimeUnit.MINUTES)
                        .setInputData(data)
                        .build()
                        
                    workManager.enqueueUniqueWork(
                        workName,
                        ExistingWorkPolicy.REPLACE,
                        exitWork
                    )
                }
            }
        }
    }
}
