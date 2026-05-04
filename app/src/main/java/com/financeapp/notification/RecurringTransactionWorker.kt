package com.financeapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.FinanceApplication
import com.financeapp.data.local.entity.TransactionEntity
import java.util.Calendar

class RecurringTransactionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as FinanceApplication
            val now = System.currentTimeMillis()
            val dueTransactions = app.database.transactionDao().getRecurringDue(now)
            dueTransactions.forEach { entity ->
                val nextDate = computeNextDate(entity)
                val newEntity = entity.copy(id = 0, date = now, recurringNextDate = nextDate)
                app.database.transactionDao().insert(newEntity)
                val updated = entity.copy(recurringNextDate = nextDate)
                app.database.transactionDao().update(updated)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun computeNextDate(entity: TransactionEntity): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
        return when (entity.recurringPeriod) {
            "DAILY" -> { cal.add(Calendar.DAY_OF_YEAR, 1); cal.timeInMillis }
            "WEEKLY" -> { cal.add(Calendar.WEEK_OF_YEAR, 1); cal.timeInMillis }
            "MONTHLY" -> { cal.add(Calendar.MONTH, 1); cal.timeInMillis }
            "YEARLY" -> { cal.add(Calendar.YEAR, 1); cal.timeInMillis }
            else -> { cal.add(Calendar.MONTH, 1); cal.timeInMillis }
        }
    }
}
