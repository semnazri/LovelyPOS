// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.domain.usecase.GetTransactionDetailUseCase
import com.bahri.lovelypos.domain.usecase.GetTransactionHistoryUseCase
import com.bahri.lovelypos.util.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class HistoryViewModel(
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    private val getTransactionDetailUseCase: GetTransactionDetailUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()

    init {
        // Bug 1 Fix: Set default to local today's start
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        _selectedDate.value = cal.timeInMillis
    }

    private val _historyState = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<Transaction>>> = _historyState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _selectedDate.flatMapLatest { dateMs ->
        _historyState.value = UiState.Loading
        if (dateMs == null) {
            getTransactionHistoryUseCase()
        } else {
            // Bug 1 Fix: DatePicker returns UTC millis. 
            // We need to treat it as "Local Date" midnight.
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCal.timeInMillis = dateMs
            
            val localCal = Calendar.getInstance()
            localCal.set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            localCal.set(Calendar.MILLISECOND, 0)
            
            val startMs = localCal.timeInMillis
            localCal.add(Calendar.DAY_OF_YEAR, 1)
            val endMs = localCal.timeInMillis - 1

            getTransactionHistoryUseCase(startMs, endMs)
        }
    }
    .onEach { items -> _historyState.value = UiState.Success(items) }
    .catch { e -> _historyState.value = UiState.Error(e.message ?: "Unknown Error") }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Long> = transactions.map { list ->
        list.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _selectedTransactionDetail = MutableStateFlow<TransactionWithItems?>(null)
    val selectedTransactionDetail: StateFlow<TransactionWithItems?> = _selectedTransactionDetail.asStateFlow()

    fun setDateFilter(dateMs: Long?) {
        _selectedDate.value = dateMs
    }

    fun loadTransactionDetail(transactionId: Long) {
        viewModelScope.launch {
            _selectedTransactionDetail.value = getTransactionDetailUseCase(transactionId)
        }
    }

    fun clearDetail() {
        _selectedTransactionDetail.value = null
    }
}
