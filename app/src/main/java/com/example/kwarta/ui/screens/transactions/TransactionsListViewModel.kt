package com.example.kwarta.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.local.TransactionEntity
import com.example.kwarta.data.repository.FinanceRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TransactionWithCategory(
    val transaction: TransactionEntity,
    val category: CategoryEntity?
)

class TransactionsListViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val selectedTypeFilter: StateFlow<String> = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryIdFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryIdFilter: StateFlow<Long?> = _selectedCategoryIdFilter.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow("ALL") // "ALL", "TODAY", "WEEK", "MONTH", "CUSTOM_DATE", "CUSTOM_MONTH"
    val selectedDateFilter: StateFlow<String> = _selectedDateFilter.asStateFlow()

    private val _customDate = MutableStateFlow<LocalDate?>(null)
    val customDate: StateFlow<LocalDate?> = _customDate.asStateFlow()

    private val _customMonth = MutableStateFlow<YearMonth?>(null)
    val customMonth: StateFlow<YearMonth?> = _customMonth.asStateFlow()

    val categories = repository.getAllActiveCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions = combine(
        repository.getAllTransactions(),
        categories,
        _searchQuery,
        _selectedTypeFilter,
        _selectedCategoryIdFilter,
        _selectedDateFilter,
        _customDate,
        _customMonth
    ) { flowsArray ->
        @Suppress("UNCHECKED_CAST")
        val txList = flowsArray[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val catList = flowsArray[1] as List<CategoryEntity>
        val query = flowsArray[2] as String
        val type = flowsArray[3] as String
        val catId = flowsArray[4] as Long?
        val dateFilter = flowsArray[5] as String
        val customDateVal = flowsArray[6] as LocalDate?
        val customMonthVal = flowsArray[7] as YearMonth?

        val catMap = catList.associateBy { it.id }
        
        // Calculate timestamp thresholds
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val weekStart = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        txList.map { tx ->
            TransactionWithCategory(tx, catMap[tx.categoryId])
        }.filter { item ->
            // Filter by type
            val matchesType = when (type) {
                "INCOME" -> item.transaction.type == "INCOME"
                "EXPENSE" -> item.transaction.type == "EXPENSE"
                else -> true
            }
            // Filter by category
            val matchesCat = if (catId != null) item.transaction.categoryId == catId else true
            
            // Filter by date range
            val txLocalDate = Instant.ofEpochMilli(item.transaction.date).atZone(ZoneId.systemDefault()).toLocalDate()
            val matchesDate = when (dateFilter) {
                "TODAY" -> item.transaction.date >= todayStart
                "WEEK" -> item.transaction.date >= weekStart
                "MONTH" -> item.transaction.date >= monthStart
                "CUSTOM_DATE" -> customDateVal != null && txLocalDate == customDateVal
                "CUSTOM_MONTH" -> customMonthVal != null && YearMonth.from(txLocalDate) == customMonthVal
                else -> true
            }

            // Filter by search query (merchantName, note, or categoryName)
            val matchesQuery = if (query.isNotBlank()) {
                (item.transaction.merchantName?.contains(query, ignoreCase = true) == true) ||
                (item.transaction.note?.contains(query, ignoreCase = true) == true) ||
                (item.category?.name?.contains(query, ignoreCase = true) == true)
            } else true

            matchesType && matchesCat && matchesDate && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTypeFilter(type: String) {
        _selectedTypeFilter.value = type
    }

    fun setSelectedCategoryIdFilter(categoryId: Long?) {
        _selectedCategoryIdFilter.value = categoryId
    }

    fun setSelectedDateFilter(dateFilter: String) {
        _selectedDateFilter.value = dateFilter
        if (dateFilter != "CUSTOM_DATE" && dateFilter != "CUSTOM_MONTH") {
            _customDate.value = null
            _customMonth.value = null
        }
    }

    fun setCustomDate(date: LocalDate?) {
        _customDate.value = date
        if (date != null) {
            _selectedDateFilter.value = "CUSTOM_DATE"
            _customMonth.value = null
        }
    }

    fun setCustomMonth(month: YearMonth?) {
        _customMonth.value = month
        if (month != null) {
            _selectedDateFilter.value = "CUSTOM_MONTH"
            _customDate.value = null
        }
    }
}
