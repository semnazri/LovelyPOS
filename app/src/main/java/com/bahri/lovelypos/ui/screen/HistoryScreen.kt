// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
import com.bahri.lovelypos.ui.viewmodel.HistoryViewModel
import com.bahri.lovelypos.util.CurrencyFormatter
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = koinViewModel()) {
    val historyState by viewModel.historyState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val selectedDateMs by viewModel.selectedDate.collectAsStateWithLifecycle()
    val totalRevenue by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val detail by viewModel.selectedTransactionDetail.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > 600

    val dateLabel =
        if (selectedDateMs == null) "Semua Riwayat" else formatDateLabel(selectedDateMs!!)

    LovelyPOSTheme { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            SummaryBar(dateLabel, transactions.size, totalRevenue) { viewModel.setDateFilter(null) }

            AnimatedContent(
                targetState = historyState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "HistoryContent"
            ) { state ->
                when (state) {
                    is UiState.Loading -> Box(
                        Modifier.fillMaxSize(),
                        Alignment.Center
                    ) { CircularProgressIndicator() }

                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            EmptyHistoryState()
                        } else {
                            if (isLandscape) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(0.4f)) {
                                        TransactionList(
                                            transactions,
                                            selectedDateMs == null
                                        ) { viewModel.loadTransactionDetail(it.id) }
                                    }
                                    VerticalDivider()
                                    Box(modifier = Modifier.weight(0.6f)) {
                                        if (detail != null) TransactionDetailContent(detail!!)
                                        else Box(
                                            Modifier.fillMaxSize(),
                                            Alignment.Center
                                        ) {
                                            Text(
                                                "Pilih transaksi untuk rincian",
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            } else {
                                TransactionList(
                                    transactions,
                                    selectedDateMs == null
                                ) { viewModel.loadTransactionDetail(it.id) }
                            }
                        }
                    }

                    is UiState.Error -> Box(
                        Modifier.fillMaxSize(),
                        Alignment.Center
                    ) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMs ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMs ->
                        // Convert UTC midnight from DatePicker to Local midnight
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            .apply { timeInMillis = utcMs }
                        val localCal = Calendar.getInstance().apply {
                            set(
                                utcCal.get(Calendar.YEAR),
                                utcCal.get(Calendar.MONTH),
                                utcCal.get(Calendar.DAY_OF_MONTH),
                                0,
                                0,
                                0
                            )
                            set(Calendar.MILLISECOND, 0)
                        }
                        viewModel.setDateFilter(localCal.timeInMillis)
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (!isLandscape && detail != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearDetail() },
            dragHandle = { BottomSheetDefaults.DragHandle() }) {
            TransactionDetailContent(detail!!, Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun SummaryBar(label: String, count: Int, revenue: Long, onClear: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontWeight = FontWeight.Bold)
                    if (label != "Semua Riwayat") {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Semua",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onClear() })
                    }
                }
                Text("$count Transaksi", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Pendapatan", style = MaterialTheme.typography.labelSmall)
                Text(
                    CurrencyFormatter.formatRupiah(revenue),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    showHeaders: Boolean,
    onItemClick: (Transaction) -> Unit
) {
    val grouped =
        if (showHeaders) transactions.groupBy { formatDateLabel(it.createdAt) } else mapOf("" to transactions)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (date, list) ->
            if (date.isNotEmpty()) {
                stickyHeader {
                    Text(
                        date,
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(list, key = { it.id }) { transaction ->
                TransactionCard(transaction) { onItemClick(transaction) }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: Transaction, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp)) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("#${transaction.id.toString().padStart(4, '0')}", fontWeight = FontWeight.Bold)
                Text(
                    formatTime(transaction.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PaymentBadge(transaction.paymentMethod)
                Spacer(Modifier.width(12.dp))
                Text(
                    CurrencyFormatter.formatRupiah(transaction.totalAmount),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PaymentBadge(method: String) {
    val color = when (method) {
        "Cash" -> Color(0xFF2E7D32)
        "Transfer" -> Color(0xFF1565C0)
        else -> Color(0xFFE65100)
    }
    Surface(color = color.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
        Text(
            method,
            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransactionDetailContent(detail: TransactionWithItems, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text(
                    "ID Transaksi #${detail.transaction.id.toString().padStart(4, '0')}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    SimpleDateFormat(
                        "dd MMM yyyy, HH:mm",
                        Locale("id", "ID")
                    ).format(Date(detail.transaction.createdAt)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            PaymentBadge(detail.transaction.paymentMethod)
        }
        HorizontalDivider()
        Text("Rincian Item", fontWeight = FontWeight.Bold)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            detail.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(item.menuItemName)
                        Text(
                            "${item.quantity} x ${CurrencyFormatter.formatRupiah(item.menuItemPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        CurrencyFormatter.formatRupiah(item.subtotal),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DetailRow("Total", CurrencyFormatter.formatRupiah(detail.transaction.totalAmount), true)
            if (detail.transaction.paymentMethod == "Cash") {
                DetailRow("Bayar", CurrencyFormatter.formatRupiah(detail.transaction.amountPaid))
                DetailRow(
                    "Kembali",
                    CurrencyFormatter.formatRupiah(detail.transaction.change),
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(
            value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Info, null, Modifier.size(64.dp), tint = Color.Gray.copy(0.4f))
        Spacer(Modifier.height(16.dp))
        Text("Tidak ada transaksi pada tanggal ini", color = Color.Gray)
    }
}

private fun formatDateLabel(dateMs: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(dateMs))

private fun formatTime(dateMs: Long): String =
    SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date(dateMs))
