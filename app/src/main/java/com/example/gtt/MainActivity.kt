package com.example.gtt

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gtt.data.AppDatabase
import com.example.gtt.data.LocationEntity
import com.example.gtt.service.GeofenceBroadcastReceiver
import com.example.gtt.service.TrackingForegroundService
import com.example.gtt.ui.AddZoneScreen
import com.example.gtt.ui.DashboardScreen
import com.example.gtt.ui.DashboardViewModel
import com.example.gtt.ui.HistoryScreen
import com.example.gtt.ui.PermissionsScreen
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getDatabase(this).gttDao()
        val geofencingClient = LocationServices.getGeofencingClient(this)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var permissionsGranted by remember { mutableStateOf(false) }
                    
                    if (permissionsGranted) {
                        val navController = rememberNavController()
                        val scope = rememberCoroutineScope()
                        
                        val locations by dashboardViewModel.locations.collectAsState()
                        val visits by db.getAllVisits().collectAsState(initial = emptyList())
                        val activeVisit by dashboardViewModel.activeVisit.collectAsState()
                        val totalHoursThisWeek by dashboardViewModel.totalHoursThisWeek.collectAsState()
                        
                        NavHost(navController = navController, startDestination = "dashboard") {
                            composable("dashboard") {
                                DashboardScreen(
                                    onAddZoneClick = { navController.navigate("add_zone") },
                                    onHistoryClick = { navController.navigate("history") },
                                    locations = locations,
                                    activeVisit = activeVisit,
                                    totalHoursThisWeek = totalHoursThisWeek,
                                    onPunchOut = {
                                        activeVisit?.let { visit ->
                                            dashboardViewModel.punchOut(visit)
                                            TrackingForegroundService.stopService(this@MainActivity)
                                        }
                                    }
                                )
                            }
                            composable("add_zone") {
                                AddZoneScreen(
                                    onSaveClick = { name, lat, lng, radius ->
                                        scope.launch {
                                            val id = db.insertLocation(LocationEntity(name = name, latitude = lat, longitude = lng, radiusMeters = radius)).toInt()
                                            
                                            // Add geofence
                                            val geofence = Geofence.Builder()
                                                .setRequestId(id.toString())
                                                .setCircularRegion(lat, lng, radius)
                                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                                                .build()
                                                
                                            val geofencingRequest = GeofencingRequest.Builder()
                                                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                                                .addGeofence(geofence)
                                                .build()
                                                
                                            val intent = Intent(this@MainActivity, GeofenceBroadcastReceiver::class.java)
                                            val pendingIntent = PendingIntent.getBroadcast(
                                                this@MainActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                            )
                                            
                                            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                                            
                                            navController.popBackStack()
                                        }
                                    },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                            composable("history") {
                                HistoryScreen(
                                    visits = visits,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    } else {
                        PermissionsScreen(onPermissionsGranted = { permissionsGranted = true })
                    }
                }
            }
        }
    }
}
