package com.example.kwarta.ui.screens.onboarding

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kwarta.R
import com.example.kwarta.ui.screens.settings.ColorPaletteDot
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val dailyReminderEnabled by viewModel.dailyReminderEnabled.collectAsStateWithLifecycle()
    val budgetAlertsEnabled by viewModel.budgetAlertsEnabled.collectAsStateWithLifecycle()
    val showSafeToSpend by viewModel.showSafeToSpend.collectAsStateWithLifecycle()

    var pageIndex by remember { mutableIntStateOf(0) }
    val pageCount = 6

    // Interactive simulator inputs
    var incomeTitle by remember { mutableStateOf("Salary") }
    var incomeAmountInput by remember { mutableStateOf("45000") }
    var budgetAmountInput by remember { mutableFloatStateOf(12000f) }
    var expenseTitle by remember { mutableStateOf("Groceries") }
    var expenseAmountInput by remember { mutableStateOf("1500") }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))

    val simulatedIncome = incomeAmountInput.toDoubleOrNull() ?: 0.0
    val simulatedExpense = expenseAmountInput.toDoubleOrNull() ?: 0.0
    val simulatedBalance = simulatedIncome - simulatedExpense

    // Background Blob Animations
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundBlobTransition")
    val animX1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlobX1"
    )
    val animY1 by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(9500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlobY1"
    )
    val animX2 by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlobX2"
    )
    val animY2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(10500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlobY2"
    )

    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawBehind {
                drawCircle(
                    color = Color(0xFFAB47BC).copy(alpha = 0.08f),
                    radius = size.width * 0.48f,
                    center = androidx.compose.ui.geometry.Offset(
                        x = size.width * 0.2f + animX1.dp.toPx(),
                        y = size.height * 0.25f + animY1.dp.toPx()
                    )
                )
                drawCircle(
                    color = Color(0xFFF4511E).copy(alpha = 0.08f),
                    radius = size.width * 0.52f,
                    center = androidx.compose.ui.geometry.Offset(
                        x = size.width * 0.8f + animX2.dp.toPx(),
                        y = size.height * 0.75f + animY2.dp.toPx()
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header Page Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until pageCount) {
                    val isCurrent = pageIndex == i
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isCurrent) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "IndicatorWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(indicatorWidth)
                            .background(
                                color = if (isCurrent) Color(0xFFAB47BC) else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Persistent Live Simulator Header (Only shows on simulator steps 1, 2, 3)
            if (pageIndex in 1..3) {
                LiveWalletHeader(
                    simulatedBalance = if (pageIndex >= 3) simulatedBalance else if (pageIndex >= 1) simulatedIncome else 0.0,
                    simulatedIncome = if (pageIndex >= 1) simulatedIncome else 0.0,
                    simulatedExpense = if (pageIndex >= 3) simulatedExpense else 0.0,
                    currencyFormatter = currencyFormatter
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Page Content with Expressive Animated Transitions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = pageIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(400)))
                                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(400)))
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(400)))
                                .togetherWith(slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(400)))
                        }
                    },
                    label = "OnboardingPageTransition"
                ) { targetPage ->
                    when (targetPage) {
                        0 -> WelcomePage()
                        1 -> IncomeSetupPage(
                            title = incomeTitle,
                            onTitleChange = { incomeTitle = it },
                            amount = incomeAmountInput,
                            onAmountChange = { incomeAmountInput = it }
                        )
                        2 -> BudgetSetupPage(
                            limitAmount = budgetAmountInput,
                            onLimitChange = { budgetAmountInput = it },
                            currencyFormatter = currencyFormatter
                        )
                        3 -> ExpenseSetupPage(
                            title = expenseTitle,
                            onTitleChange = { expenseTitle = it },
                            amount = expenseAmountInput,
                            onAmountChange = { expenseAmountInput = it },
                            budgetLimit = budgetAmountInput.toDouble(),
                            currencyFormatter = currencyFormatter
                        )
                        4 -> PersonalizationPage(
                            themeMode = themeMode,
                            themeColor = themeColor,
                            dailyReminderEnabled = dailyReminderEnabled,
                            budgetAlertsEnabled = budgetAlertsEnabled,
                            showSafeToSpend = showSafeToSpend,
                            viewModel = viewModel
                        )
                        5 -> AllSetPage()
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pageIndex > 0) {
                    TextButton(
                        onClick = { pageIndex-- },
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
                    ) {
                        Text("Back", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                val brandGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFAB47BC), Color(0xFFF4511E))
                )
                Button(
                    onClick = {
                        if (pageIndex < pageCount - 1) {
                            pageIndex++
                        } else {
                            viewModel.completeOnboarding(
                                incomeTitle = incomeTitle,
                                incomeAmount = simulatedIncome,
                                budgetLimit = budgetAmountInput.toDouble(),
                                expenseTitle = expenseTitle,
                                expenseAmount = simulatedExpense,
                                onComplete = onComplete
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(brandGradient, shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (pageIndex == pageCount - 1) "Enter Dashboard" else "Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (pageIndex == pageCount - 1) Icons.Default.DoneAll else Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWalletHeader(
    simulatedBalance: Double,
    simulatedIncome: Double,
    simulatedExpense: Double,
    currencyFormatter: NumberFormat
) {
    val brandGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFAB47BC),
            Color(0xFFF4511E)
        )
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        contentColor = Color.White,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(brandGradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Simulated Balance",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = currencyFormatter.format(simulatedBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormatter.format(simulatedIncome),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormatter.format(simulatedExpense),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White, shape = RoundedCornerShape(32.dp))
                .border(1.dp, Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(32.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Kwarta Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to Kwarta",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Let's experience budget planning in 3 quick steps.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun IncomeSetupPage(
    title: String,
    onTitleChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "1. Add Your Income",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Start by entering your monthly income or salary.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Income Source") },
            placeholder = { Text("e.g. Salary, Pocket Money") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFAB47BC),
                focusedLabelColor = Color(0xFFAB47BC),
                cursorColor = Color(0xFFAB47BC)
            )
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { input ->
                if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                    if (input.count { it == '.' } <= 1) {
                        onAmountChange(input)
                    }
                }
            },
            label = { Text("Amount") },
            prefix = { Text("₱ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFAB47BC),
                focusedLabelColor = Color(0xFFAB47BC),
                cursorColor = Color(0xFFAB47BC)
            )
        )
    }
}

@Composable
fun BudgetSetupPage(
    limitAmount: Float,
    onLimitChange: (Float) -> Unit,
    currencyFormatter: NumberFormat
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "2. Set a Food Budget",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Define your target spending limit for dining and groceries.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Food Category Limit", fontWeight = FontWeight.Bold)
                    Text(
                        text = currencyFormatter.format(limitAmount.toDouble()),
                        color = Color(0xFFAB47BC),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Slider(
                    value = limitAmount,
                    onValueChange = onLimitChange,
                    valueRange = 0f..50000f,
                    steps = 49,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFAB47BC),
                        activeTrackColor = Color(0xFFAB47BC),
                        inactiveTrackColor = Color(0xFFAB47BC).copy(alpha = 0.2f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )

                // Simulating a mini progress bar preview
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Food Budget Spent", style = MaterialTheme.typography.bodySmall)
                        Text("0% Spent", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = 0f,
                        color = Color(0xFFAB47BC),
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseSetupPage(
    title: String,
    onTitleChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    budgetLimit: Double,
    currencyFormatter: NumberFormat
) {
    val expenseVal = amount.toDoubleOrNull() ?: 0.0
    val budgetPct = if (budgetLimit > 0.0) (expenseVal / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f
    
    // Calculate simulated daily safe to spend allowance preview
    val dailyLimit = budgetLimit / 30.0
    val safeToSpendToday = (budgetLimit - expenseVal) / 30.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "3. Log an Expense",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Enter a sample expense to see how your balance and budget adapt.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Expense Title") },
                singleLine = true,
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFAB47BC),
                    focusedLabelColor = Color(0xFFAB47BC),
                    cursorColor = Color(0xFFAB47BC)
                )
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                        if (input.count { it == '.' } <= 1) {
                            onAmountChange(input)
                        }
                    }
                },
                label = { Text("Cost") },
                prefix = { Text("₱") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(0.8f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFAB47BC),
                    focusedLabelColor = Color(0xFFAB47BC),
                    cursorColor = Color(0xFFAB47BC)
                )
            )
        }

        // Real-time Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Real-Time Indicators Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFAB47BC)
                )
                
                // Budget Spent Preview
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Food Category Spent", style = MaterialTheme.typography.bodySmall)
                        Text("${(budgetPct * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = budgetPct,
                        color = if (budgetPct >= 0.8f) MaterialTheme.colorScheme.error else Color(0xFFAB47BC),
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }

                HorizontalDivider()

                // Safe to Spend Preview (Stack vertically to avoid overlaps)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Simulated Safe-to-Spend Today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currencyFormatter.format(safeToSpendToday.coerceAtLeast(0.0)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (safeToSpendToday <= 0.0) MaterialTheme.colorScheme.error else Color(0xFFAB47BC)
                        )
                        Text(
                            text = "Limit: ${currencyFormatter.format(dailyLimit)} / day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalizationPage(
    themeMode: String,
    themeColor: String,
    dailyReminderEnabled: Boolean,
    budgetAlertsEnabled: Boolean,
    showSafeToSpend: Boolean,
    viewModel: OnboardingViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "4. Personalize",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Customize the app's look and notification preferences.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Palette Dots
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Choose Accent Color", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ColorPaletteDot(
                        colorName = "Brand",
                        color = Color.Transparent,
                        isSelected = themeColor == "BRAND",
                        onClick = { viewModel.setThemeColor("BRAND") },
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFAB47BC), Color(0xFFF4511E))
                        )
                    )
                    ColorPaletteDot(
                        colorName = "Purple",
                        color = Color(0xFF6650a4),
                        isSelected = themeColor == "PURPLE",
                        onClick = { viewModel.setThemeColor("PURPLE") }
                    )
                    ColorPaletteDot(
                        colorName = "Blue",
                        color = Color(0xFF1E88E5),
                        isSelected = themeColor == "BLUE",
                        onClick = { viewModel.setThemeColor("BLUE") }
                    )
                    ColorPaletteDot(
                        colorName = "Green",
                        color = Color(0xFF2E7D32),
                        isSelected = themeColor == "GREEN",
                        onClick = { viewModel.setThemeColor("GREEN") }
                    )
                    ColorPaletteDot(
                        colorName = "Orange",
                        color = Color(0xFFE65100),
                        isSelected = themeColor == "ORANGE",
                        onClick = { viewModel.setThemeColor("ORANGE") }
                    )
                    ColorPaletteDot(
                        colorName = "Black",
                        color = Color(0xFF212121),
                        isSelected = themeColor == "BLACK",
                        onClick = { viewModel.setThemeColor("BLACK") }
                    )
                }
            }
        }

        // Theme Mode Segmented Toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val options = listOf(
                    Triple("SYSTEM", "System", Icons.Default.Settings),
                    Triple("LIGHT", "Light", Icons.Default.LightMode),
                    Triple("DARK", "Dark", Icons.Default.DarkMode)
                )
                options.forEach { (mode, label, icon) ->
                    val isSelected = themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) Color(0xFFAB47BC) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Notification toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Reminders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = dailyReminderEnabled,
                        onCheckedChange = { viewModel.setDailyReminderEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFAB47BC)
                        )
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Budget Alerts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = budgetAlertsEnabled,
                        onCheckedChange = { viewModel.setBudgetAlertsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFAB47BC)
                        )
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Safe-to-Spend Dial", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = showSafeToSpend,
                        onCheckedChange = { viewModel.setShowSafeToSpend(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFAB47BC)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AllSetPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val brandGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFAB47BC), Color(0xFFF4511E))
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(brandGradient, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "All Set!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your sandbox entries are saved. Let's go to your dashboard!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

