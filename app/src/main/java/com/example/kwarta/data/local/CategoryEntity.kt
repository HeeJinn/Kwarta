package com.example.kwarta.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isCustom: Boolean = false,
    val transactionType: String, // INCOME, EXPENSE, BOTH
    val priorityTag: String, // NEED, WANT, SAVING
    val isActive: Boolean = true
)
