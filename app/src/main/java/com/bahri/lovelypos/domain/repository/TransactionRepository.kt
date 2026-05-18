// MODIFIED — Milestone 6
package com.bahri.lovelypos.domain.repository

import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun createTransaction(
        items: List<CartItem>,
        paymentMethod: String,
        amountPaid: Long
    ): Long

    fun getAllTransactions(): Flow<List<Transaction>>
    
    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>>
    
    suspend fun getTransactionWithItems(transactionId: Long): TransactionWithItems?

    fun getSalesSummary(startMs: Long, endMs: Long): Flow<SummaryReport>
}
