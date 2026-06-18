package com.example.kwarta.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: FinanceRepository,
    private val context: Context
) : ViewModel() {

    val themeMode = repository.getThemeMode()
    val themeColor = repository.getThemeColor()
    val showSafeToSpend = repository.getShowSafeToSpend()

    private val _balanceOffset = MutableStateFlow(0.0)
    val balanceOffset = _balanceOffset.asStateFlow()

    private val prefs = context.getSharedPreferences("kwarta_prefs", Context.MODE_PRIVATE)

    private val _dailyReminderEnabled = MutableStateFlow(true)
    val dailyReminderEnabled = _dailyReminderEnabled.asStateFlow()

    private val _budgetAlertsEnabled = MutableStateFlow(true)
    val budgetAlertsEnabled = _budgetAlertsEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            _balanceOffset.value = repository.getInitialBalanceOffset()
            _dailyReminderEnabled.value = prefs.getBoolean("daily_reminder_enabled", true)
            _budgetAlertsEnabled.value = prefs.getBoolean("budget_alerts_enabled", true)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setThemeColor(color: String) {
        viewModelScope.launch {
            repository.setThemeColor(color)
        }
    }

    fun resetBalanceAdjustment() {
        viewModelScope.launch {
            repository.setInitialBalanceOffset(0.0)
            _balanceOffset.value = 0.0
            com.example.kwarta.widget.KwartaWidgetUpdater.update(context)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
            _dailyReminderEnabled.value = enabled
        }
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean("budget_alerts_enabled", enabled).apply()
            _budgetAlertsEnabled.value = enabled
        }
    }

    fun setShowSafeToSpend(show: Boolean) {
        viewModelScope.launch {
            repository.setShowSafeToSpend(show)
        }
    }

    fun factoryReset(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            repository.setShowSafeToSpend(true)
            _balanceOffset.value = 0.0
            _dailyReminderEnabled.value = true
            _budgetAlertsEnabled.value = true
            prefs.edit()
                .putBoolean("daily_reminder_enabled", true)
                .putBoolean("budget_alerts_enabled", true)
                .putString("theme_mode", "SYSTEM")
                .putString("theme_color", "PURPLE")
                .apply()
            com.example.kwarta.widget.KwartaWidgetUpdater.update(context)
            onComplete()
        }
    }
}
