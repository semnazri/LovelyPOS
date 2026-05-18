package com.bahri.lovelypos.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithItems(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionItem>
)