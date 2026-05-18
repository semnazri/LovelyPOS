package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.repository.MenuItemRepository

class SaveMenuItemUseCase(private val repository: MenuItemRepository) {
    suspend operator fun invoke(item: MenuItem) {
        if (item.id == 0L) {
            repository.insert(item)
        } else {
            repository.update(item)
        }
    }
}