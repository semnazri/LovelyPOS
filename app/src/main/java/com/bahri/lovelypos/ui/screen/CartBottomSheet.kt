package com.bahri.lovelypos.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.lovelypos.R
import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cartItems: List<CartItem>,
    totalAmount: Long,
    onIncreaseQty: (Long) -> Unit,
    onDecreaseQty: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onCheckout: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalQty = cartItems.sumOf { it.quantity }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                "Keranjang ($totalQty item)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        null,
                        Modifier.size(64.dp),
                        tint = Color.Gray.copy(0.4f)
                    )
                    Text("Keranjang Kosong", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(cartItems, key = { it.menuItem.id }) { item ->
                        CartItemRow(
                            item = item,
                            onInc = { onIncreaseQty(item.menuItem.id) },
                            onDec = { onDecreaseQty(item.menuItem.id) },
                            onRemove = { onRemoveItem(item.menuItem.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        CurrencyFormatter.formatRupiah(totalAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF008080)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008080)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proses Bayar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lanjut Belanja", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.menuItem.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2
            )
            Text(
                CurrencyFormatter.formatRupiah(item.menuItem.price),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Column(modifier = Modifier.weight(1f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = onDec,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.outline_remove_24),
                    null,
                    Modifier.size(14.dp),
                    tint = Color.Black
                )
            }

            Text(
                item.quantity.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.widthIn(min = 20.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = onInc,
                enabled = item.quantity < item.menuItem.stock,
                modifier = Modifier
                    .background(
                        if (item.quantity < item.menuItem.stock) Color(0xFF008080) else Color.Gray,
                        CircleShape
                    )
                    .size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
        }

        Text(
            CurrencyFormatter.formatRupiahWithoutDecimal(item.subtotal),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                null,
                tint = Color.Red.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
