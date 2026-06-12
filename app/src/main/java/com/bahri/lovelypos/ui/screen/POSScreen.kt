package com.bahri.lovelypos.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
import com.bahri.lovelypos.ui.viewmodel.POSViewModel
import com.bahri.lovelypos.util.CurrencyFormatter
import com.bahri.lovelypos.util.UiState
import kotlinx.coroutines.launch
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

    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val amountPaidInput by viewModel.amountPaidInput.collectAsStateWithLifecycle()
    val changeAmount by viewModel.changeAmount.collectAsStateWithLifecycle()
    val canConfirmPayment by viewModel.canConfirmPayment.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.paymentSuccess.collect {
            if (it) {
                snackbarHostState.showSnackbar("Transaksi berhasil!")
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { snackbarHostState.showSnackbar(it) }
    }

    POSScreenContent(
        menuState = menuState,
        filteredItems = filteredItems,
        cart = cart,
        categoryList = categoryList,
        selectedCategory = selectedCategory,
        totalAmount = totalAmount,
        itemCount = itemCount,
        paymentMethod = paymentMethod,
        amountPaidInput = amountPaidInput,
        changeAmount = changeAmount,
        canConfirmPayment = canConfirmPayment,
        snackbarHostState = snackbarHostState,
        onCategorySelected = { viewModel.setCategory(it) },
        onItemClick = { viewModel.addToCart(it) },
        onIncreaseQty = { viewModel.increaseQty(it) },
        onDecreaseQty = { viewModel.decreaseQty(it) },
        onRemoveItem = { viewModel.removeFromCart(it) },
        onSetPaymentMethod = { viewModel.setPaymentMethod(it) },
        onSetAmountPaid = { viewModel.setAmountPaid(it) },
        onProcessPayment = { viewModel.processPayment() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreenContent(
    menuState: UiState<List<MenuItem>>,
    filteredItems: List<MenuItem>,
    cart: List<CartItem>,
    categoryList: List<String>,
    selectedCategory: String,
    totalAmount: Long,
    itemCount: Int,
    paymentMethod: String,
    amountPaidInput: String,
    changeAmount: Long,
    canConfirmPayment: Boolean,
    snackbarHostState: SnackbarHostState,
    onCategorySelected: (String) -> Unit,
    onItemClick: (MenuItem) -> Unit,
    onIncreaseQty: (Long) -> Unit,
    onDecreaseQty: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onSetPaymentMethod: (String) -> Unit,
    onSetAmountPaid: (String) -> Unit,
    onProcessPayment: () -> Unit
) {
    var isPaymentSheetVisible by remember { mutableStateOf(false) }
    var isCartVisible by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }

    val badgeScale = remember { Animatable(1f) }
    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            badgeScale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            badgeScale.animateTo(1f)
        }
    }

    LovelyPOSTheme(useScaffold = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { isCartVisible = true }) {
                            BadgedBox(
                                badge = {
                                    if (itemCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFF008080),
                                            contentColor = Color.White,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = badgeScale.value
                                                scaleY = badgeScale.value
                                            }
                                        ) {
                                            Text(itemCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (cart.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { isCartVisible = true },
                        containerColor = Color(0xFF008080),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            "🛒 $itemCount item · ${CurrencyFormatter.formatRupiah(totalAmount)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                MenuSection(
                    state = menuState,
                    filteredItems = filteredItems,
                    categories = categoryList,
                    selectedCategory = selectedCategory,
                    cart = cart,
                    isGridView = isGridView,
                    onToggleView = { isGridView = !isGridView },
                    onCategorySelected = onCategorySelected,
                    onItemClick = onItemClick
                )
            }
        }
    }

    if (isCartVisible) {
        CartBottomSheet(
            cartItems = cart,
            totalAmount = totalAmount,
            onIncreaseQty = onIncreaseQty,
            onDecreaseQty = onDecreaseQty,
            onRemoveItem = onRemoveItem,
            onCheckout = {
                isCartVisible = false
                isPaymentSheetVisible = true
            },
            onDismiss = { isCartVisible = false }
        )
    }

    if (isPaymentSheetVisible) {
        PaymentBottomSheet(
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            amountPaidInput = amountPaidInput,
            changeAmount = changeAmount,
            canConfirmPayment = canConfirmPayment,
            onSetPaymentMethod = onSetPaymentMethod,
            onSetAmountPaid = onSetAmountPaid,
            onConfirmPayment = {
                onProcessPayment()
                isPaymentSheetVisible = false
            },
            onDismiss = { isPaymentSheetVisible = false }
        )
    }
}

