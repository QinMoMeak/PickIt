package com.pickit.app.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickit.app.domain.model.Platform
import com.pickit.app.presentation.component.AppSearchBar
import com.pickit.app.presentation.component.EmptyState
import com.pickit.app.presentation.component.ProductCard
import com.pickit.app.presentation.component.TagChip

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenDetail: (String) -> Unit,
    onOpenAdd: () -> Unit,
    contentPadding: PaddingValues,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PickIt", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "发现好物，留给以后更快决策。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppSearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    placeholder = "搜索商品、平台、店铺或标签",
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TagChip(label = "全部") { viewModel.onPlatformSelected(null) }
                    }
                    items(Platform.entries.toList(), key = { it.name }) { platform ->
                        TagChip(label = platform.label) { viewModel.onPlatformSelected(platform) }
                    }
                }
                Text(
                    text = "共 ${state.products.size} 条收藏",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (state.products.isEmpty()) {
            item {
                EmptyState(
                    title = "还没有收藏",
                    description = "从一张商品截图开始，把想买的先存起来。",
                    actionLabel = "去新增",
                    onAction = onOpenAdd,
                )
            }
        } else {
            items(state.products, key = { it.id }) { product ->
                ProductCard(product = product, onClick = { onOpenDetail(product.id) })
            }
        }
    }
}
