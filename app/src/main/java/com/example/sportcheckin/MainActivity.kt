package com.example.sportcheckin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sportcheckin.ui.MainScreen
import com.example.sportcheckin.ui.theme.SportCheckInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportCheckInTheme {
                MainScreen()
            }
        }
    }
}
