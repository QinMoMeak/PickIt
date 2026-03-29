package com.pickit.app.presentation.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickit.app.domain.model.Platform
import com.pickit.app.presentation.component.TagChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedProductId) {
        state.savedProductId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("识别预览") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                Text("正在提取商品信息", modifier = Modifier.padding(horizontal = 24.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 24.dp),
        ) {
            item {
                val errorMessage = state.errorMessage
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = if ((state.draft.confidence ?: 0f) < 0.8f) {
                            "部分字段可信度较低，建议你检查后再保存。"
                        } else {
                            "确认一下这些信息，再保存到收藏库。"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = state.draft.title,
                    onValueChange = viewModel::updateTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("商品名") },
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.brand.orEmpty(),
                    onValueChange = viewModel::updateBrand,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("品牌") },
                )
            }
            item {
                Text("平台", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Platform.entries.toList(), key = { it.name }) { platform ->
                        TagChip(label = platform.label) { viewModel.updatePlatform(platform) }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = state.draft.shopName.orEmpty(),
                    onValueChange = viewModel::updateShop,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("店铺") },
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.priceText.orEmpty(),
                    onValueChange = viewModel::updatePrice,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("价格") },
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.summary.orEmpty(),
                    onValueChange = viewModel::updateSummary,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("推荐摘要") },
                )
            }
            item {
                OutlinedTextField(
                    value = state.draft.recommendationReason.orEmpty(),
                    onValueChange = viewModel::updateReason,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("推荐理由") },
                )
            }
            item {
                Text("标签", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.draft.tags, key = { it }) { tag ->
                        TagChip(label = tag)
                    }
                }
            }
            item {
                Button(onClick = viewModel::saveDraft, modifier = Modifier.fillMaxWidth()) {
                    Text("确认保存")
                }
            }
        }
    }
}
