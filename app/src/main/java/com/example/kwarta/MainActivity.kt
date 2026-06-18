package com.example.kwarta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import org.koin.android.ext.android.inject
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kwarta.ui.navigation.Destination
import com.example.kwarta.ui.navigation.KwartaNavigation
import com.example.kwarta.ui.theme.KwartaTheme
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val repository: com.example.kwarta.data.repository.FinanceRepository by inject()
    private var initialDestination by mutableStateOf<Destination?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleDailyReminder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)
        checkNotificationPermission()
        scheduleDailyReminder()

        setContent {
            val themeMode by repository.getThemeMode().collectAsState(initial = "SYSTEM")
            val themeColor by repository.getThemeColor().collectAsState(initial = "PURPLE")
            KwartaTheme(themeMode = themeMode, themeColor = themeColor) {
                KwartaNavigation(
                    initialDestination = initialDestination,
                    onDestinationConsumed = { initialDestination = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val dest = when (intent?.action) {
            "com.example.kwarta.ACTION_ADD_EXPENSE",
            "android.intent.action.ASSIST" -> Destination.AddTransaction("EXPENSE")
            "com.example.kwarta.ACTION_ADD_INCOME" -> Destination.AddTransaction("INCOME")
            else -> null
        }
        if (dest != null) {
            initialDestination = dest
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun scheduleDailyReminder() {
        val workManager = WorkManager.getInstance(applicationContext)
        
        // Schedule for daily at 8:00 PM
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        val currentTime = System.currentTimeMillis()
        if (calendar.timeInMillis <= currentTime) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val initialDelay = calendar.timeInMillis - currentTime
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<com.example.kwarta.notification.DailyReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "KwartaDailyReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}
