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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
fun PermissionsScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    
    var notificationGranted by remember { 
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) 
    }
    var locationGranted by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || 
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) 
    }
    var backgroundLocationGranted by remember { 
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || 
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) 
    }

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
