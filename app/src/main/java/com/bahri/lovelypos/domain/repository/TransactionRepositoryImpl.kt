package com.bahri.lovelypos.domain.repository

import androidx.room.withTransaction
import com.bahri.lovelypos.data.db.AppDatabase
import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionItem
import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val db: AppDatabase
) : TransactionRepository {

    private val transactionDao = db.transactionDao()
    private val transactionItemDao = db.transactionItemDao()
    private val menuItemDao = db.menuItemDao()

    override suspend fun createTransaction(
        items: List<CartItem>,
        paymentMethod: String,
        amountPaid: Long
    ): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            val totalAmount = items.sumOf { it.subtotal }
            val change = if (paymentMethod == "Cash") amountPaid - totalAmount else 0L
            
            val transaction = Transaction(
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                change = if (change > 0) change else 0L,
                createdAt = System.currentTimeMillis()
            )
            
            val transactionId = transactionDao.insertTransaction(transaction)
            
            items.forEach { cartItem ->
                val tItem = TransactionItem(
                    transactionId = transactionId,
                    menuItemId = cartItem.menuItem.id,
                    menuItemName = cartItem.menuItem.name,
                    menuItemPrice = cartItem.menuItem.price,
                    quantity = cartItem.quantity,
                    subtotal = cartItem.subtotal
                )
                transactionItemDao.insertTransactionItem(tItem)
                
                val currentMenuItem = menuItemDao.getMenuItemById(cartItem.menuItem.id)
                currentMenuItem?.let {
                    val newStock = it.stock - cartItem.quantity
                    menuItemDao.updateStock(it.id, if (newStock > 0) newStock else 0)
                }
            }
            
            transactionId
        }
    }

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    override fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startMs, endMs)
    }

    override suspend fun getTransactionWithItems(transactionId: Long): TransactionWithItems? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionWithItems(transactionId)
    }

    override suspend fun getSalesSummary(startMs: Long, endMs: Long): SummaryReport = withContext(Dispatchers.IO) {
        val totalRevenue = transactionDao.getRevenueByDateRange(startMs, endMs) ?: 0L
        val transactionCount = transactionDao.getTransactionCountByDateRange(startMs, endMs)
        val topItems = transactionDao.getTopSellingItems(startMs, endMs)
        val paymentBreakdown = transactionDao.getRevenueByPaymentMethod(startMs, endMs)
        val dailyRevenue = transactionDao.getDailyRevenue(startMs, endMs)

        SummaryReport(
            totalRevenue = totalRevenue,
            transactionCount = transactionCount,
            topItems = topItems,
            paymentBreakdown = paymentBreakdown,
            dailyRevenue = dailyRevenue
        )
    }
}
