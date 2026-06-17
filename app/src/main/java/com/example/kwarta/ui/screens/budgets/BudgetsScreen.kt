package com.example.kwarta.ui.screens.budgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.border
import com.example.kwarta.data.local.CategoryEntity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BudgetsScreen(
    onRegisterFabClick: (() -> Unit) -> Unit,
    parentScrollConnection: NestedScrollConnection? = null,
    viewModel: BudgetsViewModel = koinViewModel()
) {
    val budgets by viewModel.budgetsWithSpend.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))

    var showDialog by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var limitAmount by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Category Customization states
    var showCategoryManage by remember { mutableStateOf(false) }
    var showAddEditCategory by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var categoryName by remember { mutableStateOf("") }
    var categoryColor by remember { mutableStateOf("#FF5722") }
    var categoryType by remember { mutableStateOf("EXPENSE") }
    var categoryPriority by remember { mutableStateOf("WANT") }

    val onSetBudgetClick = {
        isEditing = false
        selectedCategoryId = categories.firstOrNull()?.id
        limitAmount = ""
        showDialog = true
    }

    DisposableEffect(categories) {
        onRegisterFabClick(onSetBudgetClick)
        onDispose {
            onRegisterFabClick {}
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
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Monthly Budgets") },
                actions = {
                    IconButton(onClick = { showCategoryManage = true }) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Manage Categories"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (budgets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No budgets configured for this month",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OverallBudgetSummaryCard(
                        budgets = budgets,
                        currencyFormatter = currencyFormatter
                    )
                }

                item {
                    Text(
                        text = "Category Limits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(budgets, key = { it.categoryId }) { budget ->
                    val percentage = if (budget.limitAmount > 0) {
                        (budget.currentSpend / budget.limitAmount).toFloat().coerceIn(0f, 1f)
                    } else 0f
                    
                    val progressColor = if (percentage >= 0.9f) {
                        MaterialTheme.colorScheme.error
                    } else if (percentage >= 0.75f) {
                        Color(0xFFFFA000) // Amber
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isEditing = true
                                selectedCategoryId = budget.categoryId
                                limitAmount = if (budget.limitAmount % 1 == 0.0) {
                                    budget.limitAmount.toLong().toString()
                                } else {
                                    budget.limitAmount.toString()
                                }
                                showDialog = true
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = budget.categoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(percentage * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = progressColor
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = progressColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: ${currencyFormatter.format(budget.currentSpend)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Limit: ${currencyFormatter.format(budget.limitAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (isEditing) "Edit Monthly Budget" else "Set Monthly Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Category", style = MaterialTheme.typography.titleSmall)
                            val catName = budgets.find { it.categoryId == selectedCategoryId }?.categoryName
                                ?: categories.find { it.id == selectedCategoryId }?.name
                                ?: "Category"
                            Text(
                                text = catName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Select Category", style = MaterialTheme.typography.titleSmall)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category ->
                                    FilterChip(
                                        selected = selectedCategoryId == category.id,
                                        onClick = { selectedCategoryId = category.id },
                                        label = { Text(category.name) }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = limitAmount,
                        onValueChange = { limitAmount = it },
                        label = { Text("Limit Amount") },
                        prefix = { Text("₱") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAmount = limitAmount.toDoubleOrNull()
                        val catId = selectedCategoryId
                        if (parsedAmount != null && catId != null) {
                            viewModel.addBudget(catId, parsedAmount)
                            showDialog = false
                        }
                    },
                    enabled = limitAmount.isNotBlank() && selectedCategoryId != null && limitAmount.toDoubleOrNull() != null
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                selectedCategoryId?.let { catId ->
                                    viewModel.deleteBudget(catId)
                                }
                                showDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Category Management Dialog
    if (showCategoryManage) {
        AlertDialog(
            onDismissRequest = { showCategoryManage = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manage Categories")
                    IconButton(onClick = {
                        editingCategory = null
                        categoryName = ""
                        categoryColor = "#FF5722"
                        categoryType = "EXPENSE"
                        categoryPriority = "WANT"
                        showAddEditCategory = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (allCategories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No categories found", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allCategories, key = { it.id }) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val catColor = try {
                                        Color(android.graphics.Color.parseColor(category.colorHex))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(catColor, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(category.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = "${category.transactionType.lowercase().replaceFirstChar { it.uppercase() }} • ${category.priorityTag}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = {
                                        editingCategory = category
                                        categoryName = category.name
                                        categoryColor = category.colorHex
                                        categoryType = category.transactionType
                                        categoryPriority = category.priorityTag
                                        showAddEditCategory = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Category", modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {
                                        viewModel.archiveCategory(category.id)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Category",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryManage = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add / Edit Category Dialog
    if (showAddEditCategory) {
        val colorsList = listOf("#FF5722", "#3F51B5", "#4CAF50", "#FFC107", "#9C27B0", "#00BCD4", "#E91E63", "#795548")
        AlertDialog(
            onDismissRequest = { showAddEditCategory = false },
            title = { Text(if (editingCategory == null) "Create Custom Category" else "Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Type", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = categoryType == "EXPENSE",
                                onClick = { categoryType = "EXPENSE" },
                                label = { Text("Expense") }
                            )
                            FilterChip(
                                selected = categoryType == "INCOME",
                                onClick = { categoryType = "INCOME" },
                                label = { Text("Income") }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Priority", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("NEED", "WANT", "SAVING").forEach { tag ->
                                FilterChip(
                                    selected = categoryPriority == tag,
                                    onClick = { categoryPriority = tag },
                                    label = { Text(tag) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select Color", style = MaterialTheme.typography.titleSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(colorsList) { hex ->
                                val colorVal = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = categoryColor.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(colorVal, shape = CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { categoryColor = hex }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            val category = CategoryEntity(
                                id = editingCategory?.id ?: 0L,
                                name = categoryName.trim(),
                                iconName = editingCategory?.iconName ?: "Star",
                                colorHex = categoryColor,
                                isCustom = true,
                                transactionType = categoryType,
                                priorityTag = categoryPriority,
                                isActive = true
                            )
                            viewModel.saveCategory(category)
                            showAddEditCategory = false
                        }
                    },
                    enabled = categoryName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditCategory = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverallBudgetSummaryCard(
    budgets: List<com.example.kwarta.data.local.BudgetWithCategorySpend>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val totalLimit = budgets.sumOf { it.limitAmount }
    val totalSpent = budgets.sumOf { it.currentSpend }
    val remaining = totalLimit - totalSpent
    val isOverspent = remaining < 0

    val progress = if (totalLimit > 0) {
        (totalSpent / totalLimit).toFloat().coerceIn(0f, 1f)
    } else 0f

    val progressColor = if (isOverspent || progress >= 0.9f) {
        MaterialTheme.colorScheme.error
    } else if (progress >= 0.75f) {
        Color(0xFFFFA000) // Amber
    } else {
        MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Monthly Spend Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Large Spent Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(totalSpent),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOverspent) "Over Budget" else "Remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(if (isOverspent) -remaining else remaining),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverspent) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% of limit spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Limit: ${currencyFormatter.format(totalLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Spending Breakdown (only if there are expenses spent)
            val spentBudgets = budgets.filter { it.currentSpend > 0 }
            if (spentBudgets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Spending Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Segmented Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    spentBudgets.forEach { budget ->
                        val weight = (budget.currentSpend / totalSpent).toFloat()
                        if (weight > 0f) {
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(budget.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weight)
                                    .background(catColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    spentBudgets.forEach { budget ->
                        val percentage = (budget.currentSpend / totalSpent * 100).toInt()
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(budget.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Text(
                                text = "${budget.categoryName} ($percentage%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

