package com.bignerdranch.android.cafesmart.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Entity(tableName = "drinks")
data class Drink(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val temperatureCategory: String // "hot", "warm", "cold"
)

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drinks")
    suspend fun getAllDrinks(): List<Drink>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drinks: List<Drink>)
}

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

    private class DrinkDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.drinkDao())
                }
            }
        }

        suspend fun populateDatabase(drinkDao: DrinkDao) {
            // Очистим базу (если нужно)
            // drinkDao.deleteAll()

            val drinks = listOf(
                // Горячие напитки
                Drink(name = "Эспрессо", temperatureCategory = "hot"),
                Drink(name = "Капучино", temperatureCategory = "hot"),
                Drink(name = "Латте", temperatureCategory = "hot"),
                Drink(name = "Американо", temperatureCategory = "hot"),
                Drink(name = "Ристретто", temperatureCategory = "hot"),
                Drink(name = "Мокко", temperatureCategory = "hot"),
                Drink(name = "Горячий шоколад", temperatureCategory = "hot"),
                Drink(name = "Чай черный", temperatureCategory = "hot"),
                Drink(name = "Чай зеленый", temperatureCategory = "hot"),
                Drink(name = "Чай травяной", temperatureCategory = "hot"),

                // Теплые напитки (например, чуть прохладнее горячих)
                Drink(name = "Латте со льдом", temperatureCategory = "warm"),
                Drink(name = "Кофе с молоком", temperatureCategory = "warm"),
                Drink(name = "Матча латте", temperatureCategory = "warm"),
                Drink(name = "Чай улун", temperatureCategory = "warm"),
                Drink(name = "Имбирный чай", temperatureCategory = "warm"),

                // Холодные напитки
                Drink(name = "Холодный чай", temperatureCategory = "cold"),
                Drink(name = "Фраппучино", temperatureCategory = "cold"),
                Drink(name = "Мохито безалкогольный", temperatureCategory = "cold"),
                Drink(name = "Холодный кофе", temperatureCategory = "cold"),
                Drink(name = "Свежевыжатый апельсиновый сок", temperatureCategory = "cold"),
                Drink(name = "Лимонад", temperatureCategory = "cold"),
                Drink(name = "Коктейль смузи", temperatureCategory = "cold"),
                Drink(name = "Холодная вода", temperatureCategory = "cold"),
                Drink(name = "Газированная вода", temperatureCategory = "cold"),
                Drink(name = "Айс латте", temperatureCategory = "cold")
            )
            drinkDao.insertAll(drinks)
        }
    }
}
