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

    @Query("SELECT SUM(exitTime - entryTime) FROM visits WHERE locationId = :locationId AND exitTime IS NOT NULL AND entryTime >= :startTime AND entryTime <= :endTime")
    fun getTotalDurationForLocationInRange(locationId: Int, startTime: Long, endTime: Long): Flow<Long?>
}
