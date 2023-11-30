package com.example.stocktrendz.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stocktrendz.ui.theme.StockTrendzTheme
import java.util.Properties

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockTrendzTheme {
                val viewModel: StockTrendzViewModel = viewModel(
                    factory = StockTrendzViewModelFactory(loadApiKey())
                )
                val screenState = viewModel.state.collectAsState()

                when(val currentState = screenState.value) {
                    is StockTrendzScreenState.Initial -> {}
                    is StockTrendzScreenState.Content -> {
                        Log.d("MyTag", currentState.barList.toString())
                    }
                }
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
