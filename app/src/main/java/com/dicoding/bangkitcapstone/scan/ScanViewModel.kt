package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.bangkitcapstone.data.Result
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

    private var isErrorHandled = false  // Flag untuk menandai error sudah ditangani


    private val _frontImage = MutableLiveData<Uri?>()
    val frontImage: LiveData<Uri?> get() = _frontImage

    private val _leftImage = MutableLiveData<Uri?>()
    val leftImage: LiveData<Uri?> get() = _leftImage

    private val _rightImage = MutableLiveData<Uri?>()
    val rightImage: LiveData<Uri?> get() = _rightImage

    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> get() = _uploadStatus

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

    fun setErrorHandled(isHandled: Boolean) {
        isErrorHandled = isHandled
    }

    fun isErrorHandled(): Boolean {
        return isErrorHandled
    }


    fun uploadImages() {
        // Ambil URI gambar dari LiveData
        val frontImageUri = _frontImage.value
        val leftImageUri = _leftImage.value
        val rightImageUri = _rightImage.value

        // Lakukan upload dengan repository
        viewModelScope.launch {
            _uploadStatus.value = Result.loading()  // Tampilkan loading sebelum upload
            try {
                // Kirim gambar ke repository
                repository.uploadImages(frontImageUri, leftImageUri, rightImageUri)
                _uploadStatus.value = Result.success("Upload successful")  // Status sukses
            } catch (e: Exception) {
                _uploadStatus.value = Result.Error(e)  // Status gagal jika terjadi error
            }
        }
    }

}
