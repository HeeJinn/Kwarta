package com.example.kwarta.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // INCOME, EXPENSE
    val categoryId: Long,
    val date: Long, // epoch
    val note: String?,
    val merchantName: String?,
    val imagePath: String?,
    val isRecurring: Boolean = false,
    val status: String, // CLEARED, PENDING
    val createdAt: Long = 0
)
