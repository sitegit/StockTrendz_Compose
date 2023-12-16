package com.example.stocktrendz.presentation

import com.example.stocktrendz.data.model.Bar

sealed class StockTrendzScreenState {

    object Initial : StockTrendzScreenState()

    object Loading : StockTrendzScreenState()

    data class Error(val message: String? = null) : StockTrendzScreenState()

    data class Content(val barList: List<Bar>, val timeFrame: TimeFrame) : StockTrendzScreenState()
}