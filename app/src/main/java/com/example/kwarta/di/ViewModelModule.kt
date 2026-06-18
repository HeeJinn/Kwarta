package com.example.kwarta.di

import com.example.kwarta.ui.screens.dashboard.DashboardViewModel
import com.example.kwarta.ui.screens.transactions.TransactionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { DashboardViewModel(get()) }
    viewModel { TransactionViewModel(get()) }
    viewModel { com.example.kwarta.ui.screens.transactions.TransactionsListViewModel(get()) }
    viewModel { com.example.kwarta.ui.screens.budgets.BudgetsViewModel(get()) }
    viewModel { com.example.kwarta.ui.screens.transactions.TransactionDetailViewModel(get()) }
    viewModel { com.example.kwarta.ui.screens.settings.SettingsViewModel(get(), get()) }
}
