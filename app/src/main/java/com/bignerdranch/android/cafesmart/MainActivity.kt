package com.bignerdranch.android.cafesmart

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.cafesmart.data.DrinkAdapter
import com.bignerdranch.android.cafesmart.data.DrinkDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var weatherService: WeatherApiService
    private lateinit var weatherTextView: TextView
    private lateinit var cityTextView: TextView
    private lateinit var drinkDatabase: DrinkDatabase
    private lateinit var drinkRecyclerView: RecyclerView
    private lateinit var drinkAdapter: DrinkAdapter

    private lateinit var prefs: SharedPreferences

    // Launcher для SettingsActivity с получением результата
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val city = prefs.getString(Constants.KEY_CITY, "Moscow") ?: "Moscow"
            cityTextView.text = "Город: $city"
            getWeatherData(city)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        val isDarkTheme = prefs.getBoolean(Constants.KEY_DARK_THEME, false)
        setTheme(if (isDarkTheme) R.style.Theme_CafeSmart_Dark else R.style.Theme_CafeSmart_Light)

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navigationView)
        weatherTextView = findViewById(R.id.weatherTextView)
        cityTextView = findViewById(R.id.cityTextView)
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
                    settingsLauncher.launch(intent)  // Запускаем с ожиданием результата
                }
                R.id.nav_about -> showToast("О приложении")
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val city = prefs.getString(Constants.KEY_CITY, "Moscow") ?: "Moscow"
        cityTextView.text = "Город: $city"

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherService = retrofit.create(WeatherApiService::class.java)

        drinkAdapter = DrinkAdapter()
        drinkRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = drinkAdapter
        }

        getWeatherData(city)
    }

    override fun onResume() {
        super.onResume()
        val city = prefs.getString(Constants.KEY_CITY, "Moscow") ?: "Moscow"
        cityTextView.text = "Город: $city"
        getWeatherData(city)
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
                        weatherTextView.text =
                            "Температура: ${weather?.main?.temp ?: "-"}°C\n" +
                                    "Погодные условия: ${weather?.weather?.get(0)?.description ?: "-"}"
                    } else {
                        weatherTextView.text = "Ошибка получения данных"
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherTextView.text = "Ошибка подключения"
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                val city = prefs.getString(Constants.KEY_CITY, "Moscow") ?: "Moscow"
                getWeatherData(city)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
