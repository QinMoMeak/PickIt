package com.pickit.app.presentation.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickit.app.domain.model.ProductStatus
import com.pickit.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalCount: Int = 0,
    val notPurchasedCount: Int = 0,
    val inUseCount: Int = 0,
    val tagTop: List<Pair<String, Int>> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    productRepository: ProductRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeProducts().collect { products ->
                _uiState.value = StatsUiState(
                    totalCount = products.size,
                    notPurchasedCount = products.count { it.status == ProductStatus.NOT_PURCHASED },
                    inUseCount = products.count { it.status == ProductStatus.IN_USE },
                    tagTop = products.flatMap { product -> product.tags }
                        .groupingBy { tag -> tag.name }
                        .eachCount()
                        .toList()
                        .sortedByDescending { it.second }
                        .take(5),
                )
            }
        }
    }
}
