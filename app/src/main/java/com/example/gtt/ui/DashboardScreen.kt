package com.example.gtt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gtt.data.LocationEntity
import com.example.gtt.data.VisitEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddZoneClick: () -> Unit,
    onHistoryClick: () -> Unit,
    locations: List<LocationEntity>,
    activeVisit: VisitEntity?,
    totalHoursThisWeek: Double,
    liveStatus: Pair<Float, Boolean>?,
    onPunchOut: () -> Unit,
    onResetAppClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GTT Dashboard") },
                actions = {
                    TextButton(onClick = onResetAppClick) {
                        Text("Reset App", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = onHistoryClick) {
                        Text("History")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddZoneClick) {
                if (locations.isEmpty()) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Zone")
                } else {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Zone")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Dashboard Stats
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Hours This Week", style = MaterialTheme.typography.titleMedium)
                    val hoursStr = String.format(java.util.Locale.getDefault(), "%.1f hrs", totalHoursThisWeek)
                    Text(hoursStr, style = MaterialTheme.typography.displayMedium)
                }
            }

            if (activeVisit != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Currently Active at Zone", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onPunchOut) {
                            Text("Punch Out")
                        }
                    }
                }
            }

            if (locations.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (liveStatus?.second == true) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Live Status", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (liveStatus != null) {
                            val distStr = String.format(java.util.Locale.getDefault(), "%.1f m", liveStatus.first)
                            Text("Distance to Zone: $distStr")
                            Text(if (liveStatus.second) "Status: INSIDE RANGE" else "Status: OUTSIDE RANGE", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text("Waiting for location...")
                        }
                    }
                }
            }

            Text("Active Zones", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(locations) { location ->
                    ListItem(
                        headlineContent = { Text(location.name) },
                        supportingContent = { Text("Radius: ${location.radiusMeters}m") }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
