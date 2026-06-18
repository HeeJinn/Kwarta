package com.example.kwarta.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SafeToSpendDial(
    safeToSpend: Double,
    dailyLimit: Double,
    spentToday: Double,
    onSetBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))
    val hasBudget = dailyLimit > 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Safe-to-Spend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (hasBudget) {
                    val statusText = when {
                        safeToSpend < 0.0 -> "Overspent"
                        safeToSpend < dailyLimit * 0.5 -> "Warning"
                        else -> "On Track"
                    }
                    val statusColor = when {
                        safeToSpend < 0.0 -> Color(0xFFFF1744)
                        safeToSpend < dailyLimit * 0.5 -> Color(0xFFFF9100)
                        else -> Color(0xFF00E676)
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!hasBudget) {
                // Empty state when no budget is set
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Budget Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set a monthly budget to calculate your daily Safe-to-Spend limit and track real-time allowances.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onSetBudgetClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Budget Limit")
                    }
                }
            } else {
                // Active Safe-to-Spend state
                val percentage = if (safeToSpend > 0.0) {
                    (safeToSpend / dailyLimit).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }

                // Animate progress arc
                val animatedPercentage = remember { Animatable(0f) }
                LaunchedEffect(percentage) {
                    animatedPercentage.animateTo(
                        targetValue = percentage,
                        animationSpec = tween(durationMillis = 1000)
                    )
                }

                // Determine dynamic gradient colors
                val gradientColors = when {
                    safeToSpend < 0.0 -> listOf(Color(0xFFFF1744), Color(0xFFFF5252))
                    safeToSpend < dailyLimit * 0.5 -> listOf(Color(0xFFFF9100), Color(0xFFFFD600))
                    else -> listOf(Color(0xFF00C9FF), Color(0xFF00E676))
                }

                val safeToSpendText = currencyFormatter.format(safeToSpend)
                val fontSize = when {
                    safeToSpendText.length >= 14 -> 18.sp
                    safeToSpendText.length >= 11 -> 22.sp
                    safeToSpendText.length >= 8 -> 26.sp
                    else -> 32.sp
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(vertical = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokePx = 14.dp.toPx()
                        val radius = (size.minDimension - strokePx) / 2f
                        val startAngle = 150f
                        val sweepAngleTrack = 240f
                        val sweepAngleActive = sweepAngleTrack * animatedPercentage.value

                        // Background track
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngleTrack,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        if (safeToSpend > 0.0) {
                            val brush = Brush.sweepGradient(
                                colors = gradientColors,
                                center = center
                            )
                            // Ambient Glow effect
                            drawArc(
                                brush = Brush.linearGradient(gradientColors),
                                startAngle = startAngle,
                                sweepAngle = sweepAngleActive,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokePx + 4.dp.toPx(), cap = StrokeCap.Round),
                                alpha = 0.25f
                            )
                            // Main Foreground arc
                            drawArc(
                                brush = Brush.linearGradient(gradientColors),
                                startAngle = startAngle,
                                sweepAngle = sweepAngleActive,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        } else {
                            // Draw a warning arc if overspent (a small pulsing red segment or complete red outline)
                            drawArc(
                                color = Color(0xFFFF1744),
                                startAngle = startAngle,
                                sweepAngle = sweepAngleTrack,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Dial Center Content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = safeToSpendText,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = if (safeToSpend < 0.0) Color(0xFFFF1744) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (safeToSpend < 0.0) "Overspent Today" else "Remaining Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Dynamic helper warning if overspent
                if (safeToSpend < 0.0) {
                    val absOverspent = kotlin.math.abs(safeToSpend)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Overspent by ${currencyFormatter.format(absOverspent)} today",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF1744),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Allowance Details Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Daily Limit",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormatter.format(dailyLimit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Spent Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormatter.format(spentToday),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (spentToday > dailyLimit) Color(0xFFFF1744) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
