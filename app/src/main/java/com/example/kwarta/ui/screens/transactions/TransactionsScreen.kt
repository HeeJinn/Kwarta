package com.example.kwarta.ui.screens.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    onTransactionClick: (Long) -> Unit,
    parentScrollConnection: NestedScrollConnection? = null,
    viewModel: TransactionsListViewModel = koinViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryIdFilter.collectAsStateWithLifecycle()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val customDate by viewModel.customDate.collectAsStateWithLifecycle()
    val customMonth by viewModel.customMonth.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    // Clear search query and state when screen is entered / re-entered
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.setSearchQuery("")
        isSearchActive = false
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val customDateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val customMonthFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") }

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { item ->
            val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            format.format(Date(item.transaction.date))
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .then(
                if (parentScrollConnection != null) {
                    Modifier.nestedScroll(parentScrollConnection)
                } else {
                    Modifier
                }
            ),
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search transactions...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel search")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = { Text("All Transactions") },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Tune, contentDescription = "Filter")
                                val hasActiveFilters = selectedType != "ALL" || selectedCategoryId != null || selectedDateFilter != "ALL"
                                if (hasActiveFilters) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Transaction List or Empty Placeholder
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No transactions found", color = MaterialTheme.colorScheme.outline)
                        if (selectedType != "ALL" || selectedCategoryId != null || selectedDateFilter != "ALL" || searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setSelectedTypeFilter("ALL")
                                viewModel.setSelectedCategoryIdFilter(null)
                                viewModel.setSelectedDateFilter("ALL")
                                isSearchActive = false
                            }) {
                                Text("Clear all filters")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    groupedTransactions.forEach { (dateString, transactionsForDate) ->
                        stickyHeader {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                tonalElevation = 2.dp
                            ) {
                                Text(
                                    text = dateString,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        items(transactionsForDate, key = { it.transaction.id }) { item ->
                            val tx = item.transaction
                            val cat = item.category
                            val isIncome = tx.type == "INCOME"
                            val color = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            val sign = if (isIncome) "+" else "-"

                            ListItem(
                                leadingContent = {
                                    val catColor = cat?.colorHex?.let {
                                        try {
                                            Color(android.graphics.Color.parseColor(it))
                                        } catch (e: Exception) {
                                            null
                                        }
                                    } ?: MaterialTheme.colorScheme.primary
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(catColor.copy(alpha = 0.15f), shape = CircleShape)
                                            .border(1.dp, catColor.copy(alpha = 0.4f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cat?.name?.take(1) ?: "T",
                                            color = catColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                },
                                headlineContent = {
                                    Text(
                                        text = tx.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                supportingContent = { 
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(timeFormatter.format(Date(tx.date)))
                                        cat?.name?.let {
                                            Text("•")
                                            Text(it, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                },
                                trailingContent = {
                                    Text(
                                        text = "$sign${currencyFormatter.format(tx.amount)}",
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                modifier = Modifier.clickable { onTransactionClick(tx.id) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    val hasActiveFilters = selectedType != "ALL" || selectedCategoryId != null || selectedDateFilter != "ALL"
                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                viewModel.setSelectedTypeFilter("ALL")
                                viewModel.setSelectedCategoryIdFilter(null)
                                viewModel.setSelectedDateFilter("ALL")
                            }
                        ) {
                            Text("Reset All")
                        }
                    }
                }

                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dateRanges = listOf(
                        "ALL" to "All Time",
                        "TODAY" to "Today",
                        "WEEK" to "This Week",
                        "MONTH" to "This Month"
                    )
                    dateRanges.forEach { (rangeVal, label) ->
                        FilterChip(
                            selected = selectedDateFilter == rangeVal,
                            onClick = { viewModel.setSelectedDateFilter(rangeVal) },
                            label = { Text(label) }
                        )
                    }

                    // Specific Date Chip
                    val dateChipLabel = if (selectedDateFilter == "CUSTOM_DATE" && customDate != null) {
                        "Date: ${customDate!!.format(customDateFormatter)}"
                    } else {
                        "Choose Date..."
                    }
                    FilterChip(
                        selected = selectedDateFilter == "CUSTOM_DATE",
                        onClick = { showDatePicker = true },
                        label = { Text(dateChipLabel) }
                    )

                    // Specific Month Chip
                    val monthChipLabel = if (selectedDateFilter == "CUSTOM_MONTH" && customMonth != null) {
                        "Month: ${customMonth!!.format(customMonthFormatter)}"
                    } else {
                        "Choose Month..."
                    }
                    FilterChip(
                        selected = selectedDateFilter == "CUSTOM_MONTH",
                        onClick = { showMonthPicker = true },
                        label = { Text(monthChipLabel) }
                    )
                }

                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("ALL" to "All Types", "INCOME" to "Income", "EXPENSE" to "Expense")
                    types.forEach { (typeVal, label) ->
                        FilterChip(
                            selected = selectedType == typeVal,
                            onClick = { viewModel.setSelectedTypeFilter(typeVal) },
                            label = { Text(label) }
                        )
                    }
                }

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.setSelectedCategoryIdFilter(null) },
                        label = { Text("All Categories") }
                    )
                    categories.forEach { category ->
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = {
                                viewModel.setSelectedCategoryIdFilter(
                                    if (selectedCategoryId == category.id) null else category.id
                                )
                            },
                            label = { Text(category.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.2f),
                                selectedLabelColor = catColor,
                                selectedLeadingIconColor = catColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setCustomDate(localDate)
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showMonthPicker) {
        val currentMonth = customMonth?.month ?: java.time.LocalDate.now().month
        val currentYear = customMonth?.year ?: java.time.LocalDate.now().year
        MonthPickerDialog(
            initialMonth = currentMonth,
            initialYear = currentYear,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month, year ->
                showMonthPicker = false
                viewModel.setCustomMonth(java.time.YearMonth.of(year, month))
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerDialog(
    initialMonth: java.time.Month,
    initialYear: Int,
    onDismiss: () -> Unit,
    onMonthSelected: (java.time.Month, Int) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableStateOf(initialYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month & Year") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Year Selection
                Text("Select Year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val currentYear = LocalDate.now().year
                val years = (currentYear - 4..currentYear + 1).toList()
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    years.forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { selectedYear = year },
                            label = { Text(year.toString()) }
                        )
                    }
                }

                // Month Selection
                Text("Select Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    java.time.Month.values().forEach { month ->
                        val monthLabel = month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                        FilterChip(
                            selected = selectedMonth == month,
                            onClick = { selectedMonth = month },
                            label = { Text(monthLabel) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onMonthSelected(selectedMonth, selectedYear) }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
