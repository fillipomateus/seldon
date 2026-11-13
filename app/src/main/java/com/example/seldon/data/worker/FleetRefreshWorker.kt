package com.example.seldon.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.seldon.data.repository.DeviceRepositoryImpl

class FleetRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = DeviceRepositoryImpl(applicationContext)
            repository.getDevices()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}