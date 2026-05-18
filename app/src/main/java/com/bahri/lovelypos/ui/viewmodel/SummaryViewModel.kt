// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahri.lovelypos.domain.model.SummaryReport
import com.bahri.lovelypos.domain.usecase.GetSalesSummaryUseCase
import com.bahri.lovelypos.util.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class SummaryViewModel(
    private val getSalesSummaryUseCase: GetSalesSummaryUseCase
) : ViewModel() {

    private val _dateRangeMode = MutableStateFlow("Hari ini")
    val dateRangeMode: StateFlow<String> = _dateRangeMode.asStateFlow()

    private val _customStartDate = MutableStateFlow<Long?>(null)
    val customStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<Long?>(null)
    val customEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val summaryState: StateFlow<UiState<SummaryReport?>> = combine(
        _dateRangeMode, _customStartDate, _customEndDate
    ) { mode, customStart, customEnd ->
        Triple(mode, customStart, customEnd)
    }.flatMapLatest { (mode, start, end) ->
        val range = when (mode) {
            "Hari ini" -> getTodayRange()
            "Minggu ini" -> getThisWeekRange()
            "Bulan ini" -> getThisMonthRange()
            "Custom" -> {
                if (start != null && end != null) Pair(start, end) else null
            }
            else -> getTodayRange()
        }

        if (range == null) {
            flowOf(UiState.Success(null))
        } else {
            getSalesSummaryUseCase(range.first, range.second)
                .map { UiState.Success(it) as UiState<SummaryReport?> }
                .onStart { emit(UiState.Loading) }
                .catch { emit(UiState.Error(it.message ?: "Unknown error")) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun setDateRangeMode(mode: String) {
        _dateRangeMode.value = mode
    }

    fun setCustomDateRange(start: Long?, end: Long?) {
        _customStartDate.value = start
        _customEndDate.value = end
        if (start != null && end != null) {
            _dateRangeMode.value = "Custom"
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    private fun getThisWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    private fun getThisMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
