package com.bignerdranch.android.cafesmart

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Запрос местоположения и погоды
        getWeatherForCurrentLocation()

        // Обработчик кнопки меню
        fullMenuButton.setOnClickListener {
            startActivity(Intent(this, FullMenuActivity::class.java))
        }
    }

    private fun getWeatherForCurrentLocation() {
        // Здесь будет код для получения местоположения
        // и запроса погоды через API

        // Пример обновления UI после получения данных
        updateUIForWeather(24.0, "sunny")
    }

    private fun updateUIForWeather(temperature: Double, weatherCondition: String) {
        temperatureText.text = "${temperature.toInt()}°C"

        // Установка соответствующей иконки погоды
        val weatherIconRes = when (weatherCondition.toLowerCase()) {
            "sunny" -> R.drawable.ic_sunny
            "rainy" -> R.drawable.ic_rainy
            "cloudy" -> R.drawable.ic_cloudy
            "windy" -> R.drawable.ic_windy
            else -> R.drawable.ic_sunny
        }
        weatherIcon.setImageResource(weatherIconRes)

        // Обновление рекомендации на основе погоды
        val recommendation = when {
            temperature > 25 -> "Hot day! Perfect for Iced Coffee"
            temperature < 15 -> "Cold weather? Try our Hot Chocolate"
            else -> "In this weather we recommend Vanilla Cappuccino"
        }
        weatherRecommendation.text = recommendation

        // Обновление рекомендованных напитков
        updateRecommendedItems(temperature, weatherCondition)
    }

    private fun updateRecommendedItems(temperature: Double, weatherCondition: String) {
        // Здесь будет логика подбора рекомендаций на основе погоды
        // Например:
        if (temperature > 22) {
            recommendation1Name.text = "Iced Latte"
            recommendation1Price.text = "€ 3,80"
            recommendation2Name.text = "Cold Brew"
            recommendation2Price.text = "€ 4,20"
        } else {
            recommendation1Name.text = "Flat White"
            recommendation1Price.text = "€ 4,00"
            recommendation2Name.text = "Caramel Macchiato"
            recommendation2Price.text = "€ 4,50"
        }
    }
}