package com.example.stocktrendz.data.network

import com.example.stocktrendz.data.model.Result
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("aggs/ticker/AAPL/range/{multiplier}/{timespan}/{from}/{to}?adjusted=true&sort=desc&limit=50000")
    suspend fun loadBars(
        @Path("multiplier") multiplier: Int = 1,
        @Path("timespan") timeSpan: String = "hour",
        @Path("from") from: String = "2022-01-09",
        @Path("to") to: String = "2023-01-09",
        @Query("apiKey") apiKey: String
    ): Result

}