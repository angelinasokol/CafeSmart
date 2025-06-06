package com.bignerdranch.android.cafesmart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.TextView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.lifecycleScope
import com.bignerdranch.android.cafesmart.data.DrinkDatabase
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.cafesmart.data.DrinkAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var weatherService: WeatherApiService
    private lateinit var weatherTextView: TextView
    private lateinit var cityTextView: TextView  // Новый TextView для показа города
    private lateinit var drinkDatabase: DrinkDatabase
    private lateinit var drinkRecyclerView: RecyclerView
    private lateinit var drinkAdapter: DrinkAdapter

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Проверка темы и установка (если надо — кастомизируй тему в стиле)
        val isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
        setTheme(if (isDarkTheme) R.style.Theme_CafeSmart_Dark else R.style.Theme_CafeSmart_Light)

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navigationView)
        weatherTextView = findViewById(R.id.weatherTextView)
        cityTextView = findViewById(R.id.cityTextView)  // инициализация нового TextView
        drinkRecyclerView = findViewById(R.id.drinkRecyclerView)

        drinkDatabase = DrinkDatabase.getDatabase(this, lifecycleScope)

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
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_about -> showToast("О приложении")
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val city = prefs.getString(KEY_CITY, "Moscow") ?: "Moscow"
        cityTextView.text = "Город: $city"

        // Retrofit и WeatherService
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherService = retrofit.create(WeatherApiService::class.java)

        // Настройка RecyclerView и адаптера
        drinkAdapter = DrinkAdapter()
        drinkRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = drinkAdapter
        }

        // Запрос погоды и обновление списка напитков
        getWeatherData(city)
    }

    override fun onResume() {
        super.onResume()
        // При возвращении с настроек обновляем город и данные
        val city = prefs.getString(KEY_CITY, "Moscow") ?: "Moscow"
        cityTextView.text = "Город: $city"
        getWeatherData(city)

        // Можно также обновить тему или уведомления, если нужно
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun getWeatherData(city: String) {
        val apiKey = "32d36cf9e173a3ed9ed3e075aaec4e86"

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

                            showDrinksBasedOnTemperature(temp.toDouble())
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

    private fun showDrinksBasedOnTemperature(outsideTemp: Double) {
        lifecycleScope.launch {
            val drinks = if (outsideTemp < 10) {
                drinkDatabase.drinkDao().getAllDrinksSortedHotToCold()
            } else {
                drinkDatabase.drinkDao().getAllDrinksSortedColdToHot()
            }
            drinkAdapter.setDrinks(drinks)
        }
    }
}
