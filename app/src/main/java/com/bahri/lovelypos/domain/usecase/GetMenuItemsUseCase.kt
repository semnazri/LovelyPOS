package com.bahri.lovelypos.domain.usecase


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.repository.MenuItemRepository

class GetMenuItemsUseCase(private val repository: MenuItemRepository) {
    operator fun invoke(category: String = "Semua"): Flow<List<MenuItem>> = repository.getAll().map { items ->
        items.filter { category == "Semua" || it.category == category }
            .sortedWith(
                compareByDescending<MenuItem> { it.isAvailable && it.stock > 0 }
                    .thenBy { it.name.lowercase() }
            )
    }
}