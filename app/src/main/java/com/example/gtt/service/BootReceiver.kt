package com.example.gtt.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.gtt.data.AppDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val geofencingClient = LocationServices.getGeofencingClient(context)
            val db = AppDatabase.getDatabase(context).gttDao()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val locations = db.getAllLocations().firstOrNull() ?: return@launch
                    if (locations.isEmpty()) return@launch

                    val geofences = locations.map { location ->
                        Geofence.Builder()
                            .setRequestId(location.id.toString())
                            .setCircularRegion(location.latitude, location.longitude, location.radiusMeters)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build()
                    }

                    val geofencingRequest = GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofences(geofences)
                        .build()

                    val receiverIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, 0, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )

                    geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                } catch (e: SecurityException) {
                    // Location permissions might not be fully available on boot if not granted "All the time"
                    e.printStackTrace()
                }
            }
        }
    }
}
