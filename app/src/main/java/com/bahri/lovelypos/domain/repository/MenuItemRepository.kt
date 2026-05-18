package com.bahri.lovelypos.domain.repository
import kotlinx.coroutines.flow.Flow
import com.bahri.lovelypos.data.entity.MenuItem

interface MenuItemRepository {
    fun getAll(): Flow<List<MenuItem>>
    suspend fun insert(item: MenuItem): Long
    suspend fun update(item: MenuItem)
    suspend fun delete(item: MenuItem)
    suspend fun updateStock(id: Long, newStock: Int)
}