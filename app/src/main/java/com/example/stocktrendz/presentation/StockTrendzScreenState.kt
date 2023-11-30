package com.example.stocktrendz.presentation

import com.example.stocktrendz.data.model.Bar

sealed class StockTrendzScreenState {

    object Initial : StockTrendzScreenState()

    data class Content(val barList: List<Bar>) : StockTrendzScreenState()
}