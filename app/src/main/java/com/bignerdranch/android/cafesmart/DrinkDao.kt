package com.bignerdranch.android.cafesmart.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks ORDER BY temperatureLevel ASC")
    suspend fun getAllDrinksSortedColdToHot(): List<Drink>

    @Query("SELECT * FROM drinks ORDER BY temperatureLevel DESC")
    suspend fun getAllDrinksSortedHotToCold(): List<Drink>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drinks: List<Drink>)

    @Query("DELETE FROM drinks")
    suspend fun clearAll()
}
