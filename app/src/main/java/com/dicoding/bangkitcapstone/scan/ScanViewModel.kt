package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.bangkitcapstone.data.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository // Tambahkan repository jika perlu untuk API atau database
) : ViewModel() {

    private val _frontImage = MutableLiveData<Uri?>()
    val frontImage: LiveData<Uri?> get() = _frontImage

    private val _leftImage = MutableLiveData<Uri?>()
    val leftImage: LiveData<Uri?> get() = _leftImage

    private val _rightImage = MutableLiveData<Uri?>()
    val rightImage: LiveData<Uri?> get() = _rightImage

    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> get() = _uploadStatus

    fun setFrontImage(uri: Uri) {
        _frontImage.value = uri
    }

    fun setLeftImage(uri: Uri) {
        _leftImage.value = uri
    }

    fun setRightImage(uri: Uri) {
        _rightImage.value = uri
    }
}
