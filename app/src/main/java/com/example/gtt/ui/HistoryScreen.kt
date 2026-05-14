package com.example.gtt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gtt.data.VisitEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    visits: List<VisitEntity>,
    onBackClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(visits) { visit ->
                val entryStr = dateFormat.format(Date(visit.entryTime))
                val exitStr = visit.exitTime?.let { dateFormat.format(Date(it)) } ?: "Ongoing"
                
                ListItem(
                    headlineContent = { Text("Location ID: ${visit.locationId}") },
                    supportingContent = { Text("Entry: $entryStr\nExit: $exitStr") }
                )
                HorizontalDivider()
            }
        }
    }
}
