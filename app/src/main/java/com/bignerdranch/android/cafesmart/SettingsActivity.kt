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
                        prefs = prefs,
                        onSaveAndExit = {
                            setResult(RESULT_OK)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    prefs: SharedPreferences,
    onSaveAndExit: () -> Unit
) {
    var selectedCity by remember {
        mutableStateOf(prefs.getString(KEY_CITY, "Moscow") ?: "Moscow")
    }
    var isNotificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    }
    var isDarkThemeEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DARK_THEME, false))
    }

    val cities = listOf(
        "Москва" to "Moscow",
        "Санкт-Петербург" to "Saint Petersburg",
        "Новосибирск" to "Novosibirsk",
        "Екатеринбург" to "Yekaterinburg",
        "Казань" to "Kazan",
        // остальные города
    )

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
                cities.forEach { (russianName, englishName) ->
                    DropdownMenuItem(
                        text = { Text(russianName) },
                        onClick = {
                            selectedCity = englishName
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Включить уведомления")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isNotificationsEnabled,
                onCheckedChange = { isNotificationsEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Темная тема")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkThemeEnabled,
                onCheckedChange = { isDarkThemeEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                saveSetting(Constants.KEY_CITY, selectedCity)
                saveSetting(Constants.KEY_NOTIFICATIONS, isNotificationsEnabled)
                saveSetting(Constants.KEY_DARK_THEME, isDarkThemeEnabled)
                onSaveAndExit()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить и выйти")
        }
    }
}
