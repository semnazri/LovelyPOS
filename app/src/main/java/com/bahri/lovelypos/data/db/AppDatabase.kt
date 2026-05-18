package com.bahri.lovelypos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bahri.lovelypos.data.dao.MenuItemDao
import com.bahri.lovelypos.data.dao.TransactionDao
import com.bahri.lovelypos.data.dao.TransactionItemDao
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionItem

@Database(
    entities = [MenuItem::class, Transaction::class, TransactionItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionItemDao(): TransactionItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}