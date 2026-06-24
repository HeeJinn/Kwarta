package com.example.kwarta.ui.screens.transactions

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kwarta.data.local.CategoryEntity
import com.example.kwarta.data.local.TransactionEntity
import com.example.kwarta.data.repository.FinanceRepository
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineSyncScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: FinanceRepository = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Receive / Scan", "Share / Show QR")

    var qrBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoadingQr by remember { mutableStateOf(false) }
    var transactionCountToShare by remember { mutableIntStateOf(0) }
    
    var isSyncingData by remember { mutableStateOf(false) }

    // Re-generate QR Code whenever Tab transitions to Share tab (index 1)
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            isLoadingQr = true
            scope.launch(Dispatchers.IO) {
                try {
                    val transactions = repository.getAllTransactions().firstOrNull() ?: emptyList()
                    val categories = repository.getAllActiveCategories().firstOrNull() ?: emptyList()
                    
                    // Take the last 30 transactions to prevent QR code size blowup
                    val recentTx = transactions.take(30)
                    transactionCountToShare = recentTx.size
                    
                    val jsonArray = JSONArray()
                    for (item in recentTx) {
                        val obj = JSONObject()
                        obj.put("t", item.title)
                        obj.put("a", item.amount)
                        obj.put("ty", item.type)
                        
                        val cat = categories.find { it.id == item.categoryId }
                        obj.put("c", cat?.name ?: "Uncategorized")
                        obj.put("h", cat?.colorHex ?: "#9C27B0")
                        obj.put("d", item.date)
                        jsonArray.put(obj)
                    }
                    
                    val payload = jsonArray.toString()
                    val bmp = generateQrCodeBitmap(payload, 512)
                    if (bmp != null) {
                        qrBitmap = bmp.asImageBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingQr = false
                }
            }
        }
    }

    var showSplitConfirmDialog by remember { mutableStateOf(false) }
    var splitTitle by remember { mutableStateOf("") }
    var splitAmount by remember { mutableStateOf(0.0) }
    var splitDate by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Offline Sync") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, text ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "TabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        // Receive / Scan Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(72.dp)
                                    )
                                    Text(
                                        text = "Sync with Partner",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Point your camera at your partner's QR code on the other device to scan and merge their recent transactions directly into your database. Duplicate transactions are filtered out automatically.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    isSyncingData = true
                                    val options = GmsBarcodeScannerOptions.Builder()
                                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                        .build()
                                    val scanner = GmsBarcodeScanning.getClient(context, options)
                                    
                                    scanner.startScan()
                                        .addOnSuccessListener { barcode ->
                                            val rawValue = barcode.rawValue ?: ""
                                            
                                            // Detect bill split payload
                                            try {
                                                val json = JSONObject(rawValue)
                                                if (json.optString("s") == "split") {
                                                    // It's a bill split QR code
                                                    splitTitle = json.optString("n", "Shared Expense")
                                                    splitAmount = json.optDouble("a", 0.0)
                                                    splitDate = json.optLong("d", System.currentTimeMillis())
                                                    isSyncingData = false
                                                    showSplitConfirmDialog = true
                                                    return@addOnSuccessListener
                                                }
                                            } catch (_: Exception) {
                                                // Not a JSON object — fall through to normal sync
                                            }
                                            
                                            // Normal bulk sync flow
                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    val jsonArray = JSONArray(rawValue)
                                                    var newTxCount = 0
                                                    
                                                    val existingTx = repository.getAllTransactions().firstOrNull() ?: emptyList()
                                                    val existingCategories = repository.getAllActiveCategories().firstOrNull() ?: emptyList()
                                                    
                                                    for (i in 0 until jsonArray.length()) {
                                                        val obj = jsonArray.getJSONObject(i)
                                                        val t = obj.getString("t")
                                                        val a = obj.getDouble("a")
                                                        val ty = obj.getString("ty")
                                                        val cName = obj.getString("c")
                                                        val cHex = obj.optString("h", "#9C27B0")
                                                        val d = obj.getLong("d")
                                                        
                                                        // Deduplicate: Compare title, amount, type, and date (+/- 10s leeway)
                                                        val exists = existingTx.any {
                                                            it.title.equals(t, ignoreCase = true) &&
                                                            it.amount == a &&
                                                            it.type == ty &&
                                                            kotlin.math.abs(it.date - d) < 10000
                                                        }
                                                        
                                                        if (!exists) {
                                                            var categoryId = existingCategories.find { it.name.equals(cName, ignoreCase = true) }?.id
                                                            if (categoryId == null) {
                                                                val newCat = CategoryEntity(
                                                                    name = cName,
                                                                    iconName = "Star",
                                                                    colorHex = cHex,
                                                                    transactionType = ty,
                                                                    priorityTag = "WANT",
                                                                    isCustom = true
                                                                )
                                                                categoryId = repository.insertCategory(newCat)
                                                            }
                                                            
                                                            val tx = TransactionEntity(
                                                                title = t,
                                                                amount = a,
                                                                type = ty,
                                                                categoryId = categoryId,
                                                                date = d,
                                                                note = "Synced Offline",
                                                                merchantName = null,
                                                                imagePath = null,
                                                                status = "CLEARED"
                                                            )
                                                            repository.insertTransaction(tx)
                                                            newTxCount++
                                                        }
                                                    }
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        isSyncingData = false
                                                        if (newTxCount > 0) {
                                                            Toast.makeText(context, "Successfully synced $newTxCount new transactions!", Toast.LENGTH_LONG).show()
                                                            onBack()
                                                        } else {
                                                            Toast.makeText(context, "Already up to date. No new transactions found.", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    withContext(Dispatchers.Main) {
                                                        isSyncingData = false
                                                        Toast.makeText(context, "Invalid QR content: Failed to parse transaction data", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isSyncingData = false
                                            Toast.makeText(context, "Scanning cancelled or failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                enabled = !isSyncingData,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Partner QR Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    1 -> {
                        // Share / Show QR Tab
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Your QR Code",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Let your partner scan this screen with their Kwarta app to instantly share your last $transactionCountToShare transactions.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .size(260.dp)
                                    .background(Color.White, shape = RoundedCornerShape(24.dp))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingQr) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                } else if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap!!,
                                        contentDescription = "My Transactions QR Code",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(72.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bill Split Confirmation Dialog
    if (showSplitConfirmDialog) {
        val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("en-PH"))
        AlertDialog(
            onDismissRequest = { showSplitConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CallSplit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Shared Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Someone shared a bill split with you:")
                    Text(
                        text = splitTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your share: ${currencyFormatter.format(splitAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This will be logged as an expense transaction.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val categories = repository.getAllActiveCategories().firstOrNull() ?: emptyList()
                            val catId = categories.firstOrNull {
                                it.transactionType == "EXPENSE" || it.transactionType == "BOTH"
                            }?.id

                            if (catId != null) {
                                val tx = TransactionEntity(
                                    title = splitTitle,
                                    amount = splitAmount,
                                    type = "EXPENSE",
                                    categoryId = catId,
                                    date = splitDate,
                                    note = "Bill Split — Shared Expense",
                                    merchantName = null,
                                    imagePath = null,
                                    status = "CLEARED"
                                )
                                repository.insertTransaction(tx)
                                withContext(Dispatchers.Main) {
                                    showSplitConfirmDialog = false
                                    Toast.makeText(context, "Shared expense saved: ${currencyFormatter.format(splitAmount)}", Toast.LENGTH_LONG).show()
                                    onBack()
                                }
                            }
                        }
                    }
                ) {
                    Text("Save Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSplitConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ZXing QR Code Bitmap Generator
private fun generateQrCodeBitmap(content: String, sizePx: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
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
