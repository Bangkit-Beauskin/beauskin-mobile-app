package com.dicoding.bangkitcapstone.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "secure_auth"
        private const val ACCESS_TOKEN = "access_token"
        private const val ACCESS_TOKEN_EXPIRY = "access_token_expiry"
        private const val SESSION_TOKEN = "session_token"
        private const val SESSION_TOKEN_EXPIRY = "session_token_expiry"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        PREF_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String) {
        encryptedPrefs.edit()
            .putString(ACCESS_TOKEN, token)
            .putLong(ACCESS_TOKEN_EXPIRY, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))
            .apply()
    }

    fun saveSessionToken(token: String) {
        encryptedPrefs.edit()
            .putString(SESSION_TOKEN, token)
            .putLong(SESSION_TOKEN_EXPIRY, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))
            .apply()
    }

    fun getAccessToken(): String? = encryptedPrefs.getString(ACCESS_TOKEN, null)

    fun getSessionToken(): String? = encryptedPrefs.getString(SESSION_TOKEN, null)

    fun isAccessTokenValid(): Boolean {
        val expiry = encryptedPrefs.getLong(ACCESS_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() < expiry
    }

    fun isSessionTokenValid(): Boolean {
        val expiry = encryptedPrefs.getLong(SESSION_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() < expiry
    }

    fun clearTokens() {
        encryptedPrefs.edit().clear().apply()
    }
}