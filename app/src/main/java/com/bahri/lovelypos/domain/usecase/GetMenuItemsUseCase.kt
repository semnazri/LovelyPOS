package com.bahri.lovelypos.domain.usecase


import kotlinx.coroutines.flow.Flow
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.repository.MenuItemRepository

class GetMenuItemsUseCase(private val repository: MenuItemRepository) {
    operator fun invoke(): Flow<List<MenuItem>> = repository.getAll()
}