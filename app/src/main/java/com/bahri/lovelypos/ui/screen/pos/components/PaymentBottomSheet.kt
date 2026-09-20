package com.bahri.lovelypos.ui.screen.pos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.lovelypos.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    totalAmount: Long,
    paymentMethod: String,
    amountPaidInput: String,
    changeAmount: Long,
    canConfirmPayment: Boolean,
    onSetPaymentMethod: (String) -> Unit,
    onSetAmountPaid: (String) -> Unit,
    onConfirmPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Pembayaran",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Tagihan", fontWeight = FontWeight.Bold)
                    Text(
                        CurrencyFormatter.formatRupiah(totalAmount),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "Transfer", "QRIS").forEach { m ->
                    FilterChip(
                        selected = paymentMethod == m,
                        onClick = { onSetPaymentMethod(m) },
                        label = {
                            Text(
                                m,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (paymentMethod == "Cash") {
                OutlinedTextField(
                    value = amountPaidInput,
                    onValueChange = onSetAmountPaid,
                    label = { Text("Uang Diterima") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (canConfirmPayment) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian")
                        Text(
                            CurrencyFormatter.formatRupiah(changeAmount),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Button(
                onClick = onConfirmPayment,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = canConfirmPayment,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Konfirmasi Pembayaran")
            }
        }
    }
}
