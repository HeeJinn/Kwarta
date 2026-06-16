package com.example.kwarta.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.kwarta.ui.screens.budgets.BudgetsScreen
import com.example.kwarta.ui.screens.dashboard.DashboardScreen
import com.example.kwarta.ui.screens.transactions.AddTransactionScreen
import com.example.kwarta.ui.screens.transactions.TransactionsScreen
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.roundToInt



sealed interface Destination {
    data object Dashboard : Destination
    data object Transactions : Destination
    data object Budgets : Destination
    data class AddTransaction(val type: String) : Destination
    data class TransactionDetail(val transactionId: Long) : Destination
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val destination: Destination
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Home, Destination.Dashboard),
    BottomNavItem("Transactions", Icons.AutoMirrored.Filled.List, Destination.Transactions),
    BottomNavItem("Budgets", Icons.Default.PieChart, Destination.Budgets)
)

@Composable
fun KwartaNavigation() {
    val backStack = remember { mutableStateListOf<Any>(Destination.Dashboard) }

    val density = LocalDensity.current
    val bottomBarHeight = 80.dp
    val bottomBarHeightPx = remember(density) { with(density) { bottomBarHeight.toPx() } }
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }

    val currentDestination = backStack.lastOrNull()
    val isTopLevel = currentDestination is Destination.Dashboard || 
                     currentDestination is Destination.Transactions || 
                     currentDestination is Destination.Budgets

    val bottomPaddingState = remember(density) {
        derivedStateOf { with(density) { (bottomBarHeightPx + bottomBarOffsetHeightPx).toDp() } }
    }

    LaunchedEffect(currentDestination) {
        bottomBarOffsetHeightPx = 0f
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = bottomBarOffsetHeightPx + delta
                bottomBarOffsetHeightPx = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = { key ->
                    when (key) {
                        is Destination.Dashboard -> NavEntry(key) {
                            DashboardScreen(
                                bottomBarPadding = { bottomPaddingState.value },
                                onAddTransaction = { type -> backStack.add(Destination.AddTransaction(type)) },
                                onTransactionClick = { id -> backStack.add(Destination.TransactionDetail(id)) },
                                onConfigureBudgets = {
                                    backStack.clear()
                                    backStack.add(Destination.Dashboard)
                                    backStack.add(Destination.Budgets)
                                },
                                onViewAllTransactions = {
                                    backStack.clear()
                                    backStack.add(Destination.Dashboard)
                                    backStack.add(Destination.Transactions)
                                }
                            )
                        }
                        is Destination.Transactions -> NavEntry(key) {
                            TransactionsScreen(
                                onTransactionClick = { id -> backStack.add(Destination.TransactionDetail(id)) }
                            )
                        }
                        is Destination.Budgets -> NavEntry(key) {
                            BudgetsScreen(bottomBarPadding = { bottomPaddingState.value })
                        }
                        is Destination.AddTransaction -> NavEntry(key) {
                            AddTransactionScreen(
                                transactionType = key.type,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        is Destination.TransactionDetail -> NavEntry(key) {
                            com.example.kwarta.ui.screens.transactions.TransactionDetailScreen(
                                transactionId = key.transactionId,
                                onNavigateBack = { backStack.removeLastOrNull() }
                            )
                        }
                        else -> NavEntry(Unit) { /* Handle unknown */ }
                    }
                }
            )
            }
            if (isTopLevel) {
                NavigationBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.roundToInt()) }
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination == item.destination,
                            onClick = {
                                if (currentDestination != item.destination) {
                                    // Prevent infinite stack by clearing and adding desired top-level destination
                                    backStack.clear()
                                    backStack.add(Destination.Dashboard) // Keep Dashboard as root
                                    if (item.destination != Destination.Dashboard) {
                                        backStack.add(item.destination)
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    }
}
