package com.example.stocktrendz.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import com.example.stocktrendz.data.model.Bar
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun StockTrendz(barsList: List<Bar>) {

    var stockTrendzState by rememberStockTrendzState(barsList = barsList)

    val transformableState = TransformableState { zoomChange, panChange, _ ->
        val visibleBarsCount = (stockTrendzState.visibleBarsCount / zoomChange).roundToInt()
            .coerceIn(MIN_VISIBLE_BARS_COUNT, barsList.size)

        val scrolledBy = (stockTrendzState.scrolledBy + panChange.x)
            .coerceAtLeast(0f)
            .coerceAtMost(barsList.size * stockTrendzState.barWidth - stockTrendzState.screenWidth)

        stockTrendzState = stockTrendzState.copy(
            visibleBarsCount = visibleBarsCount,
            scrolledBy = scrolledBy
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(transformableState)
            .onSizeChanged {
                stockTrendzState.screenWidth = it.width.toFloat()
            }
    ) {
        val max = stockTrendzState.visibleBars.maxOf { it.high }
        val min = stockTrendzState.visibleBars.minOf { it.low }
        val pxPerPoint = size.height / (max - min)

        translate(left = stockTrendzState.scrolledBy) {
            barsList.forEachIndexed { index, bar ->
                val offsetX = size.width - index * stockTrendzState.barWidth
                drawLine(
                    color = Color.White,
                    start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open < bar.close) Color.Green else Color.Red,
                    start = Offset(offsetX, size.height - ((bar.open - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.close - min) * pxPerPoint)),
                    strokeWidth = stockTrendzState.barWidth / 2
                )
            }
        }
    }
}