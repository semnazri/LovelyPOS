package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.domain.repository.TransactionRepository

class CreateTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(
        items: List<CartItem>,
        paymentMethod: String,
        amountPaid: Long
    ): Long {
        return repository.createTransaction(items, paymentMethod, amountPaid)
    }
}