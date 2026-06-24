@file:OptIn(ExperimentalSharedTransitionApi::class)
package com.example.kwarta.ui.screens.transactions

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.LaunchedEffect
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import com.example.kwarta.ui.navigation.LocalSharedTransitionScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    viewModel: TransactionDetailViewModel = koinViewModel()
) {
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    val transaction by viewModel.transaction.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

    val sharedTransitionScope = LocalSharedTransitionScope.current

    val detailBoundsModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                rememberSharedContentState(key = "transaction_$transactionId"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                }
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(detailBoundsModifier)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transaction Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (transaction == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val tx = transaction!!
                val isIncome = tx.type == "INCOME"
                val color = if (isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                val sign = if (isIncome) "+" else "-"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header (Amount & Date)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$sign${currencyFormatter.format(tx.amount)}",
                            style = MaterialTheme.typography.displayMedium,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormatter.format(Date(tx.date)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    HorizontalDivider()

                    // Category
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Category", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                        Text(category?.name ?: "Loading...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    // Title
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Title", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                        Text(tx.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }

                    // Merchant
                    if (!tx.merchantName.isNullOrBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Merchant", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                            Text(tx.merchantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Note
                    if (!tx.note.isNullOrBlank()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Note", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tx.note, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    // Receipt Image
                    if (!tx.imagePath.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Receipt", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(tx.imagePath))
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = "Receipt Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
