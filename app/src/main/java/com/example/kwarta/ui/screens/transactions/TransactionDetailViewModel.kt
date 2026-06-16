package com.example.kwarta.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.local.TransactionEntity
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _transaction = MutableStateFlow<TransactionEntity?>(null)
    val transaction: StateFlow<TransactionEntity?> = _transaction.asStateFlow()

    private val _category = MutableStateFlow<CategoryEntity?>(null)
    val category: StateFlow<CategoryEntity?> = _category.asStateFlow()

    private var collectJob: Job? = null

    fun loadTransaction(transactionId: Long) {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            repository.getTransactionById(transactionId).collect { tx ->
                _transaction.value = tx
                tx?.let {
                    _category.value = repository.getCategoryById(it.categoryId)
                }
            }
        }
    }
}
