package com.example.kwarta.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The seeded set of expense/income categories created on first database
 * creation (and re-applied after a Danger-Zone data wipe).
 *
 * Centralised here so the [androidx.room.RoomDatabase.Callback.onCreate]
 * path (which runs before DAOs exist) and the repository's wipe path
 * stay in sync without duplicating the seed list.
 */
object DefaultCategories {

    val list: List<CategoryEntity> = listOf(
        CategoryEntity(name = "Food",          iconName = "Restaurant",    colorHex = "#FF5722", transactionType = "EXPENSE", priorityTag = "NEED"),
        CategoryEntity(name = "Rent",          iconName = "Home",          colorHex = "#3F51B5", transactionType = "EXPENSE", priorityTag = "NEED"),
        CategoryEntity(name = "Salary",        iconName = "AttachMoney",   colorHex = "#4CAF50", transactionType = "INCOME",  priorityTag = "WANT"),
        CategoryEntity(name = "Transport",     iconName = "DirectionsCar", colorHex = "#FFC107", transactionType = "EXPENSE", priorityTag = "NEED"),
        CategoryEntity(name = "Entertainment", iconName = "Movie",         colorHex = "#9C27B0", transactionType = "EXPENSE", priorityTag = "WANT")
    )

    /**
     * Raw INSERT used by [androidx.room.RoomDatabase.Callback.onCreate], which
     * fires before the database is fully wired up and DAOs are unavailable.
     */
    fun seedSql(db: SupportSQLiteDatabase) {
        list.forEach { c ->
            db.execSQL(
                """
                INSERT INTO categories
                    (name, iconName, colorHex, isCustom, transactionType, priorityTag, isActive)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    c.name,
                    c.iconName,
                    c.colorHex,
                    if (c.isCustom) 1 else 0,
                    c.transactionType,
                    c.priorityTag,
                    if (c.isActive) 1 else 0
                )
            )
        }
    }
}
