package com.example.kwarta.ui.screens.transactions

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import com.example.kwarta.data.local.TransactionEntity
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSplitterScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: FinanceRepository = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))

    var billTitle by remember { mutableStateOf("") }
    var billAmountText by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableIntStateOf(2) }

    var qrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showQrResult by remember { mutableStateOf(false) }

    val billAmount = billAmountText.toDoubleOrNull() ?: 0.0
    val perPersonShare = if (numberOfPeople > 0 && billAmount > 0) {
        Math.round(billAmount / numberOfPeople * 100.0) / 100.0
    } else 0.0

    val isValid = billAmount > 0 && numberOfPeople >= 2

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Split a Bill", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = showQrResult,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "BillSplitTransition"
        ) { isQrView ->
            if (!isQrView) {
                // ─── Input Form ───
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Column {
                                Text(
                                    text = "Offline Bill Splitter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enter the bill total and split it equally. Generate a QR code for friends to scan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Bill title
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Description (Optional)") },
                        placeholder = { Text("e.g. Dinner at Jollibee") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                        }
                    )

                    // Bill amount
                    OutlinedTextField(
                        value = billAmountText,
                        onValueChange = { billAmountText = it },
                        label = { Text("Total Bill Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        prefix = { Text("₱") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Payments, contentDescription = null)
                        }
                    )

                    // Number of people stepper
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Number of People",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Including yourself",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledIconButton(
                                    onClick = { if (numberOfPeople > 2) numberOfPeople-- },
                                    enabled = numberOfPeople > 2,
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(20.dp))
                                }

                                Text(
                                    text = "$numberOfPeople",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(40.dp),
                                    textAlign = TextAlign.Center
                                )

                                FilledIconButton(
                                    onClick = { if (numberOfPeople < 20) numberOfPeople++ },
                                    enabled = numberOfPeople < 20,
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    // Per-person share display
                    AnimatedVisibility(
                        visible = billAmount > 0,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Each Person Pays",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currencyFormatter.format(perPersonShare),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${currencyFormatter.format(billAmount)} ÷ $numberOfPeople people",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Generate QR button
                    Button(
                        onClick = {
                            isGenerating = true
                            scope.launch(Dispatchers.IO) {
                                val title = billTitle.ifBlank { "Shared Expense" }
                                val payload = JSONObject().apply {
                                    put("s", "split") // Split flag for scanner detection
                                    put("n", title)
                                    put("a", perPersonShare)
                                    put("ty", "EXPENSE")
                                    put("d", System.currentTimeMillis())
                                }.toString()

                                val bmp = generateSplitQrBitmap(payload, 512)
                                withContext(Dispatchers.Main) {
                                    if (bmp != null) {
                                        qrBitmap = bmp.asImageBitmap()
                                        showQrResult = true
                                    }
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = isValid && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Share QR Code",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                // ─── QR Result View ───
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary
                    Text(
                        text = billTitle.ifBlank { "Shared Expense" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Each person: ${currencyFormatter.format(perPersonShare)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Ask your friends to scan this QR code\nwith their Kwarta app to log their share.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // QR Code display
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color.White, shape = RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!,
                                contentDescription = "Bill Share QR Code",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Info chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("${currencyFormatter.format(billAmount)} total") },
                            icon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("$numberOfPeople people") },
                            icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Back to edit button
                    OutlinedButton(
                        onClick = { showQrResult = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Split", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val categories = repository.getAllActiveCategories().firstOrNull() ?: emptyList()
                                    val catId = categories.firstOrNull {
                                        it.transactionType == "EXPENSE" || it.transactionType == "BOTH"
                                    }?.id

                                    if (catId != null) {
                                        val tx = TransactionEntity(
                                            title = billTitle.ifBlank { "Shared Expense" },
                                            amount = perPersonShare,
                                            type = "EXPENSE",
                                            categoryId = catId,
                                            date = System.currentTimeMillis(),
                                            note = "Bill Split — My Share",
                                            merchantName = null,
                                            imagePath = null,
                                            status = "CLEARED"
                                        )
                                        repository.insertTransaction(tx)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "My share logged: ${currencyFormatter.format(perPersonShare)}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onBack()
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            onBack()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        onBack()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ZXing QR Code Bitmap Generator for split payloads
private fun generateSplitQrBitmap(content: String, sizePx: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y))
                    android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
