// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.usecase.DeleteMenuItemUseCase
import com.bahri.lovelypos.domain.usecase.GetMenuItemsUseCase
import com.bahri.lovelypos.domain.usecase.SaveMenuItemUseCase
import com.bahri.lovelypos.util.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(
    private val getMenuItemsUseCase: GetMenuItemsUseCase,
    private val saveMenuItemUseCase: SaveMenuItemUseCase,
    private val deleteMenuItemUseCase: DeleteMenuItemUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _menuState = MutableStateFlow<UiState<List<MenuItem>>>(UiState.Loading)
    val menuState: StateFlow<UiState<List<MenuItem>>> = _menuState.asStateFlow()

    val allItems: StateFlow<List<MenuItem>> = getMenuItemsUseCase()
        .onEach { items -> _menuState.value = UiState.Success(items) }
        .catch { e -> _menuState.value = UiState.Error(e.message ?: "Unknown Error") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryList: StateFlow<List<String>> = allItems
        .map { items ->
            listOf("Semua") + items.map { it.category }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Semua"))

    val filteredItems: StateFlow<List<MenuItem>> = combine(allItems, _selectedCategory) { items, category ->
        if (category == "Semua") items else items.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun saveItem(item: MenuItem) {
        viewModelScope.launch {
            try {
                saveMenuItemUseCase(item)
            } catch (e: Exception) {
                // Error handling could be expanded to a separate SharedFlow for UI alerts
            }
        }
    }

    fun deleteItem(item: MenuItem) {
        viewModelScope.launch {
            try {
                deleteMenuItemUseCase(item)
            } catch (e: Exception) {
            }
        }
    }
}
