// MODIFIED — Milestone 6
package com.bahri.lovelypos.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.ui.viewmodel.MenuViewModel
import com.bahri.lovelypos.util.CurrencyFormatter
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel = koinViewModel()
) {
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val categoryList by viewModel.categoryList.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MenuItem?>(null) }

    val configuration = LocalConfiguration.current
    val columns = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kelola Menu", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { itemToEdit = null; showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Tambah Menu") }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            CategoryFilter(
                categories = categoryList,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) }
            )

            AnimatedContent(
                targetState = menuState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MenuContent"
            ) { state ->
                when (state) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            EmptyMenuState(onAddClick = { itemToEdit = null; showBottomSheet = true })
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredItems, key = { it.id }) { item ->
                                    MenuItemCard(
                                        item = item,
                                        onEdit = { itemToEdit = item; showBottomSheet = true },
                                        onDelete = { showDeleteDialog = item },
                                        onToggleAvailable = { viewModel.saveItem(item.copy(isAvailable = it)) }
                                    )
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        MenuFormBottomSheet(
            item = itemToEdit,
            existingCategories = categoryList.filter { it != "Semua" },
            onDismiss = { showBottomSheet = false },
            onSave = { viewModel.saveItem(it); showBottomSheet = false }
        )
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Menu?") },
            text = { Text("Menu ini akan dihapus permanen. Data riwayat transaksi tidak terpengaruh.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteItem(showDeleteDialog!!); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun CategoryFilter(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuItemCard(item: MenuItem, onEdit: () -> Unit, onDelete: () -> Unit, onToggleAvailable: (Boolean) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (item.isAvailable) 1f else 0.6f)
            .combinedClickable(onClick = onEdit, onLongClick = { showMenu = true }),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(item.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(CurrencyFormatter.formatRupiah(item.price), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.stock == 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) { Text("Habis", color = Color.White) }
                    } else {
                        Text("Stok: ${item.stock}", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Switch(checked = item.isAvailable, onCheckedChange = onToggleAvailable, modifier = Modifier.scale(0.7f))
                }
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                DropdownMenuItem(text = { Text("Hapus", color = Color.Red) }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFormBottomSheet(item: MenuItem?, existingCategories: List<String>, onDismiss: () -> Unit, onSave: (MenuItem) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var priceStr by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(item?.stock?.toString() ?: "0") }
    var isAvailable by remember { mutableStateOf(item?.isAvailable ?: true) }

    var nameError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (item == null) "Tambah Menu Baru" else "Edit Menu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = it.isBlank() },
                label = { Text("Nama Menu") }, modifier = Modifier.fillMaxWidth(),
                isError = nameError, supportingText = { if (nameError) Text("Nama tidak boleh kosong") }
            )

            Column {
                OutlinedTextField(
                    value = category, onValueChange = { category = it; categoryError = it.isBlank() },
                    label = { Text("Kategori") }, modifier = Modifier.fillMaxWidth(),
                    isError = categoryError, supportingText = { if (categoryError) Text("Kategori tidak boleh kosong") }
                )
                if (existingCategories.isNotEmpty()) {
                    LazyRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(existingCategories.filter { it.contains(category, true) || category.isEmpty() }) { cat ->
                            SuggestionChip(onClick = { category = cat; categoryError = false }, label = { Text(cat) })
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = priceStr, onValueChange = { if (it.all { c -> c.isDigit() }) { priceStr = it; priceError = (it.toLongOrNull() ?: 0L) <= 0L } },
                    label = { Text("Harga") }, modifier = Modifier.weight(1f),
                    prefix = { Text("Rp ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError, supportingText = { if (priceError) Text("Harga > 0") }
                )
                OutlinedTextField(
                    value = stockStr, onValueChange = { if (it.all { c -> c.isDigit() }) stockStr = it },
                    label = { Text("Stok") }, modifier = Modifier.weight(0.7f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ketersediaan", modifier = Modifier.weight(1f))
                Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && category.isNotBlank() && (priceStr.toLongOrNull() ?: 0L) > 0L) {
                        onSave(MenuItem(item?.id ?: 0L, name, category, priceStr.toLongOrNull() ?: 0L, stockStr.toIntOrNull() ?: 0, isAvailable))
                    } else {
                        nameError = name.isBlank()
                        categoryError = category.isBlank()
                        priceError = (priceStr.toLongOrNull() ?: 0L) <= 0L
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Simpan Menu") }
        }
    }
}

@Composable
fun EmptyMenuState(onAddClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Menu, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text("Belum ada menu. Tambah menu pertama!", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddClick) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Tambah Menu") }
    }
}
