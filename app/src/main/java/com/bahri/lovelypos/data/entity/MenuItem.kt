package com.bahri.lovelypos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "menu_item")
data class MenuItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Long,
    val stock: Int,
    val isAvailable: Boolean
)