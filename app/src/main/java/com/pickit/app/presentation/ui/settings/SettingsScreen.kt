package com.pickit.app.presentation.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pickit.app.presentation.component.SectionCard

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    contentPadding: PaddingValues,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        viewModel.exportToUri(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        viewModel.importFromUri(uri)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("设置", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            SectionCard(title = "AI Provider 配置", modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.aiProvider,
                        onValueChange = viewModel::onAiProviderChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_PROVIDER") },
                        placeholder = { Text("zhipu") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.apiBaseUrl,
                        onValueChange = viewModel::onApiBaseUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_BASE_URL") },
                        placeholder = { Text("https://open.bigmodel.cn/api/paas/v4") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.aiApiKey,
                        onValueChange = viewModel::onAiApiKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_API_KEY") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = state.aiModel,
                        onValueChange = viewModel::onAiModelChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_MODEL") },
                        placeholder = { Text("glm-4.6v-flash") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.aiTimeoutSeconds,
                        onValueChange = viewModel::onAiTimeoutSecondsChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_TIMEOUT_SECONDS") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.aiMaxTokens,
                        onValueChange = viewModel::onAiMaxTokensChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_MAX_TOKENS") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.aiTemperature,
                        onValueChange = viewModel::onAiTemperatureChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("AI_TEMPERATURE") },
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI_ENABLE_THINKING")
                            Text(
                                text = if (state.aiEnableThinking) "enabled" else "disabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.aiEnableThinking,
                            onCheckedChange = viewModel::onAiEnableThinkingChange,
                        )
                    }
                    Button(
                        onClick = viewModel::saveAiConfig,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存 AI 配置")
                    }
                    Text(
                        "当前默认模型为智谱 glm-4.6v-flash；后续切换服务商时只需要改 provider 配置和适配器。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        item {
            SectionCard(title = "本地导入导出", modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { exportLauncher.launch("pickit-backup.json") },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("导出 JSON 备份")
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("导入 JSON 备份")
                    }
                    Text(
                        "导入会覆盖当前本地数据，请先确认备份文件来源。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        item {
            SectionCard(title = "WebDAV 备份", modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.webDavPath,
                        onValueChange = viewModel::onWebDavPathChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("WebDAV 路径") },
                        placeholder = { Text("/goodstash/") },
                        singleLine = true,
                    )
                    Button(
                        onClick = viewModel::saveWebDavPath,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存 WebDAV 路径")
                    }
                    Text("最近操作：${state.lastAction}")
                    Button(
                        onClick = viewModel::backupNow,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("立即备份到 WebDAV")
                    }
                    Button(
                        onClick = viewModel::restoreNow,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("从 WebDAV 恢复")
                    }
                    Text(
                        "默认会把备份写到“AI_BASE_URL + WebDAV 路径 + pickit-backup.json”，请确认目录已存在。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
