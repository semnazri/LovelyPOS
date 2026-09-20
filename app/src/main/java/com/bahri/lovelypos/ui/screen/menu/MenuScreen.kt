package com.bahri.lovelypos.ui.screen.menu

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.ui.components.common.CategoryFilter
import com.bahri.lovelypos.ui.components.common.LovelyEmptyState
import com.bahri.lovelypos.ui.screen.menu.components.MenuFormBottomSheet
import com.bahri.lovelypos.ui.screen.menu.components.MenuItemCard
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
import com.bahri.lovelypos.ui.viewmodel.MenuViewModel
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MenuScreen(
    viewModel: MenuViewModel = koinViewModel()
) {
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val categoryList by viewModel.categoryList.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    MenuScreenContent(
        menuState = menuState,
        filteredItems = filteredItems,
        categoryList = categoryList,
        selectedCategory = selectedCategory,
        onCategorySelected = { viewModel.setCategory(it) },
        onSaveItem = { viewModel.saveItem(it) },
        onDeleteItem = { viewModel.deleteItem(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreenContent(
    menuState: UiState<List<MenuItem>>,
    filteredItems: List<MenuItem>,
    categoryList: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onSaveItem: (MenuItem) -> Unit,
    onDeleteItem: (MenuItem) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MenuItem?>(null) }

    val configuration = LocalConfiguration.current
    val columns = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 4 else 2

    LovelyPOSTheme(
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
                onCategorySelected = onCategorySelected
            )

            AnimatedContent(
                targetState = menuState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MenuContent"
            ) { state ->
                when (state) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Success -> {
                        if (state.data.isEmpty()) {
                            LovelyEmptyState(
                                icon = Icons.Default.Menu,
                                message = "Belum ada menu. Tambah menu pertama!",
                                action = {
                                    Button(onClick = { itemToEdit = null; showBottomSheet = true }) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Tambah Menu")
                                    }
                                }
                            )
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
                                        onToggleAvailable = {
                                            onSaveItem(item.copy(isAvailable = it))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
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
            onSave = { onSaveItem(it); showBottomSheet = false }
        )
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus Menu?") },
            text = { Text("Menu ini akan dihapus permanen. Data riwayat transaksi tidak terpengaruh.") },
            confirmButton = {
                Button(
                    onClick = { onDeleteItem(showDeleteDialog!!); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Batal") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    val dummyItems = listOf(
        MenuItem(1, "Kopi Latte", "Minuman", 25000, 10, true),
        MenuItem(2, "Espresso", "Minuman", 20000, 5, true),
        MenuItem(3, "Croissant", "Makanan", 15000, 3, true),
        MenuItem(4, "Red Velvet", "Makanan", 30000, 0, false)
    )
    LovelyPOSTheme(useScaffold = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MenuScreenContent(
                menuState = UiState.Success(dummyItems),
                filteredItems = dummyItems,
                categoryList = listOf("Semua", "Makanan", "Minuman"),
                selectedCategory = "Semua",
                onCategorySelected = {},
                onSaveItem = {},
                onDeleteItem = {}
            )
        }
    }
}
