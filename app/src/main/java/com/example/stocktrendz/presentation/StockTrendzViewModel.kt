package com.example.stocktrendz.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocktrendz.data.network.ApiFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockTrendzViewModel(private val apiKey: String) : ViewModel() {

    private val apiServise = ApiFactory.apiService

    private val _state = MutableStateFlow<StockTrendzScreenState>(StockTrendzScreenState.Initial)
    val state = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("MyTag", "Exception caught: $throwable")
    }

    init {
        loadBarsList()
    }

    private fun loadBarsList() {
        viewModelScope.launch(exceptionHandler) {
            val barList = apiServise.loadBars(apiKey = apiKey).barList
            _state.value = StockTrendzScreenState.Content(barList = barList)
        }
    }
}