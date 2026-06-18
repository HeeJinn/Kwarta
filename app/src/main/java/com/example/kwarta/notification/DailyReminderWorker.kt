package com.example.kwarta.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("kwarta_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("daily_reminder_enabled", true)) {
            NotificationHelper.showDailyReminder(applicationContext)
        }
        return Result.success()
    }
}
