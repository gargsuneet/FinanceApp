package com.financeapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.data.local.FinanceDatabase
import com.financeapp.data.remote.GoogleDriveService
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveAutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
                ?: return@withContext Result.success() // Not signed in — skip silently

            val token = try {
                GoogleAuthUtil.getToken(
                    applicationContext,
                    account.account!!,
                    "oauth2:https://www.googleapis.com/auth/drive.file"
                )
            } catch (e: Exception) {
                return@withContext Result.retry()
            }

            val dbFile = applicationContext.getDatabasePath(FinanceDatabase.DATABASE_NAME)
            if (!dbFile.exists()) return@withContext Result.success()
            val dbBytes = dbFile.readBytes()

            val folderId = GoogleDriveService.getOrCreateFolder(token)
                ?: return@withContext Result.retry()
            val success = GoogleDriveService.uploadBackup(token, dbBytes, folderId)

            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
