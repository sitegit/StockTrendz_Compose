package com.example.stocktrendz.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.stocktrendz.ui.theme.StockTrendzTheme
import java.util.Properties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockTrendzTheme {
                StockTrendz(apiKey = loadApiKey())
            }
        }
    }

    private fun loadApiKey(): String {
        val properties = Properties()
        val assetManager = this.assets
        val inputStream = assetManager.open("config.properties")
        properties.load(inputStream)
        return properties.getProperty("api_key", "")
    }
}
