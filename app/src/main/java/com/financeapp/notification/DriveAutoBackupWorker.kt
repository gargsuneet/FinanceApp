package com.financeapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveAutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Auto-backup via background worker requires a stored OAuth token.
        // Token retrieval via GoogleAuthUtil is not available in play-services-auth 21+.
        // Backup is handled manually through SettingsViewModel / DriveBackupScreen.
        Result.success()
    }
}
