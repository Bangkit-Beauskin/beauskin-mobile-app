package com.dicoding.bangkitcapstone.scan

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class ScanViewModel @Inject constructor() : ViewModel() {

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    // Method to update the image URI
    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }
}
