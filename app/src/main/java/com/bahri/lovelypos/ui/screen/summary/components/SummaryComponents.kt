package com.bahri.lovelypos.ui.screen.summary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.lovelypos.domain.model.ItemSalesSummary
import com.bahri.lovelypos.domain.model.SummaryReport
import com.bahri.lovelypos.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateRangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    val ranges = listOf("Hari Ini", "7 Hari Terakhir", "30 Hari Terakhir", "Bulan Ini", "Custom")
    val selectedIndex = ranges.indexOf(selectedRange).coerceAtLeast(0)
    
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        ranges.forEach { range ->
            Tab(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                text = { Text(range) }
            )
        }
    }
}

@Composable
fun CustomDateRangeRow(
    startDate: Long?,
    endDate: Long?,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onStartClick, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (startDate != null) formatDate(startDate) else "Mulai")
        }
        OutlinedButton(onClick = onEndClick, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (endDate != null) formatDate(endDate) else "Selesai")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogBase(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let(onDateSelected)
            }) { Text("Pilih") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun SummaryContent(report: SummaryReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    title = "Pendapatan",
                    value = CurrencyFormatter.formatRupiah(report.totalRevenue),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Transaksi",
                    value = report.transactionCount.toString(),
                    modifier = Modifier.weight(0.7f)
                )
            }
        }

        item {
            Text("Metode Pembayaran", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    report.paymentBreakdown.forEachIndexed { index, payment ->
                        PaymentRow(payment.paymentMethod, payment.count, payment.total)
                        if (index < report.paymentBreakdown.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        item {
            Text("Item Terlaris", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (report.topItems.isEmpty()) {
                        Text("Belum ada data item", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
                    } else {
                        val maxQty = report.topItems.first().totalQty
                        report.topItems.take(5).forEachIndexed { index, item ->
                            TopItemRow(index + 1, item, maxQty)
                            if (index < 4 && index < report.topItems.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun TopItemRow(rank: Int, item: ItemSalesSummary, maxQty: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(28.dp)
                .background(
                    if (rank == 1) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                rank.toString(),
                fontWeight = FontWeight.Bold,
                color = if (rank == 1) Color.White else Color.Black,
                fontSize = 12.sp
            )
        }
        Column(Modifier.weight(1f)) {
            Text(item.menuItemName, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { if (maxQty > 0) item.totalQty.toFloat() / maxQty.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${item.totalQty} pcs", fontWeight = FontWeight.Bold)
            Text(CurrencyFormatter.formatRupiah(item.totalRevenue), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun PaymentRow(label: String, count: Int, total: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(label, fontWeight = FontWeight.Medium)
            Text("$count transaksi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(CurrencyFormatter.formatRupiah(total), fontWeight = FontWeight.Bold)
    }
}

private fun formatDate(ms: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(ms))
