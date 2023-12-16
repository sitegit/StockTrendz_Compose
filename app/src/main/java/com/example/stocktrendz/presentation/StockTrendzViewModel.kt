package com.example.stocktrendz.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocktrendz.data.network.ApiFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StockTrendzViewModel(private val apiKey: String) : ViewModel() {

    private val apiServise = ApiFactory.apiService

    private val _state = MutableStateFlow<StockTrendzScreenState>(StockTrendzScreenState.Initial)
    val state = _state.asStateFlow()

    private var lastState: StockTrendzScreenState = StockTrendzScreenState.Initial

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->

        when(throwable) {
            is HttpException -> {
                _state.value = StockTrendzScreenState.Error("HttpException")
                viewModelScope.launch {
                    delay(100)
                    _state.value = lastState
                }
            }
            else -> {
                _state.value = StockTrendzScreenState.Error()
            }
        }
    }

    init {
        loadBarsList()
    }

    fun loadBarsList(timeFrame: TimeFrame = TimeFrame.HOUR_1) {
        lastState = _state.value
        _state.value = StockTrendzScreenState.Loading
        viewModelScope.launch(exceptionHandler) {
            val barList = apiServise.loadBars(apiKey = apiKey, timeframe = timeFrame.value).barList
            _state.value = StockTrendzScreenState.Content(barList = barList, timeFrame = timeFrame)
        }
    }
}