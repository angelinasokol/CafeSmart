package com.bignerdranch.android.cafesmart

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.cafesmart.ui.theme.CafeSmartTheme

private const val PREFS_NAME = "app_prefs"
private const val KEY_CITY = "city"
private const val KEY_NOTIFICATIONS = "notifications"
private const val KEY_DARK_THEME = "dark_theme"

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            val isDarkThemePref = prefs.getBoolean(KEY_DARK_THEME, false)

            CafeSmartTheme(darkTheme = isDarkThemePref) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        SmallTopAppBar(
                            title = { Text("Настройки") }
                        )
                    }
                ) { innerPadding ->
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        prefs = prefs
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, prefs: SharedPreferences) {
    // Читаем из prefs начальные значения
    var selectedCity by remember {
        mutableStateOf(prefs.getString(KEY_CITY, "Moscow") ?: "Moscow")
    }
    var isNotificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    }
    var isDarkThemeEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DARK_THEME, false))
    }

    // Список городов для выбора (можно расширить)
    val cities = listOf("Moscow", "Saint Petersburg", "Novosibirsk", "Yekaterinburg", "Kazan")

    var expanded by remember { mutableStateOf(false) }

    val saveSetting = { key: String, value: Any ->
        with(prefs.edit()) {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                else -> error("Unsupported type")
            }
            apply()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Настройки приложения",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Выбор города через DropdownMenu
        Text(text = "Выберите город", style = MaterialTheme.typography.titleMedium)
        Box {
            Text(
                text = selectedCity,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            selectedCity = city
                            saveSetting(KEY_CITY, city)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Переключатель уведомлений
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Включить уведомления")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isNotificationsEnabled,
                onCheckedChange = {
                    isNotificationsEnabled = it
                    saveSetting(KEY_NOTIFICATIONS, it)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Переключатель темы
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Темная тема")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkThemeEnabled,
                onCheckedChange = {
                    isDarkThemeEnabled = it
                    saveSetting(KEY_DARK_THEME, it)
                    // Для применения темы нужно перезапустить активити или приложение, можно показать Toast
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    // Для превью используем пустой SharedPreferences-подобный объект
    val fakePrefs = object : SharedPreferences {
        private val map = mutableMapOf<String, Any>(
            KEY_CITY to "Moscow",
            KEY_NOTIFICATIONS to true,
            KEY_DARK_THEME to false
        )

        override fun getAll() = map.toMap()
        override fun getString(key: String?, defValue: String?) = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?) = null
        override fun getInt(key: String?, defValue: Int) = 0
        override fun getLong(key: String?, defValue: Long) = 0L
        override fun getFloat(key: String?, defValue: Float) = 0f
        override fun getBoolean(key: String?, defValue: Boolean) = map[key] as? Boolean ?: defValue
        override fun contains(key: String?) = map.containsKey(key)
        override fun edit() = throw UnsupportedOperationException()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    CafeSmartTheme {
        SettingsScreen(prefs = fakePrefs)
    }
}
