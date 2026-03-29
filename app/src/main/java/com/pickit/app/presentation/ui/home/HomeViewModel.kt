package com.pickit.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickit.app.domain.model.Platform
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val query: String = "",
    val selectedPlatform: Platform? = null,
    val products: List<ProductItem> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {
    private var allProducts: List<ProductItem> = emptyList()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeProducts().collect { products ->
                allProducts = products
                applyFilter()
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        applyFilter()
    }

    fun onPlatformSelected(platform: Platform?) {
        _uiState.update { current ->
            current.copy(
                selectedPlatform = if (current.selectedPlatform == platform) null else platform,
            )
        }
        applyFilter()
    }

    private fun applyFilter() {
        val current = _uiState.value
        val normalized = current.query.trim()
        val filtered = allProducts.filter { product ->
            val matchesQuery = normalized.isBlank() || listOfNotNull(
                product.title,
                product.brand,
                product.shopName,
                product.summary,
            ).any { it.contains(normalized, ignoreCase = true) } ||
                product.tags.any { it.name.contains(normalized, ignoreCase = true) }
            val matchesPlatform = current.selectedPlatform == null || product.platform == current.selectedPlatform
            matchesQuery && matchesPlatform
        }
        _uiState.update { it.copy(products = filtered) }
    }
}
