package com.example.kwarta.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kwarta.MainActivity
import com.example.kwarta.R
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
    const val CHANNEL_DAILY_REMINDERS = "daily_reminders"
    
    private const val DAILY_REMINDER_NOTIFICATION_ID = 9999

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Budget Alerts Channel
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warning notifications when spending gets close to or exceeds budget limits."
                enableLights(true)
                enableVibration(true)
            }
            
            // Daily Reminders Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log your transactions."
                enableLights(true)
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun showBudgetAlert(
        context: Context,
        categoryId: Long,
        categoryName: String,
        percentage: Int,
        limit: Double,
        currentSpend: Double
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        val limitStr = currencyFormatter.format(limit)
        val spentStr = currencyFormatter.format(currentSpend)

        val title = if (percentage >= 100) {
            "Budget Exceeded: $categoryName"
        } else {
            "Budget Warning: $categoryName"
        }

        val text = if (percentage >= 100) {
            "You have exceeded your monthly budget for $categoryName! Spent $spentStr of $limitStr."
        } else {
            "You have used $percentage% of your monthly budget for $categoryName. Spent $spentStr of $limitStr."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            categoryId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(categoryId.toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showDailyReminder(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Track Your Kwarta!"
        val text = "Don't forget to record today's expenses and income to keep your budget on track."

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
