package com.bahri.lovelypos.ui.screen.pos.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bahri.lovelypos.data.entity.MenuItem
import com.bahri.lovelypos.util.CurrencyFormatter
import kotlinx.coroutines.launch

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
