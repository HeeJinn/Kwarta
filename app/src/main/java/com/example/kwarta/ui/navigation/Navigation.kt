package com.example.kwarta.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.kwarta.ui.screens.budgets.BudgetsScreen
import com.example.kwarta.ui.screens.dashboard.DashboardScreen
import com.example.kwarta.ui.screens.transactions.AddTransactionScreen
import com.example.kwarta.ui.screens.transactions.TransactionsScreen



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
    BottomNavItem("Dashboard", Icons.Rounded.Home, Destination.Dashboard),
    BottomNavItem("Transactions", Icons.AutoMirrored.Rounded.ReceiptLong, Destination.Transactions),
    BottomNavItem("Budgets", Icons.Rounded.AccountBalanceWallet, Destination.Budgets)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KwartaNavigation(
    initialDestination: Destination? = null,
    onDestinationConsumed: () -> Unit = {}
) {
    val backStack = remember { mutableStateListOf<Any>(Destination.Dashboard) }

    LaunchedEffect(initialDestination) {
        if (initialDestination != null) {
            backStack.clear()
            backStack.add(Destination.Dashboard)
            backStack.add(initialDestination)
            onDestinationConsumed()
        }
    }

    val density = LocalDensity.current
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)
    var isFabExpanded by remember { mutableStateOf(false) }
    val expansionProgress by animateFloatAsState(
        targetValue = if (isFabExpanded) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "FabMenuExpansion"
    )
    var onBudgetsFabClick by remember { mutableStateOf<(() -> Unit)?>(null) }
    var triggerSetBudgetOnLoad by remember { mutableStateOf(false) }

    val currentDestination = backStack.lastOrNull()
    val isTopLevel = currentDestination is Destination.Dashboard || 
                     currentDestination is Destination.Transactions || 
                     currentDestination is Destination.Budgets

    LaunchedEffect(currentDestination) {
        scrollBehavior.state.offset = 0f
    }

    LaunchedEffect(onBudgetsFabClick) {
        if (triggerSetBudgetOnLoad && onBudgetsFabClick != null) {
            onBudgetsFabClick?.invoke()
            triggerSetBudgetOnLoad = false
        }
    }

    BackHandler(isFabExpanded) {
        isFabExpanded = false
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior)
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is Destination.Dashboard -> NavEntry(key) {
                                DashboardScreen(
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
                                    onTransactionClick = { id -> backStack.add(Destination.TransactionDetail(id)) },
                                    parentScrollConnection = scrollBehavior
                                )
                            }
                            is Destination.Budgets -> NavEntry(key) {
                                BudgetsScreen(
                                    onRegisterFabClick = { callback -> onBudgetsFabClick = callback },
                                    parentScrollConnection = scrollBehavior
                                )
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

            if (isTopLevel) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    floatingActionButton = {
                        FloatingActionButtonMenu(
                            modifier = (if (expansionProgress > 0f) {
                                Modifier.wrapContentSize(
                                    align = Alignment.BottomCenter,
                                    unbounded = true
                                )
                            } else {
                                Modifier
                            }).offset(y = 12.dp),
                            expanded = isFabExpanded,
                            button = {
                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                    tooltip = {
                                        PlainTooltip(
                                            modifier = Modifier.semantics {
                                                liveRegion = LiveRegionMode.Assertive
                                                paneTitle = "Actions Menu"
                                            }
                                        ) {
                                            Text("Actions Menu")
                                        }
                                    },
                                    state = rememberTooltipState()
                                ) {
                                    ToggleFloatingActionButton(
                                        modifier = Modifier.semantics {
                                            stateDescription = if (isFabExpanded) "Expanded" else "Collapsed"
                                            contentDescription = "Actions Menu"
                                        },
                                        checked = isFabExpanded,
                                        onCheckedChange = { isFabExpanded = !isFabExpanded }
                                    ) {
                                        val imageVector by remember {
                                            derivedStateOf {
                                                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                                            }
                                        }
                                        Icon(
                                            painter = rememberVectorPainter(imageVector),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .animateIcon({ checkedProgress })
                                        )
                                    }
                                }
                            }
                        ) {
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    isFabExpanded = false
                                    backStack.add(Destination.AddTransaction("INCOME"))
                                },
                                icon = { Icon(Icons.Default.Paid, contentDescription = "Add Income") },
                                text = { Text("Add Income") }
                            )
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    isFabExpanded = false
                                    backStack.add(Destination.AddTransaction("EXPENSE"))
                                },
                                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Add Expense") },
                                text = { Text("Add Expense") }
                            )
                            FloatingActionButtonMenuItem(
                                onClick = {
                                    isFabExpanded = false
                                    if (currentDestination is Destination.Budgets) {
                                        onBudgetsFabClick?.invoke()
                                    } else {
                                        triggerSetBudgetOnLoad = true
                                        if (currentDestination != Destination.Budgets) {
                                            backStack.clear()
                                            backStack.add(Destination.Dashboard) // Keep Dashboard as root
                                            backStack.add(Destination.Budgets)
                                        }
                                    }
                                },
                                icon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = "Set Budget") },
                                text = { Text("Set Budget") }
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -ScreenOffset)
                        .zIndex(1f),
                    colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    scrollBehavior = scrollBehavior,
                    content = {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentDestination == item.destination
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                tooltip = {
                                    PlainTooltip(
                                        modifier = Modifier.semantics {
                                            liveRegion = LiveRegionMode.Assertive
                                            paneTitle = item.title
                                        }
                                    ) {
                                        Text(item.title)
                                    }
                                },
                                state = rememberTooltipState()
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentDestination != item.destination) {
                                            backStack.clear()
                                            backStack.add(Destination.Dashboard) // Keep Dashboard as root
                                            if (item.destination != Destination.Dashboard) {
                                                backStack.add(item.destination)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
