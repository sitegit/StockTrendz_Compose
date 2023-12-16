package com.example.stocktrendz.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StockTrendzViewModelFactory(private val apiKey: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockTrendzViewModel(apiKey) as T
    }
}