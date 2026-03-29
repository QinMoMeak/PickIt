package com.pickit.app.presentation.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickit.app.domain.model.ProductItem
import com.pickit.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
) : ViewModel() {
    private val productId: String? = savedStateHandle["productId"]
    private val _product = MutableStateFlow<ProductItem?>(null)
    val product: StateFlow<ProductItem?> = _product.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.observeProducts().collect { products ->
                _product.value = productId?.let { id -> products.firstOrNull { it.id == id } } ?: products.firstOrNull()
            }
        }
    }
}
