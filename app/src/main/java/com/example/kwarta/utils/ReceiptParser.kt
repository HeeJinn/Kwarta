package com.example.kwarta.utils

import java.util.Locale

data class ReceiptDetails(
    val merchantName: String?,
    val totalAmount: Double?
)

object ReceiptParser {

    private val priceRegex = Regex("""(?:\d{1,3}(?:,\d{3})+|\d+)\.\d{2}""")
    
    private val tinRegex = Regex("""\d{3}[-\s]\d{3}[-\s]\d{3}""")
    private val phoneRegex = Regex("""(?:\+63|09)\d{9}""")
    private val dateRegex = Regex("""\d{2}[/-]\d{2}[/-]\d{2,4}""")

    private val addressKeywords = listOf(
        "st.", "street", "ave", "avenue", "rd", "road", "city", "brgy", "barangay", "bldg", 
        "building", "highway", "hwy", "zone", "prov", "province", "philippines", "ph"
    )
    
    private val metadataKeywords = listOf(
        "receipt", "invoice", "trans", "order", "welcome", "duplicate", "official", "copy",
        "tax", "tin", "vat", "tel", "phone", "fax", "pos", "terminal", "or#", "cashier", "date"
    )

    fun parseReceiptText(rawText: String): ReceiptDetails {
        if (rawText.isBlank()) {
            return ReceiptDetails(null, null)
        }

        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val merchantName = extractMerchantName(lines)
        val totalAmount = extractTotalAmount(lines)

        return ReceiptDetails(merchantName, totalAmount)
    }

    private fun extractMerchantName(lines: List<String>): String? {
        // Look at the first 6 lines of the receipt
        val candidateLines = lines.take(6)

        for (line in candidateLines) {
            val lowerLine = line.lowercase(Locale.ROOT)

            // Skip lines that match typical skip patterns (TIN, Phone, Date)
            if (tinRegex.containsMatchIn(line) || 
                phoneRegex.containsMatchIn(line) || 
                dateRegex.containsMatchIn(line)) {
                continue
            }

            // Skip lines containing address keywords
            if (addressKeywords.any { lowerLine.contains(it) }) {
                continue
            }

            // Skip lines containing metadata/receipt descriptors
            if (metadataKeywords.any { lowerLine.contains(it) }) {
                continue
            }

            // Skip lines that are purely numeric or symbols
            if (line.all { !it.isLetter() }) {
                continue
            }

            // Clean up merchant name: Remove trailing/leading symbols, commas, etc.
            val cleaned = line.replace(Regex("""^[^\w]+|[^\w]+$"""), "").trim()
            if (cleaned.length in 3..35) {
                return cleaned
            }
        }

        return null
    }

    private fun extractTotalAmount(lines: List<String>): Double? {
        val totalKeywords = listOf("total", "amount due", "payable", "grand total", "net total", "net due")
        val fallbackKeywords = listOf("cash", "amount", "due", "php", "₱")

        var bestMatch: Double? = null

        // 1. Direct keyword scanning
        for (line in lines) {
            val lowerLine = line.lowercase(Locale.ROOT)
            if (totalKeywords.any { lowerLine.contains(it) }) {
                val match = priceRegex.findAll(line).lastOrNull()?.value
                if (match != null) {
                    val amount = parsePrice(match)
                    if (amount != null && amount > 0.0) {
                        return amount
                    }
                }
            }
        }

        // 2. Secondary keyword scanning
        for (line in lines) {
            val lowerLine = line.lowercase(Locale.ROOT)
            if (fallbackKeywords.any { lowerLine.contains(it) }) {
                val match = priceRegex.findAll(line).lastOrNull()?.value
                if (match != null) {
                    val amount = parsePrice(match)
                    if (amount != null && amount > 0.0) {
                        return amount
                    }
                }
            }
        }

        // 3. Fallback: Find the largest valid price matching priceRegex (ignoring items that look like TIN or dates)
        val allPrices = mutableListOf<Double>()
        for (line in lines) {
            // Skip lines with TIN/Date/Phone pattern
            if (tinRegex.containsMatchIn(line) || dateRegex.containsMatchIn(line)) {
                continue
            }
            priceRegex.findAll(line).forEach { matchResult ->
                parsePrice(matchResult.value)?.let { amount ->
                    if (amount > 0.0) {
                        allPrices.add(amount)
                    }
                }
            }
        }

        if (allPrices.isNotEmpty()) {
            // Return the maximum price found (the bill total is usually the largest price)
            return allPrices.maxOrNull()
        }

        return null
    }

    private fun parsePrice(priceStr: String): Double? {
        return try {
            // Remove commas if any
            val cleanStr = priceStr.replace(",", "")
            cleanStr.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
