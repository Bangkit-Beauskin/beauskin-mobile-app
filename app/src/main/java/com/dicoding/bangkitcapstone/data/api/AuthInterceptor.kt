package com.dicoding.bangkitcapstone.data.api

import com.dicoding.bangkitcapstone.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        val token = when {
            tokenManager.isAccessTokenValid() -> tokenManager.getAccessToken()
            tokenManager.isSessionTokenValid() -> tokenManager.getSessionToken()
            else -> null
        }

        val newRequest = originalRequest.newBuilder().apply {
            token?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}