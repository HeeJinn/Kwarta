package com.example.kwarta.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.kwarta.ui.components.ExpressiveBudgetRing
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransaction: (String) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onConfigureBudgets: () -> Unit,
    onViewAllTransactions: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: () -> Dp = { 0.dp },
    viewModel: DashboardViewModel = koinViewModel()
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val budgets by viewModel.budgetsWithSpend.collectAsState()

    var isFabExpanded by remember { mutableStateOf(false) }

    // Computations
    val totalIncome = allTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = allTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // 1. Daily Spend Limit Calculation
    val currentYearMonth = YearMonth.now()
    val totalBudgetLimit = budgets.sumOf { it.limitAmount }
    val totalBudgetSpend = budgets.sumOf { it.currentSpend }
    val remainingBudget = (totalBudgetLimit - totalBudgetSpend).coerceAtLeast(0.0)
    val daysRemaining = (currentYearMonth.lengthOfMonth() - LocalDate.now().dayOfMonth + 1).coerceAtLeast(1)
    val dailyLimit = remainingBudget / daysRemaining

    // 2. Savings Rate Calculation
    val savingsRate = if (totalIncome > 0) {
        ((totalIncome - totalExpense) / totalIncome * 100).coerceAtLeast(0.0)
    } else 0.0

    // 3. Top Expense Category from Budget list
    val topBudgetExpense = budgets.filter { it.currentSpend > 0 }.maxByOrNull { it.currentSpend }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = bottomBarPadding() >= 76.dp,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = bottomBarPadding())
                ) {
                    AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = fadeIn() + expandVertically() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut() + shrinkVertically() + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Add Income Option
                            ExtendedFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onAddTransaction("INCOME")
                                },
                                icon = { Icon(Icons.Default.ArrowDownward, contentDescription = "Add Income") },
                                text = { Text("Add Income") },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            // Add Expense Option
                            ExtendedFloatingActionButton(
                                onClick = {
                                    isFabExpanded = false
                                    onAddTransaction("EXPENSE")
                                },
                                icon = { Icon(Icons.Default.ArrowUpward, contentDescription = "Add Expense") },
                                text = { Text("Add Expense") },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Main FAB
                    FloatingActionButton(
                        onClick = { isFabExpanded = !isFabExpanded },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isFabExpanded) 45f else 0f,
                            label = "FAB rotation"
                        )
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Toggle Transaction Menu",
                            modifier = Modifier.rotate(rotationAngle)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Hero Gradient Balance Card
                item {
                    val gradientBrush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        tonalElevation = 8.dp,
                        shadowElevation = 6.dp
                    ) {
                        Box(modifier = Modifier.background(gradientBrush).padding(24.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(
                                        text = "Total Balance",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = currencyFormatter.format(totalBalance),
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Income",
                                                tint = Color(0xFF81C784),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Income",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Text(
                                            text = currencyFormatter.format(totalIncome),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Expense",
                                                tint = Color(0xFFFF8A80),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Expense",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Text(
                                            text = currencyFormatter.format(totalExpense),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


            // Primary Budget Ring
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Primary Budget Limit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        
                        val topBudget = budgets.firstOrNull()
                        val budgetPercentage = if (topBudget != null && topBudget.limitAmount > 0) {
                            (topBudget.currentSpend / topBudget.limitAmount).toFloat().coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        ExpressiveBudgetRing(
                            percentage = budgetPercentage,
                            label = topBudget?.categoryName ?: "No Budget Set",
                            activeColor = if (budgetPercentage >= 0.8f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        // If there are other budgets, display summaries
                        if (budgets.size > 1) {
                            HorizontalDivider()
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                budgets.drop(1).take(2).forEach { budget ->
                                    val pct = if (budget.limitAmount > 0) (budget.currentSpend / budget.limitAmount).toFloat().coerceIn(0f, 1f) else 0f
                                    val catCol = try {
                                        Color(android.graphics.Color.parseColor(budget.colorHex))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(budget.categoryName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("${(pct * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = catCol, fontWeight = FontWeight.Bold)
                                        }
                                        LinearProgressIndicator(
                                            progress = { pct },
                                            modifier = Modifier.fillMaxWidth().height(6.dp),
                                            color = catCol,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onViewAllTransactions) {
                        Text("See All")
                    }
                }
                
                if (recentTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No recent transactions", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Recent Transactions list
            items(recentTransactions, key = { it.id }) { transaction ->
                val isIncome = transaction.type == "INCOME"
                val color = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                val sign = if (isIncome) "+" else "-"

                // Try to find the category color from budgets or databases
                val budget = budgets.find { it.categoryId == transaction.categoryId }
                val catColor = budget?.colorHex?.let {
                    try {
                        Color(android.graphics.Color.parseColor(it))
                    } catch (e: Exception) {
                        null
                    }
                } ?: MaterialTheme.colorScheme.primary

                ListItem(
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(catColor.copy(alpha = 0.15f), shape = CircleShape)
                                .border(1.dp, catColor.copy(alpha = 0.4f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = budget?.categoryName?.take(1) ?: "T",
                                color = catColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = transaction.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = { Text(dateFormatter.format(Date(transaction.date))) },
                    trailingContent = {
                        Text(
                            text = "$sign${currencyFormatter.format(transaction.amount)}",
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.clickable { onTransactionClick(transaction.id) }
                )
                HorizontalDivider()
            }
        }

        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isFabExpanded = false
                    }
            )
        }
    }
}
}
