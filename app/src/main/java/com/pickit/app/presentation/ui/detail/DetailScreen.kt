package com.pickit.app.presentation.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickit.app.presentation.component.SectionCard
import com.pickit.app.presentation.component.StatusChip
import com.pickit.app.presentation.component.TagChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    val product by viewModel.product.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.title ?: "商品详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        val item = product ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 24.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.title, style = MaterialTheme.typography.headlineMedium)
                    StatusChip(status = item.status)
                    Text(
                        item.currentPriceText ?: "待补充价格",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text("${item.platform.label} · ${item.shopName ?: "未识别店铺"}")
                }
            }
            item {
                SectionCard(title = "推荐摘要", modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.summary ?: "暂无摘要",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    Text(
                        text = item.recommendationReason ?: "暂无推荐理由",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }
            }
            item {
                SectionCard(title = "标签", modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item.tags.forEach { tag -> TagChip(label = tag.name) }
                    }
                }
            }
            item {
                SectionCard(title = "来源说明", modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.sourceNote ?: "暂无来源备注",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }
            }
            item {
                SectionCard(title = "价格历史", modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item.priceHistory.forEach { history ->
                            Column {
                                Text(
                                    text = history.priceText ?: "未知价格",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text("${history.platform.label} · ${history.recordedAt.take(10)}")
                            }
                        }
                    }
                }
            }
        }
    }
}
