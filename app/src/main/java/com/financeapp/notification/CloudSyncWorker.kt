package com.financeapp.notification

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.FinanceApplication
import com.financeapp.data.local.dataStore
import com.financeapp.data.remote.FirebaseSyncService
import kotlinx.coroutines.flow.first

class CloudSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val prefs = FinanceApplication.instance.dataStore.data.first()
            val token = prefs[stringPreferencesKey("sync_id_token")] ?: return Result.success()
            val uid = prefs[stringPreferencesKey("sync_uid")] ?: return Result.success()
            val dbFile = applicationContext.getDatabasePath("finance_database")
            if (!dbFile.exists()) return Result.success()
            val success = FirebaseSyncService.uploadBackup(token, uid, dbFile.readBytes())
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
