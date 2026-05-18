package com.bahri.lovelypos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "transaction")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalAmount: Long,
    val paymentMethod: String, // "Cash", "Transfer", "QRIS"
    val amountPaid: Long,
    val change: Long,
    val createdAt: Long
)