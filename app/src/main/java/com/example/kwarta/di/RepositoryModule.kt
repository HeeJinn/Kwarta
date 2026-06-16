package com.example.kwarta.di

import com.example.kwarta.data.repository.FinanceRepository
import com.example.kwarta.data.repository.FinanceRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<FinanceRepository> { FinanceRepositoryImpl(get(), get(), get()) }
}
