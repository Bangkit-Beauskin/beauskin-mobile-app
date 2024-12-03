package com.dicoding.bangkitcapstone.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.repository.MainRepository
import com.dicoding.bangkitcapstone.data.model.Item
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    private val _selectedSkinType = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _selectedItem = MutableStateFlow<Item?>(null)

    val isLoading: StateFlow<Boolean> = _isLoading
    val error: StateFlow<String?> = _error
    val selectedItem: StateFlow<Item?> = _selectedItem

    val filteredItems = combine(_items, _selectedSkinType) { items, skinType ->
        when {
            skinType == null -> items
            else -> items.filter { it.skinType == skinType || it.skinType == null }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getProducts().fold(
                    onSuccess = { response ->
                        _items.value = response.data.map { item ->
                            // Transform YouTube URLs to proper format if needed
                            if (item.type == "video" && item.url?.contains("youtu.be") == true) {
                                item.copy(url = item.url)
                            } else {
                                item
                            }
                        }
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Unknown error occurred"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSkinType(type: String?) {
        _selectedSkinType.value = type
    }

    fun setSelectedItem(item: Item) {
        _selectedItem.value = item
        if (item.type == "product") {
            fetchItemDetails(item.id)
        }
    }

    private fun fetchItemDetails(itemId: String) {
        viewModelScope.launch {
            try {
                repository.getProductDetail(itemId).fold(
                    onSuccess = { response ->
                        val updatedItem = response.data.firstOrNull()
                        if (updatedItem != null) {
                            val currentItems = _items.value.toMutableList()
                            val index = currentItems.indexOfFirst { it.id == itemId }
                            if (index != -1) {
                                currentItems[index] = updatedItem
                                _items.value = currentItems
                                _selectedItem.value = updatedItem
                            }
                        }
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Failed to fetch item details"
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            }
        }
    }
}