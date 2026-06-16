package com.example.kwarta.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class BudgetWithCategorySpend(
    val categoryId: Long,
    val categoryName: String,
    val limitAmount: Double,
    val currentSpend: Double,
    val colorHex: String
)

@Dao
interface BudgetDao {
    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity): Long

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId AND monthYear = :monthYear")
    suspend fun deleteBudget(categoryId: Long, monthYear: String): Int

    @Query("""
        SELECT 
            b.categoryId as categoryId, 
            c.name as categoryName, 
            b.limitAmount as limitAmount, 
            COALESCE(SUM(t.amount), 0.0) as currentSpend,
            c.colorHex as colorHex
        FROM budgets b
        JOIN categories c ON b.categoryId = c.id
        LEFT JOIN transactions t ON b.categoryId = t.categoryId 
            AND strftime('%Y-%m', t.date / 1000, 'unixepoch') = b.monthYear
        WHERE b.monthYear = :monthYear
        GROUP BY b.categoryId
    """)
    fun getBudgetsWithSpend(monthYear: String): Flow<List<BudgetWithCategorySpend>>
}
