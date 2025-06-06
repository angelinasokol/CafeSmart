package com.bignerdranch.android.cafesmart

data class WeatherResponse(
    val name: String, // название города
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Float // температура
)

data class Weather(
    val description: String, // описание (например, "ясно")
    val icon: String         // иконка погоды
)
