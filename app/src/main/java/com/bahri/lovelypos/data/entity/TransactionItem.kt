package com.bahri.lovelypos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "transaction_item")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val menuItemId: Long,
    val menuItemName: String, // snapshot
    val menuItemPrice: Long, // snapshot
    val quantity: Int,
    val subtotal: Long
)