// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahri.lovelypos.domain.model.SummaryReport
import com.bahri.lovelypos.domain.usecase.GetSalesSummaryUseCase
import com.bahri.lovelypos.util.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class SummaryViewModel(
    private val getSalesSummaryUseCase: GetSalesSummaryUseCase
) : ViewModel() {

    private val _summaryState = MutableStateFlow<UiState<SummaryReport?>>(UiState.Loading)
    val summaryState: StateFlow<UiState<SummaryReport?>> = _summaryState.asStateFlow()

    private val _dateRangeMode = MutableStateFlow("Hari ini")
    val dateRangeMode: StateFlow<String> = _dateRangeMode.asStateFlow()

    private val _customStartDate = MutableStateFlow<Long?>(null)
    val customStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow<Long?>(null)
    val customEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()

    init {
        loadSummary("Hari ini")
    }

    fun setDateRangeMode(mode: String) {
        _dateRangeMode.value = mode
        if (mode != "Custom") {
            loadSummary(mode)
        }
    }

    fun setCustomDateRange(start: Long?, end: Long?) {
        _customStartDate.value = start
        _customEndDate.value = end
        if (start != null && end != null) {
            loadSummary("Custom")
        }
    }

    private fun loadSummary(mode: String) {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading
            try {
                val (start, end) = when (mode) {
                    "Hari ini" -> getTodayRange()
                    "Minggu ini" -> getThisWeekRange()
                    "Bulan ini" -> getThisMonthRange()
                    "Custom" -> {
                        val s = _customStartDate.value ?: return@launch
                        val e = _customEndDate.value ?: return@launch
                        Pair(s, e)
                    }
                    else -> getTodayRange()
                }
                val report = getSalesSummaryUseCase(start, end)
                _summaryState.value = UiState.Success(report)
            } catch (e: Exception) {
                _summaryState.value = UiState.Error(e.message ?: "Unknown Error")
            }
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
