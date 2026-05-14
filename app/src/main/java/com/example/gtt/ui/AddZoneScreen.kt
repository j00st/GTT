package com.example.gtt.ui

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddZoneScreen(
    onSaveClick: (name: String, lat: Double, lng: Double, radius: Float) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("My Tracking Zone") }
    var radius by remember { mutableStateOf("100") }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var isFetching by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        isFetching = true
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                currentLocation = loc
                isFetching = false
            }
            .addOnFailureListener {
                isFetching = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Zone") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Zone Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Location", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isFetching) {
                        Text("Fetching current location...")
                        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                    } else if (currentLocation != null) {
                        Text("Lat: ${currentLocation?.latitude}")
                        Text("Lng: ${currentLocation?.longitude}")
                        Text("Accuracy: ${currentLocation?.accuracy}m")
                    } else {
                        Text("Failed to get location.")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        isFetching = true
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { loc ->
                                currentLocation = loc
                                isFetching = false
                            }
                            .addOnFailureListener {
                                isFetching = false
                            }
                    }) {
                        Text("Refresh Location")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Radius (meters)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val latD = currentLocation?.latitude ?: 0.0
                    val lngD = currentLocation?.longitude ?: 0.0
                    val radF = radius.toFloatOrNull() ?: 100f
                    if (latD != 0.0 && lngD != 0.0) {
                        onSaveClick(name, latD, lngD, radF)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = currentLocation != null && !isFetching
            ) {
                Text("Save Zone")
            }
        }
    }
}
