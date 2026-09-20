package com.bahri.lovelypos.ui.screen.history

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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.ui.components.common.LovelyEmptyState
import com.bahri.lovelypos.ui.screen.history.components.SummaryBar
import com.bahri.lovelypos.ui.screen.history.components.TransactionDetailContent
import com.bahri.lovelypos.ui.screen.history.components.TransactionList
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
import com.bahri.lovelypos.ui.viewmodel.HistoryViewModel
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val selectedDetail by viewModel.selectedTransactionDetail.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDetailSheet by remember { mutableStateOf(false) }

    val dateLabel = if (selectedDate == null) "Semua Waktu" else formatDateLabel(selectedDate!!)

    LovelyPOSTheme { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SummaryBar(
                dateLabel = dateLabel,
                count = transactions.size,
                total = totalRevenue,
                onDateClick = { showDatePicker = true }
            )

            AnimatedContent(
                targetState = historyState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "HistoryContent"
            ) { state ->
                when (state) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            LovelyEmptyState(
                                icon = Icons.Default.Info,
                                message = "Tidak ada transaksi pada tanggal ini"
                            )
                        } else {
                            TransactionList(
                                transactions = transactions,
                                showHeaders = selectedDate == null,
                                onItemClick = {
                                    viewModel.loadTransactionDetail(it.id)
                                    showDetailSheet = true
                                }
                            )
                        }
                    }

                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDateFilter(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.setDateFilter(null)
                    showDatePicker = false
                }) { Text("Semua") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDetailSheet && selectedDetail != null) {
        ModalBottomSheet(onDismissRequest = { showDetailSheet = false }) {
            TransactionDetailContent(detail = selectedDetail!!)
        }
    }
}

private fun formatDateLabel(dateMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(dateMillis))
}
