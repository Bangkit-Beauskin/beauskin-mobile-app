package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.Result
import com.dicoding.bangkitcapstone.data.model.ScanResponse
import com.dicoding.bangkitcapstone.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    // Flag to track first initialization
    private var isFirstLoad = true
    private var isFirstLoad1 = true

    private var isErrorHandled = false  // Flag untuk menandai error sudah ditangani
    private var isErrorHandled1 = false  // Flag untuk menandai error sudah ditangani


    private val _frontImage = MutableLiveData<Uri?>()
    val frontImage: LiveData<Uri?> get() = _frontImage

    private val _leftImage = MutableLiveData<Uri?>()
    val leftImage: LiveData<Uri?> get() = _leftImage

    private val _rightImage = MutableLiveData<Uri?>()
    val rightImage: LiveData<Uri?> get() = _rightImage


    fun setFrontImage(uri: Uri?) {
        _frontImage.value = uri
    }

    fun setLeftImage(uri: Uri?) {
        _leftImage.value = uri
    }

    fun setRightImage(uri: Uri?) {
        _rightImage.value = uri
    }

    fun isFirstLoad(): Boolean {
        return isFirstLoad
    }

    // Mark the first load as complete
    fun markFirstLoadComplete() {
        isFirstLoad = false
    }

    fun isFirstLoad1(): Boolean {
        return isFirstLoad1
    }

    // Mark the first load as complete
    fun markFirstLoadComplete1() {
        isFirstLoad1 = false
    }



    fun setErrorHandled(isHandled: Boolean) {
        isErrorHandled = isHandled
    }

    fun isErrorHandled(): Boolean {
        return isErrorHandled
    }

    fun setErrorHandled1(isHandled: Boolean) {
        isErrorHandled1 = isHandled
    }

    fun isErrorHandled1(): Boolean {
        return isErrorHandled1
    }

    private val _scanResult = MutableLiveData<ScanResponse?>()
    val scanResult: LiveData<ScanResponse?> get() = _scanResult

    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> get() = _uploadStatus

    fun uploadImages() {

        val frontImageUri = _frontImage.value
        val leftImageUri = _leftImage.value
        val rightImageUri = _rightImage.value

        // Validasi gambar terlebih dahulu
        if (frontImageUri != null && leftImageUri != null && rightImageUri != null) {
            viewModelScope.launch {
                _uploadStatus.value = Result.loading()
                try {
                    // Kirim gambar ke repository dan ambil responsenya
                    val response =
                        repository.uploadImages(frontImageUri, leftImageUri, rightImageUri)

                    // Jika responsenya tidak null dan statusnya "success"
                    if (response != null && response.status == "success") {
                        _uploadStatus.value =
                            Result.Success("Upload successful: ${response.status}")  // Status sukses
                        _scanResult.value = response
                    } else {
                        _uploadStatus.value = Result.Error(Exception("Failed to upload images."))
                    }
                } catch (e: Exception) {
                    _uploadStatus.value = Result.Error(e)
                }
            }
        } else {
            _uploadStatus.value =
                Result.Error(Exception("One or more images are missing or invalid"))
        }
    }

}
