// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.domain.usecase.CreateTransactionUseCase
import com.bahri.lovelypos.domain.usecase.GetMenuItemsUseCase
import com.bahri.lovelypos.util.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class POSViewModel(
    private val getMenuItemsUseCase: GetMenuItemsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _paymentMethod = MutableStateFlow("Cash")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _amountPaidInput = MutableStateFlow("")
    val amountPaidInput: StateFlow<String> = _amountPaidInput.asStateFlow()

    private val _menuState = MutableStateFlow<UiState<List<MenuItem>>>(UiState.Loading)
    val menuState: StateFlow<UiState<List<MenuItem>>> = _menuState.asStateFlow()

    val menuItems: StateFlow<List<MenuItem>> = getMenuItemsUseCase()
        .onEach { items -> _menuState.value = UiState.Success(items) }
        .catch { e -> _menuState.value = UiState.Error(e.message ?: "Unknown Error") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryList: StateFlow<List<String>> = menuItems
        .map { items ->
            listOf("Semua") + items.map { it.category }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Semua"))

    val filteredItems: StateFlow<List<MenuItem>> = combine(menuItems, _selectedCategory) { items, category ->
        if (category == "Semua") items else items.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAmount: StateFlow<Long> = _cart.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val itemCount: StateFlow<Int> = _cart.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val changeAmount: StateFlow<Long> = combine(_amountPaidInput, totalAmount, _paymentMethod) { input, total, method ->
        if (method != "Cash") return@combine 0L
        val paid = input.toLongOrNull() ?: 0L
        if (paid >= total) paid - total else 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val canCheckout: StateFlow<Boolean> = _cart.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canConfirmPayment: StateFlow<Boolean> = combine(_paymentMethod, _amountPaidInput, totalAmount) { method, input, total ->
        if (method == "Cash") {
            (input.toLongOrNull() ?: 0L) >= total
        } else {
            true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setAmountPaid(input: String) {
        if (input.all { it.isDigit() }) {
            _amountPaidInput.value = input
        }
    }

    fun addToCart(item: MenuItem) {
        if (item.stock <= 0 || !item.isAvailable) return
        
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == item.id }
        
        if (index != -1) {
            val existingItem = currentCart[index]
            if (existingItem.quantity < item.stock) {
                currentCart[index] = existingItem.copy(quantity = existingItem.quantity + 1)
            }
        } else {
            currentCart.add(CartItem(item, 1))
        }
        _cart.value = currentCart
    }

    fun removeFromCart(itemId: Long) {
        _cart.value = _cart.value.filter { it.menuItem.id != itemId }
    }

    fun increaseQty(itemId: Long) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == itemId }
        if (index != -1) {
            val item = currentCart[index]
            if (item.quantity < item.menuItem.stock) {
                currentCart[index] = item.copy(quantity = item.quantity + 1)
                _cart.value = currentCart
            }
        }
    }

    fun decreaseQty(itemId: Long) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == itemId }
        if (index != -1) {
            val item = currentCart[index]
            if (item.quantity > 1) {
                currentCart[index] = item.copy(quantity = item.quantity - 1)
                _cart.value = currentCart
            } else {
                removeFromCart(itemId)
            }
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _amountPaidInput.value = ""
        _paymentMethod.value = "Cash"
    }

    private val _paymentSuccess = MutableSharedFlow<Boolean>()
    val paymentSuccess = _paymentSuccess.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    fun processPayment() {
        viewModelScope.launch {
            val items = _cart.value
            val method = _paymentMethod.value
            val total = totalAmount.value
            val paid = if (method == "Cash") _amountPaidInput.value.toLongOrNull() ?: 0L else total

            if (method == "Cash" && paid < total) {
                _errorMessage.emit("Uang tidak mencukupi")
                return@launch
            }

            // Re-validate stock before processing
            val isStockValid = items.all { cartItem ->
                val latestItem = menuItems.value.find { it.id == cartItem.menuItem.id }
                (latestItem?.stock ?: 0) >= cartItem.quantity
            }

            if (!isStockValid) {
                _errorMessage.emit("Stok tidak mencukupi. Periksa kembali keranjang Anda.")
                return@launch
            }

            try {
                createTransactionUseCase(items, method, paid)
                _paymentSuccess.emit(true)
                clearCart()
            } catch (e: Exception) {
                _errorMessage.emit("Terjadi kesalahan: ${e.message}")
            }
        }
    }
}
