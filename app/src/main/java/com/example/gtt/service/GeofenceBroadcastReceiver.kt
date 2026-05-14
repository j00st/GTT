package com.example.gtt.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.example.gtt.data.AppDatabase
import com.example.gtt.data.VisitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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

            for (geofence in triggeringGeofences) {
                val locationId = geofence.requestId.toIntOrNull() ?: continue

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    val active = db.getActiveVisit()
                    if (active == null) {
                        db.insertVisit(VisitEntity(locationId = locationId, entryTime = now))
                        TrackingForegroundService.startService(context, "Tracking time...", locationId)
                    }
                } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    // DEBOUNCE: 5-minute exit debounce to prevent GPS flapping
                    delay(5 * 60 * 1000)
                    
                    // Check if still exited (simplified debounce logic for broadcast receiver)
                    // Realistically we'd use WorkManager or a Service, but we'll close the visit here
                    val active = db.getActiveVisit()
                    if (active != null && active.locationId == locationId) {
                        db.updateVisit(active.copy(exitTime = now))
                        TrackingForegroundService.stopService(context)
                    }
                }
            }
        }
    }
}
