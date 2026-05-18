// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.domain.model.ItemSalesSummary
import com.bahri.lovelypos.domain.model.SummaryReport
import com.bahri.lovelypos.ui.viewmodel.SummaryViewModel
import com.bahri.lovelypos.util.CurrencyFormatter
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(viewModel: SummaryViewModel = koinViewModel()) {
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val mode by viewModel.dateRangeMode.collectAsStateWithLifecycle()
    val customStart by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEnd by viewModel.customEndDate.collectAsStateWithLifecycle()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Ringkasan Penjualan", fontWeight = FontWeight.Bold) }) }) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            DateRangeSelector(mode) { viewModel.setDateRangeMode(it) }
            if (mode == "Custom") {
                CustomDateRangeRow(customStart, customEnd, { showStartPicker = true }, { showEndPicker = true })
            }

            AnimatedContent(
                targetState = summaryState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SummaryContent"
            ) { state ->
                when (state) {
                    is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Success -> {
                        val report = state.data
                        if (report == null || report.transactionCount == 0) EmptySummaryState()
                        else SummaryContent(report)
                    }
                    is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (showStartPicker) DatePickerDialogBase({ showStartPicker = false }) { viewModel.setCustomDateRange(it, customEnd); showStartPicker = false }
    if (showEndPicker) DatePickerDialogBase({ showEndPicker = false }) { viewModel.setCustomDateRange(customStart, it); showEndPicker = false }
}

@Composable
fun DateRangeSelector(selectedMode: String, onModeSelected: (String) -> Unit) {
    val modes = listOf("Hari ini", "Minggu ini", "Bulan ini", "Custom")
    ScrollableTabRow(selectedTabIndex = modes.indexOf(selectedMode), edgePadding = 16.dp, containerColor = MaterialTheme.colorScheme.surface, divider = {}) {
        modes.forEach { mode ->
            Tab(selected = selectedMode == mode, onClick = { onModeSelected(mode) }, text = { Text(mode) })
        }
    }
}

@Composable
fun CustomDateRangeRow(start: Long?, end: Long?, onStartClick: () -> Unit, onEndClick: () -> Unit) {
    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onStartClick, Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(8.dp))
            Text(if (start != null) formatDate(start) else "Mulai", fontSize = 12.sp)
        }
        OutlinedButton(onClick = onEndClick, Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null)
            Spacer(Modifier.width(8.dp))
            Text(if (end != null) formatDate(end) else "Selesai", fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogBase(onDismiss: () -> Unit, onDateSelected: (Long) -> Unit) {
    val state = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onDateSelected(it) } }) { Text("Pilih") } }) { DatePicker(state = state) }
}

@Composable
fun SummaryContent(report: SummaryReport) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Total Pendapatan", CurrencyFormatter.formatRupiah(report.totalRevenue), MaterialTheme.colorScheme.primaryContainer)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Transaksi", report.transactionCount.toString(), Modifier.weight(1f))
                    StatCard("Rata-rata", CurrencyFormatter.formatRupiah(if (report.transactionCount > 0) report.totalRevenue / report.transactionCount else 0), Modifier.weight(1f))
                }
            }
        }
        item {
            Text("Menu Terlaris", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val maxQty = report.topItems.maxOfOrNull { it.totalQty } ?: 1
                report.topItems.take(5).forEachIndexed { index, item -> TopItemRow(index + 1, item, maxQty) }
            }
        }
        item {
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                report.paymentBreakdown.forEach { PaymentRow(it.paymentMethod, it.count, it.total) }
            }
        }
        if (report.dailyRevenue.size > 1) {
            item {
                Text("Laporan Harian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Card(Modifier.padding(top = 12.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        report.dailyRevenue.forEach { daily ->
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(daily.dateLabel, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("${daily.count} trx", fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(60.dp))
                                Text(CurrencyFormatter.formatRupiah(daily.total), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, containerColor: Color) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(Modifier.padding(20.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun TopItemRow(rank: Int, item: ItemSalesSummary, maxQty: Int) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape, modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(rank.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.width(12.dp))
            Text(item.menuItemName, Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text("${item.totalQty} terjual", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(36.dp))
            Box(Modifier.weight(1f).height(10.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Box(Modifier.fillMaxWidth(item.totalQty.toFloat() / maxQty).fillMaxHeight().background(MaterialTheme.colorScheme.primary, CircleShape))
            }
            Spacer(Modifier.width(12.dp))
            Text(CurrencyFormatter.formatRupiah(item.totalRevenue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PaymentRow(method: String, count: Int, total: Long) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column { Text(method, fontWeight = FontWeight.SemiBold); Text("$count Transaksi", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
        Text(CurrencyFormatter.formatRupiah(total), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptySummaryState() {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Info, null, Modifier.size(80.dp), tint = Color.Gray.copy(0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Tidak ada data untuk periode ini", textAlign = TextAlign.Center, color = Color.Gray)
    }
}

private fun formatDate(ms: Long): String = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(ms))
