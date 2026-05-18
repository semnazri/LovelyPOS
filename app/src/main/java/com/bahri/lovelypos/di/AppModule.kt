package com.bahri.lovelypos.di

import com.bahri.lovelypos.data.db.AppDatabase
import com.bahri.lovelypos.domain.repository.*
import com.bahri.lovelypos.domain.usecase.*
import com.bahri.lovelypos.ui.viewmodel.HistoryViewModel
import com.bahri.lovelypos.ui.viewmodel.MenuViewModel
import com.bahri.lovelypos.ui.viewmodel.POSViewModel
import com.bahri.lovelypos.ui.viewmodel.SummaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.core.module.dsl.singleOf

val appModule = module {
    // Database
    single { AppDatabase.getDatabase(get()) }
    single { get<AppDatabase>().menuItemDao() }
    single { get<AppDatabase>().transactionDao() }
    single { get<AppDatabase>().transactionItemDao() }

    // Repositories
    single<MenuItemRepository> { MenuItemRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }

    // UseCases
    singleOf(::GetMenuItemsUseCase)
    singleOf(::SaveMenuItemUseCase)
    singleOf(::DeleteMenuItemUseCase)
    singleOf(::UpdateStockUseCase)
    singleOf(::CreateTransactionUseCase)
    singleOf(::GetTransactionHistoryUseCase)
    singleOf(::GetTransactionDetailUseCase)
    singleOf(::GetSalesSummaryUseCase)

    // ViewModels
    viewModelOf(::MenuViewModel)
    viewModelOf(::POSViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::SummaryViewModel)
}
