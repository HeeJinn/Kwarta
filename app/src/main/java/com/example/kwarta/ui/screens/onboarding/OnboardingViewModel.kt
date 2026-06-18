package com.example.kwarta.ui.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.local.TransactionEntity
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: FinanceRepository,
    private val context: Context
) : ViewModel() {

    val themeMode = repository.getThemeMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")
    
    val themeColor = repository.getThemeColor()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PURPLE")

    private val prefs = context.getSharedPreferences("kwarta_prefs", Context.MODE_PRIVATE)

    private val _dailyReminderEnabled = MutableStateFlow(true)
    val dailyReminderEnabled = _dailyReminderEnabled.asStateFlow()

    private val _budgetAlertsEnabled = MutableStateFlow(true)
    val budgetAlertsEnabled = _budgetAlertsEnabled.asStateFlow()

    val showSafeToSpend = repository.getShowSafeToSpend()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultCategories = repository.getAllActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
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

    fun completeOnboarding(
        incomeTitle: String,
        incomeAmount: Double,
        budgetLimit: Double,
        expenseTitle: String,
        expenseAmount: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val allCats = repository.getAllActiveCategories().firstOrNull() ?: emptyList()
            val salaryCat = allCats.find { it.name.equals("Salary", ignoreCase = true) }
            val foodCat = allCats.find { it.name.equals("Food", ignoreCase = true) }

            // 1. Insert Income
            if (incomeAmount > 0.0 && salaryCat != null) {
                repository.insertTransaction(
                    TransactionEntity(
                        title = incomeTitle.ifBlank { "Starting Salary" },
                        amount = incomeAmount,
                        type = "INCOME",
                        categoryId = salaryCat.id,
                        date = System.currentTimeMillis(),
                        note = "Logged during onboarding sandbox",
                        merchantName = null,
                        imagePath = null,
                        status = "CLEARED"
                    )
                )
            }

            // 2. Insert Budget
            if (budgetLimit > 0.0 && foodCat != null) {
                val currentMonth = java.time.YearMonth.now().toString()
                repository.upsertBudget(
                    com.example.kwarta.data.local.BudgetEntity(
                        categoryId = foodCat.id,
                        limitAmount = budgetLimit,
                        monthYear = currentMonth
                    )
                )
            }

            // 3. Insert Expense
            if (expenseAmount > 0.0 && foodCat != null) {
                repository.insertTransaction(
                    TransactionEntity(
                        title = expenseTitle.ifBlank { "Groceries" },
                        amount = expenseAmount,
                        type = "EXPENSE",
                        categoryId = foodCat.id,
                        date = System.currentTimeMillis(),
                        note = "Logged during onboarding sandbox",
                        merchantName = null,
                        imagePath = null,
                        status = "CLEARED"
                    )
                )
            }

            // 4. Mark onboarding completed
            repository.setOnboardingCompleted(true)
            
            // 5. Update widget
            com.example.kwarta.widget.KwartaWidgetUpdater.update(context)
            
            onComplete()
        }
    }
}
