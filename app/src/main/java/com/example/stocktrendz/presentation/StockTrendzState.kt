package com.example.stocktrendz.presentation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.stocktrendz.data.model.Bar
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class StockTrendzState(
    val barsList: List<Bar>,
    val visibleBarsCount: Int = 100,
    val scrolledBy: Float = 0f,
    val screenWidth: Float = 1f,
    val screenHeight: Float = 1f
) : Parcelable {

    val barWidth: Float
        get() = screenWidth / visibleBarsCount

    private val visibleBars: List<Bar>
        get() {
            val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
            val endIndex = (startIndex + visibleBarsCount).coerceAtMost(barsList.size)
            return barsList.subList(startIndex, endIndex)
        }

    val max: Float
        get() = visibleBars.maxOf { it.high }
    val min: Float
        get() = visibleBars.minOf { it.low }
    val pxPerPoint: Float
        get() = screenHeight / (max - min)
}

@Composable
fun rememberStockTrendzState(barsList: List<Bar>): MutableState<StockTrendzState> {
    return rememberSaveable {
        mutableStateOf(StockTrendzState(barsList = barsList))
    }
}
