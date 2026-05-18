// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.R
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.ui.viewmodel.POSViewModel
import com.bahri.lovelypos.util.CurrencyFormatter
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(viewModel: POSViewModel = koinViewModel()) {
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val categoryList by viewModel.categoryList.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val itemCount by viewModel.itemCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPaymentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.paymentSuccess.collect {
            if (it) {
                showPaymentSheet = false; snackbarHostState.showSnackbar("Transaksi berhasil!")
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("Kasir", fontWeight = FontWeight.Bold) }) }
    ) { paddingValues ->
        AnimatedContent(
            targetState = isLandscape,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "POSLayout",
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) { landscape ->
            if (landscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(0.6f)) {
                        MenuSection(
                            menuState,
                            filteredItems,
                            categoryList,
                            selectedCategory,
                            cart,
                            { viewModel.setCategory(it) },
                            { viewModel.addToCart(it) },
                            3
                        )
                    }
                    VerticalDivider()
                    Box(modifier = Modifier.weight(0.4f)) {
                        CartSection(
                            cart,
                            totalAmount,
                            itemCount,
                            { viewModel.increaseQty(it) },
                            { viewModel.decreaseQty(it) },
                            { showPaymentSheet = true })
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(0.55f)) {
                        MenuSection(
                            menuState,
                            filteredItems,
                            categoryList,
                            selectedCategory,
                            cart,
                            { viewModel.setCategory(it) },
                            { viewModel.addToCart(it) },
                            2
                        )
                    }
                    HorizontalDivider()
                    Box(modifier = Modifier.weight(0.45f)) {
                        CartSection(
                            cart,
                            totalAmount,
                            itemCount,
                            { viewModel.increaseQty(it) },
                            { viewModel.decreaseQty(it) },
                            { showPaymentSheet = true })
                    }
                }
            }
        }
    }

    if (showPaymentSheet) {
        PaymentBottomSheet(viewModel, onDismiss = { showPaymentSheet = false })
    }
}

@Composable
fun MenuSection(
    state: UiState<List<MenuItem>>,
    filteredItems: List<MenuItem>,
    categories: List<String>,
    selectedCategory: String,
    cart: List<CartItem>,
    onCategorySelected: (String) -> Unit,
    onItemClick: (MenuItem) -> Unit,
    columns: Int
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = cat == selectedCategory,
                    onClick = { onCategorySelected(cat) },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        when (state) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { Text("Belum ada menu", color = Color.Gray) }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val cartItem = cart.find { it.menuItem.id == item.id }
                            POSMenuItemCard(
                                item,
                                cartItem?.quantity ?: 0,
                                onClick = { onItemClick(item) })
                        }
                    }
                }
            }

            is UiState.Error -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text(state.message, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun POSMenuItemCard(item: MenuItem, quantityInCart: Int, onClick: () -> Unit) {
    val isEnabled = item.stock > 0 && item.isAvailable
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp)
            .clickable(enabled = isEnabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                Text(
                    CurrencyFormatter.formatRupiah(item.price),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!isEnabled) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (!item.isAvailable) "Nonaktif" else "Habis",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.background(
                            if (item.isAvailable) MaterialTheme.colorScheme.error else Color.Gray,
                            RoundedCornerShape(4.dp)
                        ).padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (quantityInCart > 0 && isEnabled) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        quantityInCart.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CartSection(
    cart: List<CartItem>,
    total: Long,
    count: Int,
    onInc: (Long) -> Unit,
    onDec: (Long) -> Unit,
    onPay: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Keranjang ($count)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (cart.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart, key = { it.menuItem.id }) { item ->
                    CartItemRow(item, { onInc(item.menuItem.id) }, { onDec(item.menuItem.id) })
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                CurrencyFormatter.formatRupiah(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = cart.isNotEmpty(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Proses Bayar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onInc: () -> Unit, onDec: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(item.menuItem.name, fontWeight = FontWeight.SemiBold)
            Text(
                CurrencyFormatter.formatRupiah(item.menuItem.price),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        Row(
            Modifier
                .weight(1f)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            IconButton(
                onClick = onDec,
                modifier = Modifier.size(16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                if (item.quantity > 1) {
                    Icon(
                        painterResource(R.drawable.outline_remove_24),
                        null,
                        Modifier.size(16.dp),
                        tint = Color.Red
                    )
                } else {
                    Icon(Icons.Default.Delete, null, Modifier.size(16.dp), tint = Color.Red)
                }
            }
            Text(
                item.quantity.toString(),
                fontWeight = FontWeight.Bold,

            )
            IconButton(
                onClick = onInc,
                enabled = item.quantity < item.menuItem.stock,
                modifier = Modifier.size(16.dp)
                    .background(
                        if (item.quantity < item.menuItem.stock) MaterialTheme.colorScheme.primary else Color.Gray,
                        CircleShape
                    )
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(32.dp), tint = Color.White)
            }
        }
        Text(
            CurrencyFormatter.formatRupiahWithoutDecimal(item.subtotal),
            Modifier
                .weight(1f)
                .padding(start = 12.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(viewModel: POSViewModel, onDismiss: () -> Unit) {
    val total by viewModel.totalAmount.collectAsStateWithLifecycle()
    val method by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val paidInput by viewModel.amountPaidInput.collectAsStateWithLifecycle()
    val change by viewModel.changeAmount.collectAsStateWithLifecycle()
    val canPay by viewModel.canConfirmPayment.collectAsStateWithLifecycle()

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
                        CurrencyFormatter.formatRupiah(total),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cash", "Transfer", "QRIS").forEach { m ->
                    FilterChip(
                        selected = method == m,
                        onClick = { viewModel.setPaymentMethod(m) },
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
            if (method == "Cash") {
                OutlinedTextField(
                    value = paidInput,
                    onValueChange = { viewModel.setAmountPaid(it) },
                    label = { Text("Uang Diterima") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                if (canPay) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kembalian")
                        Text(
                            CurrencyFormatter.formatRupiah(change),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Button(
                onClick = { viewModel.processPayment() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = canPay,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Konfirmasi Pembayaran")
            }
        }
    }
}
