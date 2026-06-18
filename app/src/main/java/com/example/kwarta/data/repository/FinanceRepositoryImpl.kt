package com.example.kwarta.data.repository

import android.content.Context
import com.example.kwarta.data.local.*
import kotlinx.coroutines.flow.Flow

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
}
