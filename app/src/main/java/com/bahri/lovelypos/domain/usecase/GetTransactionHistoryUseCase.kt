package com.bahri.lovelypos.domain.usecase

import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTransactionHistoryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(startMs: Long? = null, endMs: Long? = null): Flow<List<Transaction>> {
        return if (startMs != null && endMs != null) {
            repository.getTransactionsByDateRange(startMs, endMs)
        } else {
            repository.getAllTransactions()
        }
    }
}