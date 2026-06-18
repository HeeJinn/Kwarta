package com.example.kwarta.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

class DashboardViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val currentMonth = YearMonth.now().toString()

    private val _balanceOffset = MutableStateFlow(0.0)
    val balanceOffset = _balanceOffset.asStateFlow()

    init {
        viewModelScope.launch {
            _balanceOffset.value = repository.getInitialBalanceOffset()
        }
    }

    fun updateBalanceOffset(offset: Double, context: android.content.Context) {
        viewModelScope.launch {
            repository.setInitialBalanceOffset(offset)
            _balanceOffset.value = offset
            com.example.kwarta.widget.KwartaWidgetUpdater.update(context)
        }
    }

    val showSafeToSpend = repository.getShowSafeToSpend()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val allTransactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions = repository.getAllTransactions()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetsWithSpend = repository.getBudgetsWithSpend(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, colorHex: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    iconName = "Star",
                    colorHex = colorHex,
                    transactionType = type,
                    priorityTag = "WANT",
                    isCustom = true
                )
            )
        }
    }
}
