package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.domain.repository.TransactionRepository

class GetTransactionDetailUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transactionId: Long): TransactionWithItems? {
        return repository.getTransactionWithItems(transactionId)
    }
}