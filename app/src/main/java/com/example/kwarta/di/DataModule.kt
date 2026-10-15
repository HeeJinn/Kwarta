package com.example.kwarta.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kwarta.data.local.AppDatabase
import com.example.kwarta.data.local.DefaultCategories
import com.example.kwarta.data.local.Migrations
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "kwarta.db"
        )
            // Explicit migrations only. No destructive fallback: a missing
            // migration must surface as a clear failure rather than silently
            // erasing the user's financial history.
            .addMigrations(*Migrations.ALL)
            .addCallback(object : RoomDatabase.Callback() {
                // Seeding runs once, inside the DB's own creation lifecycle,
                // before any DAO call — eliminating the previous first-launch
                // race where screens could observe an empty category list.
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    DefaultCategories.seedSql(db)
                }
            })
            .build()
    }

    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().budgetDao() }
}
