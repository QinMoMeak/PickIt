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
    val providerOptions: List<AiProviderUiModel> = AiProviderCatalog.providers,
    val selectedProviderId: String = "zhipu",
    val selectedModel: String = "glm-4.6v-flash",
    val availableModels: List<String> = AiProviderCatalog.find("zhipu").supportedModels,
    val apiKey: String = "",
    val baseUrl: String = AiProviderCatalog.find("zhipu").defaultBaseUrl,
    val webDavPath: String = "",
    val lastAction: String = "尚未执行同步操作",
    val noticeMessage: String? = null,
    val isBusy: Boolean = false,
    val isAiSheetVisible: Boolean = false,
    val hasCustomBaseUrl: Boolean = false,
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
                val provider = AiProviderCatalog.find(settings.aiProvider)
                val availableModels = provider.supportedModels
                val selectedModel = settings.aiModel
                    .takeIf { availableModels.contains(it) }
                    ?: availableModels.firstOrNull().orEmpty()
                val hasCustomBaseUrl = settings.apiBaseUrl.isNotBlank() &&
                    settings.apiBaseUrl != provider.defaultBaseUrl

                _uiState.update {
                    it.copy(
                        selectedProviderId = provider.providerId,
                        selectedModel = selectedModel,
                        availableModels = availableModels,
                        apiKey = settings.aiApiKey,
                        baseUrl = settings.apiBaseUrl.ifBlank { provider.defaultBaseUrl },
                        webDavPath = settings.webDavPath,
                        hasCustomBaseUrl = hasCustomBaseUrl,
                    )
                }
            }
        }
    }

    fun openAiSheet() {
        _uiState.update { it.copy(isAiSheetVisible = true) }
    }

    fun closeAiSheet() {
        _uiState.update { it.copy(isAiSheetVisible = false) }
    }

    fun onProviderSelected(providerId: String) {
        _uiState.update { current ->
            val oldProvider = AiProviderCatalog.find(current.selectedProviderId)
            val newProvider = AiProviderCatalog.find(providerId)
            val shouldReplaceBaseUrl = !current.hasCustomBaseUrl ||
                current.baseUrl.isBlank() ||
                current.baseUrl == oldProvider.defaultBaseUrl
            val nextModel = current.selectedModel
                .takeIf { newProvider.supportedModels.contains(it) }
                ?: newProvider.supportedModels.firstOrNull().orEmpty()

            current.copy(
                selectedProviderId = newProvider.providerId,
                selectedModel = nextModel,
                availableModels = newProvider.supportedModels,
                baseUrl = if (shouldReplaceBaseUrl) newProvider.defaultBaseUrl else current.baseUrl,
                hasCustomBaseUrl = if (shouldReplaceBaseUrl) false else current.hasCustomBaseUrl,
            )
        }
    }

    fun onModelSelected(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun onApiKeyChange(value: String) {
        _uiState.update { it.copy(apiKey = value) }
    }

    fun onBaseUrlChange(value: String) {
        _uiState.update { it.copy(baseUrl = value, hasCustomBaseUrl = true) }
    }

    fun onWebDavPathChange(value: String) {
        _uiState.update { it.copy(webDavPath = value) }
    }

    fun saveAiConfig() {
        val state = uiState.value
        viewModelScope.launch {
            settingsPreferencesDataSource.updateAiProvider(state.selectedProviderId)
            settingsPreferencesDataSource.updateAiModel(state.selectedModel)
            settingsPreferencesDataSource.updateAiApiKey(state.apiKey)
            settingsPreferencesDataSource.updateApiBaseUrl(state.baseUrl)
            _uiState.update {
                it.copy(
                    isAiSheetVisible = false,
                    lastAction = "已保存 AI 设置",
                    noticeMessage = "AI 设置已保存",
                )
            }
        }
    }

    fun saveWebDavPath() {
        val value = uiState.value.webDavPath
        viewModelScope.launch {
            settingsPreferencesDataSource.updateWebDavPath(value)
            _uiState.update {
                it.copy(
                    lastAction = "已保存 WebDAV 路径",
                    noticeMessage = "WebDAV 路径已保存",
                )
            }
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

    fun consumeNotice() {
        _uiState.update { it.copy(noticeMessage = null) }
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
                    noticeMessage = result.fold(
                        onSuccess = { successMessage },
                        onFailure = { error -> error.message ?: "操作失败" },
                    ),
                )
            }
        }
    }
}
