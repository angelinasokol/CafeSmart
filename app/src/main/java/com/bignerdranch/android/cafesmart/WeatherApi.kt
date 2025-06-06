package com.bignerdranch.android.cafesmart

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",  // для Цельсия
        @Query("lang") lang: String = "ru"         // на русском языке
    ): Call<WeatherResponse>
}
