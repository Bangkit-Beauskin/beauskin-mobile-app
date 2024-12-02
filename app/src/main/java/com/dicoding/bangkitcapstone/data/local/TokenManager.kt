package com.dicoding.bangkitcapstone.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TIMESTAMP = "token_timestamp"
        private const val TOKEN_VALIDITY_DURATION = 45 * 60 * 1000
    }

    fun saveSessionToken(token: String) {
        prefs.edit()
            .putString(KEY_SESSION_TOKEN, token)
            .putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getSessionToken(): String? {
        return prefs.getString(KEY_SESSION_TOKEN, null)
    }

    fun saveAccessToken(token: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun isAccessTokenValid(): Boolean {
        val token = getAccessToken() ?: return false
        val timestamp = prefs.getLong(KEY_TOKEN_TIMESTAMP, 0)
        return System.currentTimeMillis() - timestamp < TOKEN_VALIDITY_DURATION
    }

    fun isSessionTokenValid(): Boolean {
        val token = getSessionToken() ?: return false
        val timestamp = prefs.getLong(KEY_TOKEN_TIMESTAMP, 0)
        return System.currentTimeMillis() - timestamp < TOKEN_VALIDITY_DURATION
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_SESSION_TOKEN)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_TOKEN_TIMESTAMP)
            .apply()
    }
}