package com.bahri.lovelypos.data.dao

import androidx.room.*
import com.bahri.lovelypos.data.entity.TransactionItem

@Dao
interface TransactionItemDao {
    @Insert
    suspend fun insertTransactionItem(item: TransactionItem): Long
}