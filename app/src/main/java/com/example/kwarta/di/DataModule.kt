package com.example.kwarta.di

import androidx.room.Room
import com.example.kwarta.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "kwarta.db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().budgetDao() }
}
