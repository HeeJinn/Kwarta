package com.example.kwarta.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migration registry.
 *
 * The schema baseline under version control is **v2** — the database has
 * never shipped at v1 — so there is no historical 1→2 migration to define.
 *
 * When the next schema change lands:
 *  1. Bump [AppDatabase] version to 3.
 *  2. Add the migration here:
 *  ```
 *  val MIGRATION_2_3 = object : Migration(2, 3) {
 *      override fun migrate(db: SupportSQLiteDatabase) {
 *          db.execSQL("ALTER TABLE transactions ADD COLUMN note TEXT")
 *      }
 *  }
 *  ```
 *  3. Register it in [ALL].
 *
 * Because `fallbackToDestructiveMigration()` has been removed, forgetting to
 * add a migration will surface as a clear `IllegalStateException` instead of
 * silently wiping user data — the desired, safe behaviour for a finance app.
 */
object Migrations {

    val ALL: Array<Migration> = arrayOf(
        // Register future migrations here, e.g. MIGRATION_2_3.
    )
}
