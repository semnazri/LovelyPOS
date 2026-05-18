package com.bahri.lovelypos.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.domain.model.DailyRevenue
import com.bahri.lovelypos.domain.model.ItemSalesSummary
import com.bahri.lovelypos.domain.model.PaymentMethodSummary
import androidx.room.Transaction as RoomTransaction

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM `transaction` ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM `transaction` WHERE createdAt >= :startMs AND createdAt <= :endMs ORDER BY createdAt DESC")
    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>>

    @RoomTransaction
    @Query("SELECT * FROM `transaction` WHERE id = :transactionId")
    suspend fun getTransactionWithItems(transactionId: Long): TransactionWithItems?

    @Query("SELECT SUM(totalAmount) FROM `transaction` WHERE createdAt >= :start AND createdAt <= :end")
    suspend fun getRevenueByDateRange(start: Long, end: Long): Long?

    @Query("SELECT COUNT(*) FROM `transaction` WHERE createdAt >= :start AND createdAt <= :end")
    suspend fun getTransactionCountByDateRange(start: Long, end: Long): Int

    @Query("""
        SELECT menuItemName, SUM(quantity) as totalQty, SUM(subtotal) as totalRevenue 
        FROM transaction_item 
        INNER JOIN `transaction` ON transaction_item.transactionId = `transaction`.id 
        WHERE createdAt >= :start AND createdAt <= :end 
        GROUP BY menuItemName 
        ORDER BY totalQty DESC
    """)
    suspend fun getTopSellingItems(start: Long, end: Long): List<ItemSalesSummary>

    @Query("""
        SELECT paymentMethod, COUNT(*) as count, SUM(totalAmount) as total 
        FROM `transaction` 
        WHERE createdAt >= :start AND createdAt <= :end 
        GROUP BY paymentMethod
    """)
    suspend fun getRevenueByPaymentMethod(start: Long, end: Long): List<PaymentMethodSummary>

    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(createdAt / 1000, 'unixepoch', 'localtime')) as dateLabel, 
               SUM(totalAmount) as total, 
               COUNT(*) as count 
        FROM `transaction` 
        WHERE createdAt >= :start AND createdAt <= :end 
        GROUP BY dateLabel 
        ORDER BY dateLabel ASC
    """)
    suspend fun getDailyRevenue(start: Long, end: Long): List<DailyRevenue>
}
