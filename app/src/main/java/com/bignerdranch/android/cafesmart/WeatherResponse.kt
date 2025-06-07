package com.bignerdranch.android.cafesmart

data class WeatherResponse(
    val main: Main?,
    val weather: List<WeatherDescription>?
) {
    data class Main(
        val temp: Float? // Float, а не Double
    )
    data class WeatherDescription(
        val description: String?
    )
}
