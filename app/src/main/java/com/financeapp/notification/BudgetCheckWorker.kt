package com.financeapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financeapp.FinanceApplication
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BudgetCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as FinanceApplication
            val cal = Calendar.getInstance()
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)
            val budgets = app.getBudgetsUseCase(month, year).first()
            budgets.forEach { budget ->
                if (budget.amount > 0) {
                    val percentage = ((budget.spent / budget.amount) * 100).toInt()
                    if (percentage >= 80) {
                        NotificationHelper.showBudgetAlert(applicationContext, budget.categoryName, percentage)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
