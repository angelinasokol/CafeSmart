package com.bignerdranch.android.cafesmart

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.cafesmart.data.DrinkAdapter
import com.bignerdranch.android.cafesmart.data.DrinkDatabase
import com.bignerdranch.android.cafesmart.data.prepopulate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

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
    private var currentCity = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Сопоставление английских городов к русским для отображения
    private val cityNameMap = mapOf(
        "Moscow" to "Москва",
        "Saint Petersburg" to "Санкт-Петербург",
        "Novosibirsk" to "Новосибирск",
        "Yekaterinburg" to "Екатеринбург",
        "Kazan" to "Казань"
    )

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val newCity = data?.getStringExtra(Constants.KEY_CITY)
            if (!newCity.isNullOrEmpty()) {
                currentCity = newCity
                saveCityToPrefs(newCity)
                val displayCity = cityNameMap[newCity] ?: newCity
                cityTextView.text = getString(R.string.city_label, displayCity)
                getWeatherData(newCity)
                reloadDrinks()
                return@registerForActivityResult
            }
        }
        // Если не пришло нового города — обновляем из prefs
        refreshCity()
        reloadDrinks()
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLocationAndUpdateCity()
        } else {
            showToast("Разрешение на доступ к местоположению отклонено")
            refreshCity()
            reloadDrinks()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        val isDarkTheme = prefs.getBoolean(Constants.KEY_DARK_THEME, false)
        setTheme(if (isDarkTheme) R.style.Theme_CafeSmart_Dark else R.style.Theme_CafeSmart_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupDatabase()
        setupWeatherService()
        setupRecyclerView()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loadInitialData()
    }

    override fun onResume() {
        super.onResume()
        refreshCity()
        reloadDrinks()
    }

    private fun loadInitialData() {
        val savedCity = prefs.getString(Constants.KEY_CITY, null)
        if (savedCity.isNullOrEmpty()) {
            requestLocationPermission()
        } else {
            refreshCity()
            reloadDrinks()
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocationAndUpdateCity()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showToast("Для определения погоды необходимо разрешение на доступ к местоположению")
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchLocationAndUpdateCity() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val cityNameRus = addresses[0].locality ?: addresses[0].subAdminArea ?: ""
                    if (cityNameRus.isNotEmpty()) {
                        // Найдём английское имя по русскому
                        val cityNameEng = cityNameMap.entries.find { it.value == cityNameRus }?.key ?: cityNameRus
                        currentCity = cityNameEng
                        cityTextView.text = getString(R.string.city_label, cityNameRus)
                        saveCityToPrefs(cityNameEng)
                        getWeatherData(cityNameEng)
                        reloadDrinks()
                        return@addOnSuccessListener
                    }
                }
                showToast("Не удалось определить город по местоположению")
                refreshCity()
                reloadDrinks()
            } else {
                showToast("Местоположение недоступно")
                refreshCity()
                reloadDrinks()
            }
        }.addOnFailureListener {
            showToast("Ошибка при получении местоположения: ${it.message}")
            refreshCity()
            reloadDrinks()
        }
    }

    private fun saveCityToPrefs(city: String) {
        prefs.edit().putString(Constants.KEY_CITY, city).apply()
    }

    private fun refreshCity() {
        val city = prefs.getString(Constants.KEY_CITY, null)
        if (city.isNullOrEmpty()) {
            cityTextView.text = getString(R.string.city_not_set)
            weatherTextView.text = getString(R.string.weather_not_available)
        } else {
            currentCity = city
            val displayCity = cityNameMap[city] ?: city
            cityTextView.text = getString(R.string.city_label, displayCity)
            getWeatherData(city)
        }
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navigationView)
        weatherTextView = findViewById(R.id.weatherTextView)
        cityTextView = findViewById(R.id.cityTextView)
        drinkRecyclerView = findViewById(R.id.drinkRecyclerView)

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
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    settingsLauncher.launch(intent)
                }
                R.id.nav_about -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    settingsLauncher.launch(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupDatabase() {
        drinkDatabase = DrinkDatabase.getDatabase(this, lifecycleScope)

        // Если хочешь гарантировать заполнение данных (например, при первом запуске), то можно так:
        lifecycleScope.launch {
            prepopulate(drinkDatabase.drinkDao())
            reloadDrinks()  // обновляем данные в адаптере после наполнения базы
        }
    }


    private fun setupWeatherService() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherService = retrofit.create(WeatherApiService::class.java)
    }

    private fun setupRecyclerView() {
        drinkAdapter = DrinkAdapter()
        drinkRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = drinkAdapter
        }
    }

    private fun reloadDrinks() {
        lifecycleScope.launch {
            val drinks = drinkDatabase.drinkDao().getAllDrinksSortedColdToHot()
            drinkAdapter.setDrinks(drinks)
        }
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
                refreshCity()
                reloadDrinks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

}
