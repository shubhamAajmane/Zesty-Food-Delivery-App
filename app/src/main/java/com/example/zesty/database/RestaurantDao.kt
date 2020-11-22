package com.example.zesty.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RestaurantDao {

    @Insert
    fun insertRestaurant(restaurantEntity: RestaurantEntity)

    @Delete
    fun deleteRestaurant(restaurantEntity: RestaurantEntity)

    @Query("SELECT * FROM restaurants")
    fun getAllRestaurant(): List<RestaurantEntity>

    @Query("SELECT * FROM restaurants WHERE restaurant_id = :restId")
    fun getRestaurantById(restId: String): RestaurantEntity
}