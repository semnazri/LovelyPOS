package com.bahri.lovelypos.data.dao

import androidx.room.*
import com.bahri.lovelypos.data.entity.MenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Insert
    suspend fun insert(item: MenuItem): Long

    @Update
    suspend fun update(item: MenuItem): Int

    @Delete
    suspend fun delete(item: MenuItem): Int

    @Query("SELECT * FROM menu_item")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_item WHERE id = :id")
    suspend fun getMenuItemById(id: Long): MenuItem?

    @Query("UPDATE menu_item SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int): Int
}