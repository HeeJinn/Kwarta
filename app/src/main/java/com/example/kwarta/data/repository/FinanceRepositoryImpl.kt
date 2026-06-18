package com.example.kwarta.data.repository

import android.content.Context
import com.example.kwarta.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FinanceRepositoryImpl(
    private val context: Context,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) : FinanceRepository {
    
    private val prefs = context.getSharedPreferences("kwarta_prefs", Context.MODE_PRIVATE)
    
    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() {
        val existing = categoryDao.getActiveCategoriesByType("BOTH").firstOrNull() ?: emptyList()
        val existingIncome = categoryDao.getActiveCategoriesByType("INCOME").firstOrNull() ?: emptyList()
        val existingExpense = categoryDao.getActiveCategoriesByType("EXPENSE").firstOrNull() ?: emptyList()
        
        if (existing.isEmpty() && existingIncome.isEmpty() && existingExpense.isEmpty()) {
            val foodCat = CategoryEntity(name = "Food", iconName = "Restaurant", colorHex = "#FF5722", transactionType = "EXPENSE", priorityTag = "NEED")
            val defaultCategories = listOf(
                foodCat,
                CategoryEntity(name = "Rent", iconName = "Home", colorHex = "#3F51B5", transactionType = "EXPENSE", priorityTag = "NEED"),
                CategoryEntity(name = "Salary", iconName = "AttachMoney", colorHex = "#4CAF50", transactionType = "INCOME", priorityTag = "WANT"),
                CategoryEntity(name = "Transport", iconName = "DirectionsCar", colorHex = "#FFC107", transactionType = "EXPENSE", priorityTag = "NEED"),
                CategoryEntity(name = "Entertainment", iconName = "Movie", colorHex = "#9C27B0", transactionType = "EXPENSE", priorityTag = "WANT")
            )
            // Insert default categories
            defaultCategories.forEach { categoryDao.insert(it) }
        }
    }

    override fun getRecentTransactions(): Flow<List<TransactionEntity>> = 
        transactionDao.getRecentTransactions()

    override fun getAllTransactions(): Flow<List<TransactionEntity>> = 
        transactionDao.getAllTransactions()

    override fun getTransactionById(id: Long): Flow<TransactionEntity?> = 
        transactionDao.getTransactionById(id)

    override fun getActiveCategoriesByType(type: String): Flow<List<CategoryEntity>> = 
        categoryDao.getActiveCategoriesByType(type)

    override fun getAllActiveCategories(): Flow<List<CategoryEntity>> = 
        categoryDao.getAllActiveCategories()

    override suspend fun getCategoryById(id: Long): CategoryEntity? = 
        categoryDao.getCategoryById(id)

    override fun getBudgetsWithSpend(monthYear: String): Flow<List<BudgetWithCategorySpend>> = 
        budgetDao.getBudgetsWithSpend(monthYear)

    override suspend fun getBudgetWithSpendSync(categoryId: Long, monthYear: String): BudgetWithCategorySpend? = 
        budgetDao.getBudgetWithSpendSync(categoryId, monthYear)

    override suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val result = transactionDao.insert(transaction)
        com.example.kwarta.widget.KwartaWidgetUpdater.update(context)
        
        if (transaction.type == "EXPENSE") {
            try {
                val txLocalDate = java.time.Instant.ofEpochMilli(transaction.date)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                val monthYear = java.time.YearMonth.from(txLocalDate).toString()
                val budget = budgetDao.getBudgetWithSpendSync(transaction.categoryId, monthYear)
                if (budget != null) {
                    val limit = budget.limitAmount
                    val currentSpend = budget.currentSpend
                    val previousSpend = currentSpend - transaction.amount
                    
                    val currentPercent = if (limit > 0) ((currentSpend / limit) * 100).toInt() else 0
                    val previousPercent = if (limit > 0) ((previousSpend / limit) * 100).toInt() else 0
                    
                    if (previousPercent < 80 && currentPercent >= 80 && currentPercent < 100) {
                        com.example.kwarta.notification.NotificationHelper.showBudgetAlert(
                            context = context,
                            categoryId = transaction.categoryId,
                            categoryName = budget.categoryName,
                            percentage = currentPercent,
                            limit = limit,
                            currentSpend = currentSpend
                        )
                    } else if (previousPercent < 100 && currentPercent >= 100) {
                        com.example.kwarta.notification.NotificationHelper.showBudgetAlert(
                            context = context,
                            categoryId = transaction.categoryId,
                            categoryName = budget.categoryName,
                            percentage = currentPercent,
                            limit = limit,
                            currentSpend = currentSpend
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return result
    }

    override suspend fun insertCategory(category: CategoryEntity) = 
        categoryDao.insert(category)

    override suspend fun updateCategory(category: CategoryEntity) = 
        categoryDao.update(category)

    override suspend fun archiveCategory(id: Long) = 
        categoryDao.archive(id)

    override suspend fun upsertBudget(budget: BudgetEntity) = 
        budgetDao.upsertBudget(budget)

    override suspend fun deleteBudget(categoryId: Long, monthYear: String): Int {
        return budgetDao.deleteBudget(categoryId, monthYear)
    }

    override suspend fun getInitialBalanceOffset(): Double {
        return prefs.getString("initial_balance_offset", "0.0")?.toDoubleOrNull() ?: 0.0
    }

    override suspend fun setInitialBalanceOffset(offset: Double) {
        prefs.edit().putString("initial_balance_offset", offset.toString()).apply()
    }

    override fun getThemeMode(): Flow<String> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme_mode") {
                trySend(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        send(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    override fun getThemeColor(): Flow<String> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme_color") {
                trySend(prefs.getString("theme_color", "PURPLE") ?: "PURPLE")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        send(prefs.getString("theme_color", "PURPLE") ?: "PURPLE")
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setThemeColor(color: String) {
        prefs.edit().putString("theme_color", color).apply()
    }

    override fun getShowSafeToSpend(): Flow<Boolean> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "show_safe_to_spend") {
                trySend(prefs.getBoolean("show_safe_to_spend", true))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        send(prefs.getBoolean("show_safe_to_spend", true))
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setShowSafeToSpend(show: Boolean) {
        prefs.edit().putBoolean("show_safe_to_spend", show).apply()
    }

    override fun isOnboardingCompleted(): Flow<Boolean> = callbackFlow {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "onboarding_completed") {
                trySend(prefs.getBoolean("onboarding_completed", false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        send(prefs.getBoolean("onboarding_completed", false))
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    override suspend fun clearAllData() {
        transactionDao.deleteAll()
        budgetDao.deleteAll()
        categoryDao.deleteAll()
        seedDatabase()
        setInitialBalanceOffset(0.0)
        setOnboardingCompleted(false)
    }
}
