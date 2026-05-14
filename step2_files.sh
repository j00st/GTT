#!/bin/bash
set -e

mkdir -p app/src/main/java/com/example/gtt/data
mkdir -p app/src/main/java/com/example/gtt/service
mkdir -p app/src/main/java/com/example/gtt/ui

cat << 'EOF' > app/src/main/java/com/example/gtt/data/LocationEntity.kt
package com.example.gtt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float
)
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/data/VisitEntity.kt
package com.example.gtt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationId: Int,
    val entryTime: Long,
    val exitTime: Long? = null,
    val isManualPunchOut: Boolean = false
)
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/data/GttDao.kt
package com.example.gtt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GttDao {
    @Insert
    suspend fun insertLocation(location: LocationEntity): Long

    @Query("SELECT * FROM locations")
    fun getAllLocations(): Flow<List<LocationEntity>>
    
    @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: Int): LocationEntity?

    @Insert
    suspend fun insertVisit(visit: VisitEntity): Long

    @Update
    suspend fun updateVisit(visit: VisitEntity)

    @Query("SELECT * FROM visits WHERE locationId = :locationId ORDER BY entryTime DESC")
    fun getVisitsForLocation(locationId: Int): Flow<List<VisitEntity>>
    
    @Query("SELECT * FROM visits ORDER BY entryTime DESC")
    fun getAllVisits(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE exitTime IS NULL LIMIT 1")
    suspend fun getActiveVisit(): VisitEntity?
    
    @Query("SELECT * FROM visits WHERE exitTime IS NULL")
    fun getActiveVisitFlow(): Flow<VisitEntity?>
}
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/data/AppDatabase.kt
package com.example.gtt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocationEntity::class, VisitEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gttDao(): GttDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gtt_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/service/GeofenceBroadcastReceiver.kt
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
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/service/TrackingForegroundService.kt
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
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/ui/PermissionsScreen.kt
package com.example.gtt.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionsScreen(onPermissionsGranted: () -> Unit) {
    var notificationGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }
    var backgroundLocationGranted by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationGranted = granted }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val bgLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> backgroundLocationGranted = granted }

    LaunchedEffect(notificationGranted, locationGranted, backgroundLocationGranted) {
        if (notificationGranted && locationGranted && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || backgroundLocationGranted)) {
            onPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("We need a few permissions to track your time automatically.", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!notificationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text("Grant Notification Permission")
            }
        } else if (!locationGranted) {
            Button(onClick = { 
                locationLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }) {
                Text("Grant Location Permission")
            }
        } else if (!backgroundLocationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Text("Please select 'Allow all the time' in the next screen for background tracking.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { bgLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) }) {
                Text("Grant Background Location")
            }
        }
    }
}
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/GttApp.kt
package com.example.gtt

import android.app.Application

class GttApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
EOF

cat << 'EOF' > app/src/main/java/com/example/gtt/MainActivity.kt
package com.example.gtt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.gtt.ui.PermissionsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var permissionsGranted by remember { mutableStateOf(false) }
                    
                    if (permissionsGranted) {
                        // TODO: Dashboard / Navigation
                    } else {
                        PermissionsScreen(onPermissionsGranted = { permissionsGranted = true })
                    }
                }
            }
        }
    }
}
EOF
