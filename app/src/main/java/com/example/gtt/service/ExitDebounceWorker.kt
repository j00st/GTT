package com.example.gtt.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gtt.data.AppDatabase
import com.example.gtt.data.VisitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExitDebounceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val locationId = inputData.getInt(KEY_LOCATION_ID, -1)
        val exitTime = inputData.getLong(KEY_EXIT_TIME, -1L)

        if (locationId == -1 || exitTime == -1L) {
            return@withContext Result.failure()
        }

        val db = AppDatabase.getDatabase(applicationContext).gttDao()
        val active = db.getActiveVisit()

        // Verify we are still in the 'EXIT' state (active visit still open for this location)
        // If the user re-entered, the work would have been cancelled by GeofenceBroadcastReceiver
        // but just in case, we verify
        if (active != null && active.locationId == locationId) {
            db.updateVisit(active.copy(exitTime = exitTime))
            TrackingForegroundService.stopService(applicationContext)
        }

        Result.success()
    }

    companion object {
        const val KEY_LOCATION_ID = "location_id"
        const val KEY_EXIT_TIME = "exit_time"
        const val WORK_NAME_PREFIX = "exit_debounce_"
    }
}
