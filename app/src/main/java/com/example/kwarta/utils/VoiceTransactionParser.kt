package com.example.kwarta.utils

import com.example.kwarta.data.local.CategoryEntity
import java.util.Locale

/**
 * Parses a spoken / transcribed text string into structured transaction fields.
 * Fully offline — no network or ML model required.
 *
 * Example inputs:
 *  - "Groceries 1500 pesos"        → title=Groceries, amount=1500, type=EXPENSE
 *  - "Salary received 25000"       → title=Salary, amount=25000, type=INCOME
 *  - "Grab ride 200"               → title=Grab ride, amount=200, type=EXPENSE
 *  - "Logged food for 350 pesos"   → title=Food, amount=350, type=EXPENSE
 */
data class VoiceParsedTransaction(
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val matchedCategoryId: Long?, // Best-guess category ID from existing categories
    val matchedCategoryName: String? // For display
)

object VoiceTransactionParser {

    // Filler words to strip from the title extraction
    private val fillerWords = setOf(
        "logged", "log", "add", "added", "spent", "paid", "bought", "got",
        "for", "of", "at", "in", "on", "the", "a", "an", "my", "some",
        "around", "about", "roughly", "approximately", "worth", "today",
        "just", "pesos", "peso", "php", "money", "total"
    )

    // Income indicator keywords
    private val incomeKeywords = setOf(
        "salary", "income", "received", "earn", "earned", "earnings",
        "freelance", "bonus", "allowance", "refund", "cashback",
        "cash back", "commission", "dividend", "profit", "revenue",
        "payment received", "got paid", "sweldo", "sahod", "kita"
    )

    // Amount patterns: matches "1500", "1,500", "1500.50", "₱1500", "P1500", "1500 pesos"
    private val amountRegex = Regex(
        """(?:₱|(?:^|\s)[Pp](?=\d))?\s*(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)"""
    )

    fun parse(
        rawText: String,
        existingCategories: List<CategoryEntity> = emptyList()
    ): VoiceParsedTransaction? {
        if (rawText.isBlank()) return null

        val text = rawText.trim()

        // 1. Extract amount
        val amount = extractAmount(text) ?: return null // Can't create a transaction without an amount

        // 2. Detect income vs expense
        val type = detectType(text)

        // 3. Extract title (everything that's not the amount or filler words)
        val title = extractTitle(text, amount)
        if (title.isBlank()) return null

        // 4. Fuzzy-match to an existing category
        val (categoryId, categoryName) = matchCategory(title, type, existingCategories)

        return VoiceParsedTransaction(
            title = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            amount = amount,
            type = type,
            matchedCategoryId = categoryId,
            matchedCategoryName = categoryName
        )
    }

    private fun extractAmount(text: String): Double? {
        // Find all number-like patterns and pick the best one
        val matches = amountRegex.findAll(text).toList()
        if (matches.isEmpty()) return null

        // Prefer the first match that looks like a valid amount
        for (match in matches) {
            val numStr = match.groupValues[1].replace(",", "")
            val value = numStr.toDoubleOrNull()
            if (value != null && value > 0.0) {
                return value
            }
        }
        return null
    }

    private fun detectType(text: String): String {
        val lower = text.lowercase(Locale.ROOT)
        return if (incomeKeywords.any { lower.contains(it) }) "INCOME" else "EXPENSE"
    }

    private fun extractTitle(text: String, amount: Double): String {
        var working = text

        // Remove any amount-like substrings from the text
        val amountStr = if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            amount.toString()
        }
        // Remove formatted versions too (e.g., "1,500")
        val formattedAmount = String.format(Locale.US, "%,.0f", amount)

        working = working
            .replace(amountStr, " ")
            .replace(formattedAmount, " ")
            .replace(Regex("""₱\s*"""), " ")
            .replace(Regex("""\b[Pp]\s*(?=\d)"""), " ")
            // Remove any remaining standalone numbers
            .replace(Regex("""\b\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?\b"""), " ")

        // Split into words and filter out filler words
        val words = working
            .split(Regex("\\s+"))
            .map { it.trim().replace(Regex("""^[^a-zA-Z]+|[^a-zA-Z]+$"""), "") }
            .filter { word ->
                word.isNotBlank() &&
                word.length >= 2 &&
                word.lowercase(Locale.ROOT) !in fillerWords
            }

        return words.joinToString(" ").trim()
    }

    private fun matchCategory(
        title: String,
        type: String,
        categories: List<CategoryEntity>
    ): Pair<Long?, String?> {
        if (categories.isEmpty()) return Pair(null, null)

        val titleLower = title.lowercase(Locale.ROOT)
        val titleWords = titleLower.split(Regex("\\s+"))

        // Filter categories by type compatibility
        val compatibleCategories = categories.filter {
            it.transactionType == type || it.transactionType == "BOTH"
        }

        // Score each category based on word overlap
        var bestScore = 0
        var bestCategory: CategoryEntity? = null

        for (category in compatibleCategories) {
            val catNameLower = category.name.lowercase(Locale.ROOT)
            val catWords = catNameLower.split(Regex("[\\s&]+"))

            var score = 0

            // Full name containment in title (highest weight)
            if (titleLower.contains(catNameLower)) {
                score += 10
            }

            // Word-level matching
            for (catWord in catWords) {
                if (catWord.length < 3) continue
                for (titleWord in titleWords) {
                    if (titleWord.length < 3) continue
                    // Exact word match
                    if (titleWord == catWord) {
                        score += 5
                    }
                    // Prefix match (e.g., "grocer" matches "groceries")
                    else if (titleWord.startsWith(catWord.take(4)) || catWord.startsWith(titleWord.take(4))) {
                        score += 3
                    }
                }
            }

            if (score > bestScore) {
                bestScore = score
                bestCategory = category
            }
        }

        // Only return a match if confidence is reasonable
        return if (bestScore >= 3 && bestCategory != null) {
            Pair(bestCategory.id, bestCategory.name)
        } else {
            // Return the first compatible category as a fallback
            val fallback = compatibleCategories.firstOrNull()
            Pair(fallback?.id, fallback?.name)
        }
    }
}
