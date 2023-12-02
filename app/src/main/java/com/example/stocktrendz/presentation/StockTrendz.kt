package com.example.stocktrendz.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stocktrendz.data.model.Bar
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun StockTrendz(
    modifier: Modifier = Modifier,
    barsList: List<Bar>
) {

    var stockTrendzState by rememberStockTrendzState(barsList = barsList)

    Chart(
        modifier = modifier,
        stockTrendzState = stockTrendzState,
        onStockTrendzStateChanged = {
            stockTrendzState = it
        }
    )

    barsList.firstOrNull()?.let {
        Prices(
            modifier = modifier,
            max = stockTrendzState.max,
            min = stockTrendzState.min,
            pxPerPoint = stockTrendzState.pxPerPoint,
            lastPrice = it.close
        )
    }
}

@Composable
private fun Chart(
    modifier: Modifier = Modifier,
    stockTrendzState: StockTrendzState,
    onStockTrendzStateChanged: (StockTrendzState) -> Unit
) {
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarsCount = (stockTrendzState.visibleBarsCount / zoomChange).roundToInt()
            .coerceIn(MIN_VISIBLE_BARS_COUNT, stockTrendzState.barsList.size)

        val scrolledBy = (stockTrendzState.scrolledBy + panChange.x)
            .coerceAtLeast(0f)
            .coerceAtMost(stockTrendzState.barsList.size * stockTrendzState.barWidth - stockTrendzState.screenWidth)

        onStockTrendzStateChanged(
            stockTrendzState.copy(
                visibleBarsCount = visibleBarsCount,
                scrolledBy = scrolledBy
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
            .padding(
                top = 32.dp,
                bottom = 32.dp,
                end = 16.dp
            )
            .transformable(transformableState)
            .onSizeChanged {
                onStockTrendzStateChanged(
                    stockTrendzState.copy(
                        screenWidth = it.width.toFloat(),
                        screenHeight = it.height.toFloat()
                    )
                )
            }
    ) {
        val min = stockTrendzState.min
        val pxPerPoint = stockTrendzState.pxPerPoint

        translate(left = stockTrendzState.scrolledBy) {
            stockTrendzState.barsList.forEachIndexed { index, bar ->
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

@OptIn(ExperimentalTextApi::class)
@Composable
private fun Prices(
    modifier: Modifier = Modifier,
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(vertical = 32.dp)
    ) {
        drawPrices(
            max = max,
            min = min,
            pxPerPoint = pxPerPoint,
            lastPrice = lastPrice,
            textMeasurer = textMeasurer
        )
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasurer: TextMeasurer
) {
    //max
    val maxPriceOffsetY = 0f
    drawDashedLine(
        start = Offset(0f, maxPriceOffsetY),
        end = Offset(size.width, maxPriceOffsetY)
    )
    drawTextPrice(
        textMeasurer = textMeasurer,
        price = max,
        offsetY = maxPriceOffsetY
    )
    //lastPrice
    val lastPriceOffsetY = size.height - ((lastPrice - min) * pxPerPoint)
    drawDashedLine(
        start = Offset(0f, lastPriceOffsetY),
        end = Offset(size.width, lastPriceOffsetY)
    )
    drawTextPrice(
        textMeasurer = textMeasurer,
        price = lastPrice,
        offsetY = lastPriceOffsetY
    )
    //min
    val minPriceOffsetY = size.height
    drawDashedLine(
        start = Offset(0f, minPriceOffsetY),
        end = Offset(size.width, minPriceOffsetY)
    )
    drawTextPrice(
        textMeasurer = textMeasurer,
        price = min,
        offsetY = minPriceOffsetY
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTextPrice(
    textMeasurer: TextMeasurer,
    price: Float,
    offsetY: Float,
) {
    val textLayoutResult = textMeasurer.measure(
        text = price.toString(),
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(size.width - (textLayoutResult.size.width + 5.dp.toPx()), offsetY)
    )
}

private fun DrawScope.drawDashedLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )
}