package com.bahri.lovelypos.ui.screen.history.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.lovelypos.data.entity.Transaction
import com.bahri.lovelypos.data.entity.TransactionWithItems
import com.bahri.lovelypos.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SummaryBar(
    dateLabel: String,
    count: Int,
    total: Long,
    onDateClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .clickable { onDateClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dateLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("$count Transaksi", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                CurrencyFormatter.formatRupiah(total),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
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
    val grouped = if (showHeaders) {
        transactions.groupBy { formatDateLabel(it.createdAt) }
    } else {
        mapOf("" to transactions)
    }

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
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
    Surface(
        color = when (method) {
            "Cash" -> Color(0xFFE8F5E9)
            "QRIS" -> Color(0xFFE3F2FD)
            else -> Color(0xFFFFF3E0)
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = method,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when (method) {
                "Cash" -> Color(0xFF2E7D32)
                "QRIS" -> Color(0xFF1565C0)
                else -> Color(0xFFE65100)
            }
        )
    }
}

@Composable
fun TransactionDetailContent(
    detail: TransactionWithItems,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
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
                    formatDateFull(detail.transaction.createdAt),
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(item.menuItemName, fontWeight = FontWeight.Medium)
                        Text(
                            "${item.quantity} x ${CurrencyFormatter.formatRupiah(item.menuItemPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Text(
                        CurrencyFormatter.formatRupiah(item.subtotal),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider()

        if (detail.transaction.paymentMethod == "Cash") {
            DetailRow("Total Tagihan", CurrencyFormatter.formatRupiah(detail.transaction.totalAmount))
            DetailRow("Uang Tunai", CurrencyFormatter.formatRupiah(detail.transaction.amountPaid))
            DetailRow(
                "Kembalian",
                CurrencyFormatter.formatRupiah(detail.transaction.change),
                isBold = true,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(
                    CurrencyFormatter.formatRupiah(detail.transaction.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

private fun formatDateLabel(dateMs: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(dateMs))

private fun formatDateFull(dateMs: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(dateMs))

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
