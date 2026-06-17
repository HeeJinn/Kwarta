package com.example.kwarta

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.kwarta.ui.navigation.Destination
import com.example.kwarta.ui.navigation.KwartaNavigation
import com.example.kwarta.ui.theme.KwartaTheme

class MainActivity : ComponentActivity() {

    private var initialDestination by mutableStateOf<Destination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            KwartaTheme {
                KwartaNavigation(
                    initialDestination = initialDestination,
                    onDestinationConsumed = { initialDestination = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val dest = when (intent?.action) {
            "com.example.kwarta.ACTION_ADD_EXPENSE",
            "android.intent.action.ASSIST" -> Destination.AddTransaction("EXPENSE")
            "com.example.kwarta.ACTION_ADD_INCOME" -> Destination.AddTransaction("INCOME")
            else -> null
        }
        if (dest != null) {
            initialDestination = dest
        }
    }
}
