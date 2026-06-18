package com.example.kwarta.data.repository

import com.example.kwarta.data.local.BudgetWithCategorySpend
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun getRecentTransactions(): Flow<List<TransactionEntity>>
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getTransactionById(id: Long): Flow<TransactionEntity?>
    fun getActiveCategoriesByType(type: String): Flow<List<CategoryEntity>>
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: Long): CategoryEntity?
    fun getBudgetsWithSpend(monthYear: String): Flow<List<BudgetWithCategorySpend>>
    suspend fun getBudgetWithSpendSync(categoryId: Long, monthYear: String): BudgetWithCategorySpend?
    suspend fun getInitialBalanceOffset(): Double
    suspend fun setInitialBalanceOffset(offset: Double)
    fun getThemeMode(): Flow<String>
    suspend fun setThemeMode(mode: String)
    fun getThemeColor(): Flow<String>
    suspend fun setThemeColor(color: String)
    fun getShowSafeToSpend(): Flow<Boolean>
    suspend fun setShowSafeToSpend(show: Boolean)
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity): Int
    suspend fun archiveCategory(id: Long): Int
    suspend fun upsertBudget(budget: com.example.kwarta.data.local.BudgetEntity): Long
    suspend fun deleteBudget(categoryId: Long, monthYear: String): Int
    suspend fun clearAllData()
}
