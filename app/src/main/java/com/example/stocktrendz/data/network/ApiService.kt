package com.example.stocktrendz.data.network

import com.example.stocktrendz.data.model.Result
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("aggs/ticker/AAPL/range/{timeframe}/{date}?adjusted=true&sort=desc&limit=50000")
    suspend fun loadBars(
        @Path("timeframe") timeframe: String,
        @Path("date") fromTo: String = "2022-01-09/2023-01-09",
        @Query("apiKey") apiKey: String
    ): Result

}