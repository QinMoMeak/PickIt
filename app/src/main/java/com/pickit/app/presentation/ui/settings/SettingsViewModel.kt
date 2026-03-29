package com.pickit.app.presentation.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickit.app.data.preferences.SettingsPreferencesDataSource
import com.pickit.app.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val aiProvider: String = "",
    val apiBaseUrl: String = "",
    val aiApiKey: String = "",
    val aiModel: String = "",
    val aiTimeoutSeconds: String = "",
    val aiEnableThinking: Boolean = false,
    val aiMaxTokens: String = "",
    val aiTemperature: String = "",
    val webDavPath: String = "",
    val lastAction: String = "尚未执行同步操作",
    val isBusy: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPreferencesDataSource: SettingsPreferencesDataSource,
    private val syncRepository: SyncRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsPreferencesDataSource.settingsFlow.collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        aiProvider = settings.aiProvider,
                        apiBaseUrl = settings.apiBaseUrl,
                        aiApiKey = settings.aiApiKey,
                        aiModel = settings.aiModel,
                        aiTimeoutSeconds = settings.aiTimeoutSeconds.toString(),
                        aiEnableThinking = settings.aiEnableThinking,
                        aiMaxTokens = settings.aiMaxTokens.toString(),
                        aiTemperature = settings.aiTemperature.toString(),
                        webDavPath = settings.webDavPath,
                    )
                }
            }
        }
    }

    fun onAiProviderChange(value: String) {
        _uiState.update { it.copy(aiProvider = value) }
    }

    fun onApiBaseUrlChange(value: String) {
        _uiState.update { it.copy(apiBaseUrl = value) }
    }

    fun onAiApiKeyChange(value: String) {
        _uiState.update { it.copy(aiApiKey = value) }
    }

    fun onAiModelChange(value: String) {
        _uiState.update { it.copy(aiModel = value) }
    }

    fun onAiTimeoutSecondsChange(value: String) {
        _uiState.update { it.copy(aiTimeoutSeconds = value) }
    }

    fun onAiEnableThinkingChange(value: Boolean) {
        _uiState.update { it.copy(aiEnableThinking = value) }
    }

    fun onAiMaxTokensChange(value: String) {
        _uiState.update { it.copy(aiMaxTokens = value) }
    }

    fun onAiTemperatureChange(value: String) {
        _uiState.update { it.copy(aiTemperature = value) }
    }

    fun onWebDavPathChange(value: String) {
        _uiState.update { it.copy(webDavPath = value) }
    }

    fun saveAiConfig() {
        val state = uiState.value
        viewModelScope.launch {
            settingsPreferencesDataSource.updateAiProvider(state.aiProvider)
            settingsPreferencesDataSource.updateApiBaseUrl(state.apiBaseUrl)
            settingsPreferencesDataSource.updateAiApiKey(state.aiApiKey)
            settingsPreferencesDataSource.updateAiModel(state.aiModel)
            settingsPreferencesDataSource.updateAiTimeoutSeconds(state.aiTimeoutSeconds.toIntOrNull() ?: 60)
            settingsPreferencesDataSource.updateAiEnableThinking(state.aiEnableThinking)
            settingsPreferencesDataSource.updateAiMaxTokens(state.aiMaxTokens.toIntOrNull() ?: 1024)
            settingsPreferencesDataSource.updateAiTemperature(state.aiTemperature.toDoubleOrNull() ?: 0.1)
            _uiState.update { it.copy(lastAction = "已保存 AI Provider 配置") }
        }
    }

    fun saveWebDavPath() {
        val value = uiState.value.webDavPath
        viewModelScope.launch {
            settingsPreferencesDataSource.updateWebDavPath(value)
            _uiState.update { it.copy(lastAction = "已保存 WebDAV 路径") }
        }
    }

    fun exportToUri(uri: Uri?) {
        executeAction(
            successMessage = "已导出本地备份文件",
            action = { syncRepository.exportJson(uri) },
        )
    }

    fun importFromUri(uri: Uri?) {
        executeAction(
            successMessage = "已从本地文件恢复数据",
            action = { syncRepository.importJson(uri) },
        )
    }

    fun backupNow() {
        executeAction(
            successMessage = "已上传到 WebDAV 备份",
            action = syncRepository::backupToWebDav,
        )
    }

    fun restoreNow() {
        executeAction(
            successMessage = "已从 WebDAV 恢复数据",
            action = syncRepository::restoreFromWebDav,
        )
    }

    private fun executeAction(
        successMessage: String,
        action: suspend () -> Result<Unit>,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val result = action()
            _uiState.update {
                it.copy(
                    isBusy = false,
                    lastAction = result.fold(
                        onSuccess = { successMessage },
                        onFailure = { error -> error.message ?: "操作失败" },
                    ),
                )
            }
        }
    }
}
