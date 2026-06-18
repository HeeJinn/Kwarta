package com.example.kwarta.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showDailyReminder(applicationContext)
        return Result.success()
    }
}
