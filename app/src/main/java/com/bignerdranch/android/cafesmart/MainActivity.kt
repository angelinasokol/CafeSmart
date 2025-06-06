package com.bignerdranch.android.cafesmart

import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.TextView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView



class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var weatherService: WeatherApiService
    private lateinit var weatherTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navigationView)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)



        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> showToast("Главная")
                R.id.nav_orders -> showToast("История заказов")
                R.id.nav_settings -> showToast("Настройки")
                R.id.nav_about -> showToast("О приложении")
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true

        }
        weatherTextView = findViewById(R.id.weatherTextView)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(WeatherApiService::class.java)

        getWeatherData("Moscow") // <-- можешь сменить город

    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
    private fun getWeatherData(city: String) {
        val apiKey = "26e4d36cd821d447f9da5f40f7a01f56"

        weatherService.getCurrentWeather(city, apiKey, "metric")
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weather = response.body()
                        weather?.let {
                            val temp = it.main.temp
                            val description = it.weather[0].description
                            val output = "Температура: $temp°C\nПогода: $description"
                            weatherTextView.text = output
                        }
                    } else {
                        weatherTextView.text = "Ошибка: ${response.code()}"
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherTextView.text = "Ошибка подключения: ${t.localizedMessage}"
                }
            })
    }

}
