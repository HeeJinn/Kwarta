package com.example.kwarta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kwarta.ui.navigation.KwartaNavigation
import com.example.kwarta.ui.theme.KwartaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KwartaTheme {
                    KwartaNavigation()
            }
        }
    }
}
