package com.one.memorymatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.one.memorymatch.ui.navigation.AppNavHost
import com.one.memorymatch.ui.theme.KidsMemoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KidsMemoryTheme {
                AppNavHost()
            }
        }
    }
}
