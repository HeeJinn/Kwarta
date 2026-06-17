package com.example.kwarta.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.kwarta.MainActivity
import com.example.kwarta.R
import com.example.kwarta.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.NumberFormat
import java.util.Locale

class KwartaAppWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val repository: FinanceRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Run update in background scope
        CoroutineScope(Dispatchers.IO).launch {
            val allTx = repository.getAllTransactions().firstOrNull() ?: emptyList()
            val totalIncome = allTx.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = allTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val totalBalance = totalIncome - totalExpense

            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))
            val formattedBalance = currencyFormatter.format(totalBalance)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.kwarta_widget)
                
                // Set balance text
                views.setTextViewText(R.id.widget_balance_value, formattedBalance)

                // Add Income Intent
                val incomeIntent = Intent(context, MainActivity::class.java).apply {
                    action = "com.example.kwarta.ACTION_ADD_INCOME"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val incomePendingIntent = PendingIntent.getActivity(
                    context,
                    101,
                    incomeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_widget_add_income, incomePendingIntent)

                // Add Expense Intent
                val expenseIntent = Intent(context, MainActivity::class.java).apply {
                    action = "com.example.kwarta.ACTION_ADD_EXPENSE"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val expensePendingIntent = PendingIntent.getActivity(
                    context,
                    102,
                    expenseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_widget_add_expense, expensePendingIntent)

                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

object KwartaWidgetUpdater {
    fun update(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, KwartaAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(context, KwartaAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
