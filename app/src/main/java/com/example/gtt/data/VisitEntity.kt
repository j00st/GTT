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
