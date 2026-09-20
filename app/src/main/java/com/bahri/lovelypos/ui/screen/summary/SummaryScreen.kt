package com.bahri.lovelypos.ui.screen.summary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.ui.components.common.LovelyEmptyState
import com.bahri.lovelypos.ui.screen.summary.components.CustomDateRangeRow
import com.bahri.lovelypos.ui.screen.summary.components.DatePickerDialogBase
import com.bahri.lovelypos.ui.screen.summary.components.DateRangeSelector
import com.bahri.lovelypos.ui.screen.summary.components.SummaryContent
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
import com.bahri.lovelypos.ui.viewmodel.SummaryViewModel
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = koinViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val dateRangeMode by viewModel.dateRangeMode.collectAsStateWithLifecycle()
    val customStart by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEnd by viewModel.customEndDate.collectAsStateWithLifecycle()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    LovelyPOSTheme { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DateRangeSelector(
                selectedRange = dateRangeMode,
                onRangeSelected = { viewModel.setDateRangeMode(it) }
            )

            if (dateRangeMode == "Custom") {
                CustomDateRangeRow(
                    startDate = customStart,
                    endDate = customEnd,
                    onStartClick = { showStartPicker = true },
                    onEndClick = { showEndPicker = true }
                )
            }

            AnimatedContent(
                targetState = summaryState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SummaryContent"
            ) { state ->
                when (state) {
                    is UiState.Loading -> Box(
                        Modifier.fillMaxSize(),
                        Alignment.Center
                    ) { CircularProgressIndicator() }

                    is UiState.Success -> {
                        val report = state.data
                        if (report == null || report.transactionCount == 0) {
                            LovelyEmptyState(
                                icon = Icons.Default.Info,
                                message = "Tidak ada data untuk periode ini"
                            )
                        } else SummaryContent(report)
                    }

                    is UiState.Error -> Box(
                        Modifier.fillMaxSize(),
                        Alignment.Center
                    ) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (showStartPicker) DatePickerDialogBase({
        showStartPicker = false
    }) { viewModel.setCustomDateRange(it, customEnd); showStartPicker = false }

    if (showEndPicker) DatePickerDialogBase({
        showEndPicker = false
    }) { viewModel.setCustomDateRange(customStart, it); showEndPicker = false }
}
