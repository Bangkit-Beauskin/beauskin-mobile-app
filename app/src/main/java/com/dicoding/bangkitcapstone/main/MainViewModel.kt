package com.dicoding.bangkitcapstone.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.repository.MainRepository
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.utils.PermissionUtils
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
    private val repository: MainRepository,
    private val permissionUtils: PermissionUtils
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
        when (skinType) {
            null -> items  // Show all items when no skin type is selected
            else -> items.filter { item ->
                when (item.type) {
                    "product" -> item.skin_type?.lowercase() == skinType.lowercase()
                    "news", "video" -> true  // Always show news and videos regardless of skin type
                    else -> false
                }
            }
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
                        _items.value = response.data
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
        viewModelScope.launch {
            _selectedSkinType.value = type?.lowercase()
        }
    }

    fun clearSkinTypeFilter() {
        viewModelScope.launch {
            _selectedSkinType.value = null
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

    // Method to check if the required permissions for scanning are granted
    fun checkPermissionForScan(
        requiredMediaPermission: String, // Media permission (e.g., storage or images)
        requiredCameraPermission: String, // Camera permission
        onPermissionGranted: () -> Unit, // Action to take if permissions are granted
        onPermissionDenied: () -> Unit // Action to take if permissions are denied
    ) {
        val cameraPermissionGranted = permissionUtils.hasPermission(requiredCameraPermission)
        val mediaPermissionGranted = permissionUtils.hasPermission(requiredMediaPermission)

        // Check different permission scenarios and take appropriate actions
        when {
            cameraPermissionGranted && mediaPermissionGranted -> {
                onPermissionGranted() // Both permissions granted, proceed with showing scan bottom sheet
            }
            cameraPermissionGranted -> {
                // Camera permission granted, but media permission not granted, request media permission
                onPermissionDenied()
            }
            mediaPermissionGranted -> {
                // Media permission granted, but camera permission not granted, request camera permission
                onPermissionDenied()
            }
            else -> {
                // Neither permission granted, request both permissions
                onPermissionDenied()
            }
        }
    }
}