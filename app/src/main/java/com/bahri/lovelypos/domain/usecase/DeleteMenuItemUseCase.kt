package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.repository.MenuItemRepository

class DeleteMenuItemUseCase(private val repository: MenuItemRepository) {
    suspend operator fun invoke(item: MenuItem) {
        repository.delete(item)
    }
}