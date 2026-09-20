package com.bahri.lovelypos.ui.screen.menu.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bahri.lovelypos.data.entity.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuFormBottomSheet(
    item: MenuItem?,
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var priceStr by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(item?.stock?.toString() ?: "0") }
    var isAvailable by remember { mutableStateOf(item?.isAvailable ?: true) }

    var nameError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (item == null) "Tambah Menu Baru" else "Edit Menu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = it.isBlank() },
                label = { Text("Nama Menu") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = { if (nameError) Text("Nama tidak boleh kosong") }
            )

            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it; categoryError = it.isBlank() },
                    label = { Text("Kategori") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = categoryError,
                    supportingText = { if (categoryError) Text("Kategori tidak boleh kosong") }
                )
                if (existingCategories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(existingCategories.filter {
                            it.contains(
                                category,
                                true
                            ) || category.isEmpty()
                        }) { cat ->
                            SuggestionChip(
                                onClick = { category = cat; categoryError = false },
                                label = { Text(cat) })
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            priceStr = it; priceError = (it.toLongOrNull() ?: 0L) <= 0L
                        }
                    },
                    label = { Text("Harga") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError,
                    supportingText = { if (priceError) Text("Harga > 0") }
                )
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stockStr = it },
                    label = { Text("Stok") },
                    modifier = Modifier.weight(0.7f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ketersediaan", modifier = Modifier.weight(1f))
                Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && category.isNotBlank() && (priceStr.toLongOrNull()
                            ?: 0L) > 0L
                    ) {
                        onSave(
                            MenuItem(
                                item?.id ?: 0L,
                                name,
                                category,
                                priceStr.toLongOrNull() ?: 0L,
                                stockStr.toIntOrNull() ?: 0,
                                isAvailable
                            )
                        )
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