@Composable
fun MenuSection(
    state: UiState<List<MenuItem>>,
    filteredItems: List<MenuItem>,
    categories: List<String>,
    selectedCategory: String,
    cart: List<CartItem>,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onItemClick: (MenuItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
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
            IconButton(onClick = onToggleView) {
                Icon(
                    if (isGridView) Icons.Default.List else Icons.Default.Menu,
                    contentDescription = "Toggle View"
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
                    val firstUnavailableIndex =
                        filteredItems.indexOfFirst { !it.isAvailable || it.stock == 0 }

                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (firstUnavailableIndex == -1) {
                                items(filteredItems, key = { it.id }) { item ->
                                    val cartItem = cart.find { it.menuItem.id == item.id }
                                    POSMenuItemCard(
                                        item,
                                        cartItem?.quantity ?: 0,
                                        onClick = { onItemClick(item) })
                                }
                            } else {
                                val available = filteredItems.subList(0, firstUnavailableIndex)
                                val unavailable =
                                    filteredItems.subList(firstUnavailableIndex, filteredItems.size)

                                items(available, key = { it.id }) { item ->
                                    val cartItem = cart.find { it.menuItem.id == item.id }
                                    POSMenuItemCard(
                                        item,
                                        cartItem?.quantity ?: 0,
                                        onClick = { onItemClick(item) })
                                }

                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        "Tidak Tersedia",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(unavailable, key = { it.id }) { item ->
                                    val cartItem = cart.find { it.menuItem.id == item.id }
                                    POSMenuItemCard(
                                        item,
                                        cartItem?.quantity ?: 0,
                                        onClick = { onItemClick(item) })
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (firstUnavailableIndex == -1) {
                                items(filteredItems, key = { it.id }) { item ->
                                    val cartItem = cart.find { it.menuItem.id == item.id }
                                    POSMenuItemRow(
                                        item,
                                        cartItem?.quantity ?: 0,
                                        onClick = { onItemClick(item) })
                                }
                            } else {
                                val available = filteredItems.subList(0, firstUnavailableIndex)
                                val unavailable =
                                    filteredItems.subList(firstUnavailableIndex, filteredItems.size)

                                items(available, key = { it.id }) { item ->
                                    val cartItem = cart.find { it.menuItem.id == item.id }
                                    POSMenuItemRow(
                                        item,
                                        cartItem?.quantity ?: 0,
                                        onClick = { onItemClick(item) })
                                }

                                item {
                                    Text(
                                        "Tidak Tersedia",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(unavailable, key = { it.id }) { item ->
                                    val cartItem = cart.find { it.menuItem.id == item.id }
                                    POSMenuItemRow(
                                        item,
                                        cartItem?.quantity ?: 0,
                                        onClick = { onItemClick(item) })
                                }
                            }
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
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clickable(enabled = isEnabled) {
                scope.launch {
                    scale.animateTo(0.95f, spring(stiffness = Spring.StiffnessHigh))
                    scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                onClick()
            },
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
fun POSMenuItemRow(item: MenuItem, quantityInCart: Int, onClick: () -> Unit) {
    val isEnabled = item.stock > 0 && item.isAvailable
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Surface(
        onClick = {
            scope.launch {
                scale.animateTo(0.95f, spring(stiffness = Spring.StiffnessHigh))
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
            onClick()
        },
        enabled = isEnabled,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isEnabled) 1f else 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (!isEnabled) {
                    Text(
                        if (!item.isAvailable) "Nonaktif" else "Stok Habis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (quantityInCart > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            quantityInCart.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                CurrencyFormatter.formatRupiah(item.price),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp
            )
        }
    }
}


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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun POSScreenPreview() {
    val dummyMenuItems = listOf(
        MenuItem(1, "Kopi Latte", "Minuman", 25000, 10, true),
        MenuItem(2, "Espresso", "Minuman", 20000, 5, true),
        MenuItem(3, "Croissant", "Makanan", 15000, 3, true),
        MenuItem(4, "Red Velvet", "Makanan", 30000, 0, true)
    )
    val dummyCart = listOf(
        CartItem(dummyMenuItems[0], 1),
        CartItem(dummyMenuItems[2], 2)
    )

    LovelyPOSTheme(useScaffold = false) {
        POSScreenContent(
            menuState = UiState.Success(dummyMenuItems),
            filteredItems = dummyMenuItems,
            cart = dummyCart,
            categoryList = listOf("Semua", "Makanan", "Minuman"),
            selectedCategory = "Semua",
            totalAmount = 55000,
            itemCount = 3,
            paymentMethod = "Cash",
            amountPaidInput = "100000",
            changeAmount = 45000,
            canConfirmPayment = true,
            snackbarHostState = remember { SnackbarHostState() },
            onCategorySelected = {},
            onItemClick = {},
            onIncreaseQty = {},
            onDecreaseQty = {},
            onRemoveItem = {},
            onSetPaymentMethod = {},
            onSetAmountPaid = {},
            onProcessPayment = {}
        )
    }
}
