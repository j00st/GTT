package com.example.gtt.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gtt.data.AppDatabase
import com.example.gtt.data.LocationEntity
import com.example.gtt.data.VisitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application).gttDao()

    val locations: StateFlow<List<LocationEntity>> = db.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeVisit: StateFlow<VisitEntity?> = db.getActiveVisitFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun punchOut(visit: VisitEntity) {
        viewModelScope.launch {
            db.updateVisit(visit.copy(exitTime = System.currentTimeMillis(), isManualPunchOut = true))
        }
    }

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal.timeInMillis
    }

    // A flow of total hours this week across all locations
    val totalHoursThisWeek: StateFlow<Double> = locations.flatMapLatest { locs ->
        if (locs.isEmpty()) {
            flowOf(0.0)
        } else {
            // In a real app we'd combine all flows or have a single query that sums across all locations
            // For simplicity, we just flow the sum query across all visits for the week
            db.getAllVisits().map { visits ->
                val startOfWeek = getStartOfWeek()
                val endOfWeek = System.currentTimeMillis()
                
                var totalDurationMs = 0L
                for (v in visits) {
                    if (v.entryTime >= startOfWeek && v.exitTime != null && v.entryTime <= endOfWeek) {
                        totalDurationMs += (v.exitTime - v.entryTime)
                    }
                }
                totalDurationMs / (1000.0 * 60 * 60)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
