package com.financeapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_BUDGET = "budget_alerts"
    const val CHANNEL_REMINDER = "daily_reminder"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_BUDGET, "Budget Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Alerts when budget limit is near or exceeded"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDER, "Daily Reminder", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Daily reminder to log transactions"
            }
        )
    }

    fun showBudgetAlert(context: Context, categoryName: String, percentage: Int) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Budget Alert: $categoryName")
                .setContentText("You've used $percentage% of your $categoryName budget")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(categoryName.hashCode(), notification)
        } catch (e: SecurityException) { /* Permission not granted */ }
    }

    fun showDailyReminder(context: Context) {
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Finance Tracker")
                .setContentText("Don't forget to log your transactions today!")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (e: SecurityException) { /* Permission not granted */ }
    }
}
