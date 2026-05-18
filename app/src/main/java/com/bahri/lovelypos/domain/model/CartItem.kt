package com.bahri.lovelypos.domain.model

import com.bahri.lovelypos.data.entity.MenuItem

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val subtotal: Long = menuItem.price * quantity
}