package com.bignerdranch.android.cafesmart.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drinks")
data class Drink(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // автоинкрементный id
    val name: String, // название напитка
    val temperatureLevel: Int // уровень температуры: 0 - холодный, 1 - теплый, 2 - горячий
)
