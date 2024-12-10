package com.dicoding.bangkitcapstone.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun isDarkModeEnabled(): Boolean {
        return sharedPrefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(isEnabled: Boolean) {
        sharedPrefs.edit().putBoolean("dark_mode", isEnabled).apply()
    }

    fun logout() {
        authRepository.clearAuth()
        tokenManager.clearTokens()
    }
}