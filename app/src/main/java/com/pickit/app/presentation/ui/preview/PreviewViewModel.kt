package com.pickit.app.presentation.ui.preview

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickit.app.application.ParseProductUseCase
import com.pickit.app.domain.model.ParsedProductDraft
import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.model.SourceType
import com.pickit.app.domain.model.Tag
import com.pickit.app.domain.model.toDraft
import com.pickit.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreviewUiState(
    val isLoading: Boolean = true,
    val draft: ParsedProductDraft = ParsedProductDraft(),
    val errorMessage: String? = null,
    val savedProductId: String? = null,
)

@HiltViewModel
class PreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val parseProductUseCase: ParseProductUseCase,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val imageUriArg: String = savedStateHandle["imageUri"] ?: ""
    private val noteArg: String = savedStateHandle["note"] ?: ""

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = parseProductUseCase(
                imageUri = imageUriArg.takeIf { it.isNotBlank() }?.let(Uri::parse),
                userNote = noteArg.ifBlank { null },
            )
            result.onSuccess { parsed ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        draft = parsed.toDraft(sourceNote = noteArg.ifBlank { null }),
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        draft = ParsedProductDraft(sourceNote = noteArg.ifBlank { null }),
                        errorMessage = error.message ?: "识别失败，请手动补充信息",
                    )
                }
            }
        }
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(draft = it.draft.copy(title = value)) }

    fun updateBrand(value: String) = _uiState.update { it.copy(draft = it.draft.copy(brand = value)) }

    fun updateShop(value: String) = _uiState.update { it.copy(draft = it.draft.copy(shopName = value)) }

    fun updatePrice(value: String) = _uiState.update {
        it.copy(
            draft = it.draft.copy(
                priceText = value,
                priceValue = value.filter { char -> char.isDigit() || char == '.' }.toDoubleOrNull(),
            ),
        )
    }

    fun updateSummary(value: String) = _uiState.update { it.copy(draft = it.draft.copy(summary = value)) }

    fun updateReason(value: String) = _uiState.update { it.copy(draft = it.draft.copy(recommendationReason = value)) }

    fun updatePlatform(platform: Platform) = _uiState.update { it.copy(draft = it.draft.copy(platform = platform)) }

    fun saveDraft() {
        val draft = uiState.value.draft
        viewModelScope.launch {
            val productId = UUID.randomUUID().toString()
            val now = Instant.now().toString()
            val product = ProductItem(
                id = productId,
                title = draft.title.ifBlank { "未命名商品" },
                brand = draft.brand,
                category = draft.category,
                subCategory = draft.subCategory,
                platform = draft.platform,
                shopName = draft.shopName,
                currentPriceText = draft.priceText,
                currentPriceValue = draft.priceValue,
                currency = draft.currency,
                spec = draft.spec,
                summary = draft.summary,
                recommendationReason = draft.recommendationReason,
                tags = draft.tags.map { Tag(UUID.randomUUID().toString(), it) },
                sourceType = SourceType.IMAGE_WITH_TEXT,
                sourceNote = draft.sourceNote,
                confidence = draft.confidence,
                aiRawJson = draft.rawText,
                createdAt = now,
                updatedAt = now,
            )
            productRepository.saveProduct(product)
            _uiState.update { it.copy(savedProductId = productId) }
        }
    }
}
