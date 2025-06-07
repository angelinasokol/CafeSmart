package com.bignerdranch.android.cafesmart

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class SettingsActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            val isDarkThemePref = prefs.getBoolean(KEY_DARK_THEME, false)

            CafeSmartTheme(darkTheme = isDarkThemePref) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(
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
    prefs: SharedPreferences,
    onSaveAndExit: (String) -> Unit
) {
    val cities = listOf(
        "Москва" to "Moscow",
        "Санкт-Петербург" to "Saint Petersburg",
        "Новосибирск" to "Novosibirsk",
        "Екатеринбург" to "Yekaterinburg",
        "Казань" to "Kazan"
    )

    var selectedCityEnglish by remember {
        mutableStateOf(prefs.getString(KEY_CITY, "Moscow") ?: "Moscow")
    }
    var isNotificationsEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_NOTIFICATIONS, true))
    }
    var isDarkThemeEnabled by remember {
        mutableStateOf(prefs.getBoolean(KEY_DARK_THEME, false))
    }
    var expanded by remember { mutableStateOf(false) }

    val selectedCityRussian = cities.find { it.second == selectedCityEnglish }?.first ?: "Москва"

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Настройки приложения",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Выбор города с ExposedDropdownMenuBox
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedCityRussian,
                onValueChange = {},
                readOnly = true,
                label = { Text("Город") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cities.forEach { (russianName, englishName) ->
                    DropdownMenuItem(
                        text = { Text(russianName) },
                        onClick = {
                            selectedCityEnglish = englishName
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // Настройка уведомлений
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Включить уведомления",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isNotificationsEnabled,
                onCheckedChange = { isNotificationsEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Настройка темы
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Темная тема",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkThemeEnabled,
                onCheckedChange = { isDarkThemeEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Кнопка сохранения
        Button(
            onClick = {
                prefs.edit().apply {
                    putString(KEY_CITY, selectedCityEnglish)
                    putBoolean(KEY_NOTIFICATIONS, isNotificationsEnabled)
                    putBoolean(KEY_DARK_THEME, isDarkThemeEnabled)
                    apply()
                }
                onSaveAndExit(selectedCityEnglish)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить и выйти")
        }
    }
}