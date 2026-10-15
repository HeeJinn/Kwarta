package com.example.kwarta.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.kwarta.ui.screens.transactions.TransactionWithCategory
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pre-computed data bundle passed from the ViewModel to the export layer.
 */
data class ExportData(
    val transactions: List<TransactionWithCategory>,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val filterDescription: String
)

object ExportHelper {

    private val DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val EXPORT_DATE_FORMAT = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
    private val CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val LINE_HEIGHT = 14f
    private const val ROW_HEIGHT = 18f

    // ---------------------------------------------------------------------------
    // CSV
    // ---------------------------------------------------------------------------

    fun generateCsv(context: Context, data: ExportData): File {
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "kwarta_export_$timestamp.csv")

        FileOutputStream(file).use { fos ->
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)
        }

        file.appendText("Date,Title,Category,Type,Amount,Merchant,Note,Status\n")

        for (item in data.transactions) {
            val tx = item.transaction
            val date = DATE_FORMAT.format(Date(tx.date))
            val title = escapeCsv(tx.title)
            val category = item.category?.name ?: ""
            val type = tx.type
            val amount = if (tx.type == "EXPENSE") -tx.amount else tx.amount
            val merchant = escapeCsv(tx.merchantName ?: "")
            val note = escapeCsv(tx.note ?: "")
            val status = tx.status

            file.appendText("$date,$title,$category,$type,$amount,$merchant,$note,$status\n")
        }

        return file
    }

    // ---------------------------------------------------------------------------
    // PDF
    // ---------------------------------------------------------------------------

    fun generatePdf(context: Context, data: ExportData): File {
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(dir, "kwarta_export_$timestamp.pdf")

        val titlePaint = paint(18f, Typeface.BOLD)
        val subtitlePaint = paint(9f, Typeface.NORMAL, Color.GRAY)
        val sectionPaint = paint(12f, Typeface.BOLD)
        val headerPaint = paint(9f, Typeface.BOLD)
        val cellPaint = paint(9f, Typeface.NORMAL)
        val tableHeaderPaint = paint(8f, Typeface.BOLD, Color.WHITE)
        val tableHeaderBgPaint = Paint().apply { color = Color.rgb(0x21, 0x21, 0x21) }

        val contentWidth = PAGE_WIDTH - (MARGIN * 2)
        val pdf = PdfDocument()
        var pageNumber = 1
        var page = pdf.startPage(pageInfo(pageNumber))
        var canvas = page.canvas
        var y = MARGIN + LINE_HEIGHT

        fun finishCurrentPage() {
            pdf.finishPage(page)
        }

        fun startNewPage() {
            finishCurrentPage()
            pageNumber++
            page = pdf.startPage(pageInfo(pageNumber))
            canvas = page.canvas
            y = MARGIN + LINE_HEIGHT
        }

        fun ensureSpace(requiredHeight: Float) {
            if (y + requiredHeight > PAGE_HEIGHT - MARGIN) {
                startNewPage()
            }
        }

        fun drawCentered(text: String, paint: Paint, spacingAfter: Float = LINE_HEIGHT) {
            ensureSpace(LINE_HEIGHT)
            val x = (PAGE_WIDTH - paint.measureText(text)) / 2f
            canvas.drawText(text, x, y, paint)
            y += spacingAfter
        }

        fun drawSection(title: String) {
            ensureSpace(LINE_HEIGHT * 2)
            y += 8f
            canvas.drawText(title, MARGIN, y, sectionPaint)
            y += LINE_HEIGHT + 4f
        }

        fun drawSummaryRow(label: String, value: String) {
            ensureSpace(ROW_HEIGHT)
            canvas.drawText(label, MARGIN, y, headerPaint)
            val valueWidth = cellPaint.measureText(value)
            canvas.drawText(value, MARGIN + contentWidth - valueWidth, y, cellPaint)
            y += ROW_HEIGHT
        }

        fun drawTableHeader(columns: List<Pair<String, Float>>) {
            ensureSpace(ROW_HEIGHT + 4f)
            var x = MARGIN
            canvas.drawRect(MARGIN, y - ROW_HEIGHT + 2f, MARGIN + contentWidth, y + 4f, tableHeaderBgPaint)
            for ((label, width) in columns) {
                val colWidth = contentWidth * width
                canvas.drawText(label, x + 4f, y, tableHeaderPaint)
                x += colWidth
            }
            y += ROW_HEIGHT + 4f
        }

        fun drawTableRow(columns: List<Pair<String, Float>>, rightAlignFrom: Int = Int.MAX_VALUE) {
            ensureSpace(ROW_HEIGHT)
            var x = MARGIN
            columns.forEachIndexed { index, (text, width) ->
                val colWidth = contentWidth * width
                val displayText = truncateToWidth(text, cellPaint, colWidth - 8f)
                val textWidth = cellPaint.measureText(displayText)
                val textX = if (index >= rightAlignFrom) {
                    x + colWidth - textWidth - 4f
                } else {
                    x + 4f
                }
                canvas.drawText(displayText, textX, y, cellPaint)
                x += colWidth
            }
            y += ROW_HEIGHT
        }

        drawCentered("Kwarta Finance Report", titlePaint, spacingAfter = LINE_HEIGHT)
        drawCentered("Exported: ${EXPORT_DATE_FORMAT.format(Date())}", subtitlePaint, spacingAfter = LINE_HEIGHT)
        drawCentered(data.filterDescription, subtitlePaint, spacingAfter = 20f)

        drawSection("Summary")
        drawSummaryRow("Total Income", CURRENCY_FORMAT.format(data.totalIncome))
        drawSummaryRow("Total Expense", CURRENCY_FORMAT.format(data.totalExpense))
        drawSummaryRow("Net Balance", CURRENCY_FORMAT.format(data.netBalance))
        y += 8f

        val categoryTotals = data.transactions
            .filter { it.transaction.type == "EXPENSE" }
            .groupBy { it.category?.name ?: "Uncategorized" }
            .mapValues { (_, items) -> items.sumOf { it.transaction.amount } }
            .toList()
            .sortedByDescending { it.second }

        if (categoryTotals.isNotEmpty()) {
            val categoryColumns = listOf(
                "Category" to 0.45f,
                "Amount" to 0.35f,
                "%" to 0.20f
            )

            drawSection("Category Breakdown")
            drawTableHeader(categoryColumns)

            val totalExp = data.totalExpense
            for ((catName, catAmount) in categoryTotals) {
                val pct = if (totalExp > 0) (catAmount / totalExp * 100).toInt() else 0
                drawTableRow(
                    columns = listOf(
                        catName to 0.45f,
                        CURRENCY_FORMAT.format(catAmount) to 0.35f,
                        "$pct%" to 0.20f
                    ),
                    rightAlignFrom = 1
                )
            }
            y += 8f
        }

        val transactionColumns = listOf(
            "Date" to 0.14f,
            "Title" to 0.24f,
            "Category" to 0.16f,
            "Type" to 0.10f,
            "Amount" to 0.16f,
            "Merchant" to 0.20f
        )

        drawSection("Transactions")
        drawTableHeader(transactionColumns)

        for (item in data.transactions) {
            val tx = item.transaction
            val sign = if (tx.type == "EXPENSE") "-" else ""
            drawTableRow(
                columns = listOf(
                    DATE_FORMAT.format(Date(tx.date)) to 0.14f,
                    tx.title to 0.24f,
                    (item.category?.name ?: "") to 0.16f,
                    tx.type to 0.10f,
                    "$sign${CURRENCY_FORMAT.format(tx.amount)}" to 0.16f,
                    (tx.merchantName ?: "") to 0.20f
                ),
                rightAlignFrom = 4
            )
        }

        finishCurrentPage()
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        return file
    }

    // ---------------------------------------------------------------------------
    // Share via FileProvider
    // ---------------------------------------------------------------------------

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Kwarta Report"))
    }

    // ---------------------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------------------

    private fun pageInfo(pageNumber: Int): PdfDocument.PageInfo {
        return PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
    }

    private fun paint(textSize: Float, style: Int, color: Int = Color.BLACK): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, style)
            this.color = color
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun truncateToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.length > 1 && paint.measureText("$truncated…") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated…"
    }
}
