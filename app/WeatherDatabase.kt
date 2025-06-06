@Database(entities = [WeatherRecommendation::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun recommendationDao(): RecommendationDao

    companion object {
        private var instance: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_db"
                ).build().also { instance = it }
            }
        }
    }
}

@Entity(tableName = "recommendations")
data class WeatherRecommendation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val minTemp: Int,
    val maxTemp: Int,
    val weatherCondition: String,
    val iconResId: Int,
    val recommendationText: String,
    val recommendedDrinks: List<Drink>
)

data class Drink(
    val name: String,
    val price: Int,
    val iconResId: Int
)

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: WeatherRecommendation)

    @Query("SELECT * FROM recommendations WHERE :temp BETWEEN minTemp AND maxTemp AND weatherCondition = :condition LIMIT 1")
    suspend fun getRecommendation(temp: Int, condition: String): WeatherRecommendation?
}