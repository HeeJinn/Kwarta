package com.example.kwarta.ui.screens.transactions

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kwarta.data.local.TransactionEntity
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddTransactionScreen(
    transactionType: String,
    onBack: () -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val filteredCategories = remember(categories, transactionType) {
        categories.filter { it.transactionType == transactionType || it.transactionType == "BOTH" }
    }

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    // Image attachment states
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
 
    var showImageOptionDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
 
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
 
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            imageUri = tempCameraUri
        }
    }

    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                imageBitmap = bitmap.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } ?: run {
            imageBitmap = null
        }
    }

    // Pre-select first category if available and none selected
    LaunchedEffect(filteredCategories) {
        if (selectedCategoryId == null && filteredCategories.isNotEmpty()) {
            selectedCategoryId = filteredCategories.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add ${transactionType.lowercase().replaceFirstChar { it.uppercase() }}") },
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Category", style = MaterialTheme.typography.titleMedium)
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredCategories.forEachIndexed { index, category ->
                    ToggleButton(
                        checked = selectedCategoryId == category.id,
                        onCheckedChange = { if (it) selectedCategoryId = category.id },
                        shapes = when {
                            filteredCategories.size == 1 -> ToggleButtonDefaults.shapes()
                            index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            index == filteredCategories.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        modifier = Modifier.semantics { role = Role.RadioButton }
                    ) {
                        Text(category.name)
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            val borderColor = MaterialTheme.colorScheme.outlineVariant
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .dashedBorder(
                        strokeWidth = 2.dp,
                        color = borderColor,
                        cornerRadius = 16.dp
                    )
                    .clickable { showImageOptionDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = "Selected Receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear image")
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach Receipt",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Attach Receipt (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    val cat = filteredCategories.find { it.id == selectedCategoryId }
                    if (parsedAmount != null && cat != null && title.isNotBlank()) {
                        val persistentUri = imageUri?.let { copyUriToInternalStorage(context, it) }
                        val transaction = TransactionEntity(
                            title = title,
                            amount = parsedAmount,
                            type = cat.transactionType, // Automatically infer INCOME/EXPENSE from category
                            categoryId = cat.id,
                            date = System.currentTimeMillis(),
                            note = note.takeIf { it.isNotBlank() },
                            merchantName = null,
                            imagePath = persistentUri?.toString() ?: imageUri?.toString(),
                            isRecurring = false,
                            status = "CLEARED"
                        )
                        viewModel.addTransaction(transaction)
                        onBack() // Navigate back on save
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && amount.isNotBlank() && selectedCategoryId != null && amount.toDoubleOrNull() != null
            ) {
                Text("Save Transaction")
            }
 
            if (showImageOptionDialog) {
                AlertDialog(
                    onDismissRequest = { showImageOptionDialog = false },
                    title = { Text("Attach Receipt") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImageOptionDialog = false
                                        try {
                                            val uri = getTempImageUri(context)
                                            tempCameraUri = uri
                                            cameraLauncher.launch(uri)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Take Photo")
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Take Photo", style = MaterialTheme.typography.bodyLarge)
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showImageOptionDialog = false
                                        imagePickerLauncher.launch("image/*")
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Choose from Gallery")
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Choose from Gallery", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showImageOptionDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val dir = java.io.File(context.filesDir, "receipts")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = java.io.File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadius: Dp
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}
 
fun getTempImageUri(context: android.content.Context): Uri {
    val tempFile = java.io.File.createTempFile("receipt_capture_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
