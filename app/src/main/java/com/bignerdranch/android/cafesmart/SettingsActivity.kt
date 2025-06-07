package com.bignerdranch.android.cafesmart

import android.content.Context
import android.content.Intent
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

                ) { innerPadding ->
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding),
                        prefs = prefs,
                        onSaveAndExit = { selectedCityEnglish ->
                            val resultIntent = Intent().apply {
                                putExtra(KEY_CITY, selectedCityEnglish)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    prefs: SharedPreferences,
    onSaveAndExit: (selectedCityEnglish: String) -> Unit
) {
    val cities = listOf(
        "Москва" to "Moscow",
        "Санкт-Петербург" to "Saint Petersburg",
        "Новосибирск" to "Novosibirsk",
        "Екатеринбург" to "Yekaterinburg",
        "Казань" to "Kazan",
        "Красноярск" to "Krasnoyarsk",
        "Иркутск" to "Irkutsk",
        "Мурманск" to "Murmansk"
    )
    val englishToRussian = cities.associate { it.second to it.first }

    var selectedCityEnglish by remember { mutableStateOf(prefs.getString(KEY_CITY, "Moscow") ?: "Moscow") }
    var selectedCityRussian by remember { mutableStateOf(englishToRussian[selectedCityEnglish] ?: "Москва") }
    var isNotificationsEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIFICATIONS, true)) }
    var isDarkThemeEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_DARK_THEME, false)) }
    var expanded by remember { mutableStateOf(false) }

    fun saveSetting(key: String, value: Any) {
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
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(text = "Выберите город", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCityRussian,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                label = { Text("Город") },
                trailingIcon = null // Без иконок
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
                            selectedCityEnglish = englishName
                            selectedCityRussian = russianName
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Включить уведомления", style = MaterialTheme.typography.bodyLarge)
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
            Text("Темная тема", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkThemeEnabled,
                onCheckedChange = { isDarkThemeEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                saveSetting(KEY_CITY, selectedCityEnglish)
                saveSetting(KEY_NOTIFICATIONS, isNotificationsEnabled)
                saveSetting(KEY_DARK_THEME, isDarkThemeEnabled)
                expanded = false
                onSaveAndExit(selectedCityEnglish)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить и выйти")
        }
    }
}
