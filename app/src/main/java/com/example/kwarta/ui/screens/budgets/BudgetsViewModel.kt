package com.example.kwarta.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwarta.data.local.BudgetEntity
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class BudgetsViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val currentMonth = YearMonth.now().toString()

    val budgetsWithSpend = repository.getBudgetsWithSpend(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Only expense categories should have budgets
    val categories = repository.getActiveCategoriesByType("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expose all active categories (both Expense and Income) for management
    val allCategories = repository.getAllActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            repository.upsertBudget(
                BudgetEntity(
                    categoryId = categoryId,
                    limitAmount = amount,
                    monthYear = currentMonth
                )
            )
        }
    }

    fun deleteBudget(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteBudget(categoryId, currentMonth)
        }
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    fun archiveCategory(id: Long) {
        viewModelScope.launch {
            repository.archiveCategory(id)
        }
    }
}
