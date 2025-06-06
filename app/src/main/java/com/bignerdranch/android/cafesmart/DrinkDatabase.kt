package com.bignerdranch.android.cafesmart.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Drink::class], version = 1, exportSchema = false)
abstract class DrinkDatabase : RoomDatabase() {

    abstract fun drinkDao(): DrinkDao

    companion object {
        @Volatile
        private var INSTANCE: DrinkDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DrinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrinkDatabase::class.java,
                    "drink_database"
                )
                    .addCallback(DrinkDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DrinkDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // При создании базы наполняем её начальными данными
            INSTANCE?.let { database ->
                scope.launch {
                    prepopulate(database.drinkDao())
                }
            }
        }
    }
}

// Функция для начального заполнения БД напитками
suspend fun prepopulate(dao: DrinkDao) {
    dao.clearAll() // очистка (на всякий случай)
    dao.insertAll(
        listOf(
            // Горячие напитки (temperatureLevel = 2)
            Drink(name = "Эспрессо", temperatureLevel = 2),
            Drink(name = "Американо", temperatureLevel = 2),
            Drink(name = "Капучино", temperatureLevel = 2),
            Drink(name = "Латте", temperatureLevel = 2),

            // Тёплые напитки (temperatureLevel = 1)
            Drink(name = "Мокка", temperatureLevel = 1),
            Drink(name = "Раф кофе", temperatureLevel = 1),

            // Холодные напитки (temperatureLevel = 0)
            Drink(name = "Фраппе", temperatureLevel = 0),
            Drink(name = "Айс-латте", temperatureLevel = 0),
            Drink(name = "Холодный чай", temperatureLevel = 0),
            Drink(name = "Лимонад", temperatureLevel = 0)
        )
    )
}
