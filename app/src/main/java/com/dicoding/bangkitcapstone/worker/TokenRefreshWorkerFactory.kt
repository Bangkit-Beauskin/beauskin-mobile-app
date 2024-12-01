package com.dicoding.bangkitcapstone.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.dicoding.bangkitcapstone.data.repository.AuthRepository
import javax.inject.Inject

class TokenRefreshWorkerFactory @Inject constructor(
    private val authRepository: AuthRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            TokenRefreshWorker::class.java.name ->
                TokenRefreshWorker(appContext, workerParameters, authRepository)
            else -> null
        }
    }
}