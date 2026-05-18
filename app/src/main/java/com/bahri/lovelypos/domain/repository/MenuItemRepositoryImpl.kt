package com.bahri.lovelypos.domain.repository

import com.bahri.lovelypos.data.dao.MenuItemDao
import com.bahri.lovelypos.data.entity.MenuItem
import kotlinx.coroutines.flow.Flow

class MenuItemRepositoryImpl(
    private val menuItemDao: MenuItemDao
) : MenuItemRepository {

    override fun getAll(): Flow<List<MenuItem>> {
        return menuItemDao.getAllMenuItems()
    }

    override suspend fun insert(item: MenuItem): Long {
        return menuItemDao.insert(item)
    }

    override suspend fun update(item: MenuItem) {
        menuItemDao.update(item)
    }

    override suspend fun delete(item: MenuItem) {
        menuItemDao.delete(item)
    }

    override suspend fun updateStock(id: Long, newStock: Int) {
        menuItemDao.updateStock(id, newStock)
    }
}