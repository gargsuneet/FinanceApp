package com.financeapp

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinanceApplication : Application() {
    override fun onCreate() {
        // Install crash handler BEFORE Hilt init (super.onCreate) so we catch Hilt crashes too
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sb = StringBuilder()
                sb.append("Thread: ${thread.name}\n\n")
                sb.append("${throwable.javaClass.name}: ${throwable.message}\n")
                throwable.stackTrace.take(20).forEach { sb.append("  at $it\n") }
                var cause = throwable.cause
                var depth = 0
                while (cause != null && depth < 3) {
                    sb.append("\nCaused by: ${cause.javaClass.name}: ${cause.message}\n")
                    cause.stackTrace.take(10).forEach { sb.append("  at $it\n") }
                    cause = cause.cause
                    depth++
                }
                getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_crash", sb.toString())
                    .commit()
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
        super.onCreate()
    }
}
