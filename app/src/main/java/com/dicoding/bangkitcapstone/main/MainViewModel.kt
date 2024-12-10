package com.dicoding.bangkitcapstone.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.data.repository.MainRepository
import com.dicoding.bangkitcapstone.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val permissionUtils: PermissionUtils
) : ViewModel() {

    private data class FilterState(
        val skinType: String? = null,
        val contentType: String? = null
    )

    private val _currentFilter = MutableStateFlow(FilterState())
    private val _error = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val error: StateFlow<String?> = _error
    val isLoading: StateFlow<Boolean> = _isLoading

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: Flow<PagingData<Item>> = _currentFilter
        .flatMapLatest { filterState ->
            repository.getPagedProducts()
                .map { pagingData ->
                    pagingData.filter { item ->
                        when {
                            // If no filters are selected (All)
                            filterState.skinType == null && filterState.contentType == null -> true

                            // If only content type filter is selected
                            filterState.skinType == null && filterState.contentType != null ->
                                item.type == filterState.contentType

                            // If only skin type filter is selected
                            filterState.skinType != null && filterState.contentType == null ->
                                if (item.type == "product") {
                                    item.skin_type?.lowercase() == filterState.skinType.lowercase()
                                } else {
                                    false
                                }

                            // If both filters are selected (shouldn't happen in current UI)
                            else -> {
                                if (filterState.contentType == "product") {
                                    item.type == "product" &&
                                            item.skin_type?.lowercase() == filterState.skinType?.lowercase()
                                } else {
                                    item.type == filterState.contentType
                                }
                            }
                        }
                    }
                }
        }
        .cachedIn(viewModelScope)

    fun setSkinType(type: String?) {
        viewModelScope.launch {
            _currentFilter.value = _currentFilter.value.copy(
                skinType = type?.lowercase(),
                contentType = if (type != null) "product" else null
            )
        }
    }

    fun setContentType(type: String?) {
        viewModelScope.launch {
            _currentFilter.value = _currentFilter.value.copy(
                contentType = type?.lowercase(),
                skinType = null // Reset skin type when content type is selected
            )
        }
    }

    fun clearFilters() {
        viewModelScope.launch {
            _currentFilter.value = FilterState()
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