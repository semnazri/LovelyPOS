package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.domain.repository.MenuItemRepository

class UpdateStockUseCase(private val repository: MenuItemRepository) {
    suspend operator fun invoke(id: Long, newStock: Int) {
        repository.updateStock(id, newStock)
    }
}