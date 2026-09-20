package com.bahri.lovelypos.ui.screen.pos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.ui.screen.pos.components.CartBottomSheet
import com.bahri.lovelypos.ui.screen.pos.components.MenuSection
import com.bahri.lovelypos.ui.screen.pos.components.PaymentBottomSheet
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 12, 0, 0)
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                MenuSection(
                    state = menuState,
                    filteredItems = filteredItems,
                    categories = categoryList,
                    selectedCategory = selectedCategory,
                    cart = cart,
                    itemCount = itemCount,
                    badgeScale = badgeScale.value,
                    isGridView = isGridView,
                    onToggleView = { isGridView = !isGridView },
                    onCategorySelected = onCategorySelected,
                    onItemClick = onItemClick,
                    onCartClick = { isCartVisible = true }
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

@Preview(showBackground = true)
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
        Surface(color = MaterialTheme.colorScheme.background) {
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
}
