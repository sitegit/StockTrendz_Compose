package com.example.stocktrendz.presentation

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stocktrendz.R
import com.example.stocktrendz.data.model.Bar
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun StockTrendz(
    modifier: Modifier = Modifier,
    apiKey: String
) {

    val viewModel: StockTrendzViewModel = viewModel(
        factory = StockTrendzViewModelFactory(apiKey)
    )
    val screenState = viewModel.state.collectAsState()

    when(val currentState = screenState.value) {
        is StockTrendzScreenState.Initial -> {}
        is StockTrendzScreenState.Content -> {
            val stockTrendzState = rememberStockTrendzState(barsList = currentState.barList)

            Chart(
                modifier = modifier,
                stockTrendzState = stockTrendzState,
                onStockTrendzStateChanged = {
                    stockTrendzState.value = it
                },
                timeFrame = currentState.timeFrame
            )

            currentState.barList.firstOrNull()?.let {
                Prices(
                    modifier = modifier,
                    stockTrendzState = stockTrendzState,
                    lastPrice = it.close
                )
            }

            TimeFrames(selectedFrame = currentState.timeFrame) {
                viewModel.loadBarsList(it)
            }
        }
        is StockTrendzScreenState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        is StockTrendzScreenState.Error -> {
            val message = if (currentState.message == "HttpException")
                stringResource(R.string.to_many_request)
            else
                stringResource(R.string.loading_failed)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (currentState.message != "HttpException") {
                    Button(onClick = { viewModel.loadBarsList() }) {
                        Text(text = stringResource(R.string.replay))
                    }
                }
            }
            Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFrames(
    selectedFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeFrame.values().forEach { timeFrame ->
            val labelResId = when(timeFrame) {
                TimeFrame.MIN_5 -> R.string.time_frame_min_5
                TimeFrame.MIN_15 -> R.string.time_frame_min_15
                TimeFrame.MIN_30 -> R.string.time_frame_min_30
                TimeFrame.HOUR_1 -> R.string.time_frame_hour_1
            }
            val isSelected = timeFrame == selectedFrame
            AssistChip(
                onClick = { if (!isSelected) onTimeFrameSelected(timeFrame) },
                label = { Text(text = stringResource(id = labelResId)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) Color.White else Color.Black,
                    labelColor = if (isSelected) Color.Black else Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun Chart(
    modifier: Modifier = Modifier,
    stockTrendzState: State<StockTrendzState>,
    timeFrame: TimeFrame,
    onStockTrendzStateChanged: (StockTrendzState) -> Unit
) {
    val currentState = stockTrendzState.value

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarsCount = (currentState.visibleBarsCount / zoomChange).roundToInt()
            .coerceIn(MIN_VISIBLE_BARS_COUNT, currentState.barsList.size)

        val scrolledBy = (currentState.scrolledBy + panChange.x)
            .coerceAtLeast(0f)
            .coerceAtMost(currentState.barsList.size * currentState.barWidth - currentState.screenWidth)

        onStockTrendzStateChanged(
            currentState.copy(
                visibleBarsCount = visibleBarsCount,
                scrolledBy = scrolledBy
            )
        )
    }

    val textMeasurer = rememberTextMeasurer()

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
                    currentState.copy(
                        screenWidth = it.width.toFloat(),
                        screenHeight = it.height.toFloat()
                    )
                )
            }
    ) {
        val min = currentState.min
        val pxPerPoint = currentState.pxPerPoint

        translate(left = currentState.scrolledBy) {
            currentState.barsList.forEachIndexed { index, bar ->
                val offsetX = size.width - index * currentState.barWidth

                drawTimeDelimiter(
                    bar = bar,
                    nextBar = if (index < currentState.barsList.size - 1) {
                        currentState.barsList[index + 1]
                    } else {
                        null
                    },
                    timeFrame = timeFrame,
                    offsetX = offsetX,
                    textMeasurer = textMeasurer
                )
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
                    strokeWidth = currentState.barWidth / 2
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun Prices(
    modifier: Modifier = Modifier,
    stockTrendzState: State<StockTrendzState>,
    lastPrice: Float
) {
    val currentState = stockTrendzState.value
    val textMeasurer = rememberTextMeasurer()

    val max = currentState.max
    val min = currentState.min
    val pxPerPoint = currentState.pxPerPoint

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

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawTimeDelimiter(
    bar: Bar,
    nextBar: Bar?,
    timeFrame: TimeFrame,
    offsetX: Float,
    textMeasurer: TextMeasurer
) {
    val calendar = bar.calendar

    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val shouldDrawDelimiter = when(timeFrame) {
        TimeFrame.MIN_5 -> {
            minutes == 0
        }
        TimeFrame.MIN_15 -> {
            minutes == 0 && hours % 2 == 0
        }
        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> {
            val nextBarDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            nextBarDay != day
        }
    }

    if (!shouldDrawDelimiter) return

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
    )

    val nameOfMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
    val text = when(timeFrame) {
        TimeFrame.MIN_5, TimeFrame.MIN_15 -> { String.format("%02d:00", hours) }
        TimeFrame.MIN_30, TimeFrame.HOUR_1 -> { String.format("%s %s", day, nameOfMonth) }
    }

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(offsetX - textLayoutResult.size.width / 2, size.height)
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