package com.bahri.lovelypos.ui.screen.pos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.domain.model.CartItem
import com.bahri.lovelypos.ui.components.common.CategoryFilter
import com.bahri.lovelypos.ui.components.common.LovelyEmptyState
import com.bahri.lovelypos.util.UiState

@Composable
fun MenuSection(
    state: UiState<List<MenuItem>>,
    filteredItems: List<MenuItem>,
    categories: List<String>,
    selectedCategory: String,
    cart: List<CartItem>,
    itemCount: Int,
    badgeScale: Float,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onItemClick: (MenuItem) -> Unit,
    onCartClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryFilter(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleView) {
                Icon(
                    if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.Menu,
                    contentDescription = "Toggle View"
                )
            }
            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        if (itemCount > 0) {
                            Badge(
                                containerColor = Color(0xFF008080),
                                contentColor = Color.White,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = badgeScale
                                    scaleY = badgeScale
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

        when (state) {
            is UiState.Loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    LovelyEmptyState(
                        icon = Icons.Default.Menu,
                        message = "Belum ada menu"
                    )
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
