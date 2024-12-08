package com.dicoding.bangkitcapstone.di

import android.content.Context
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.api.ChatApiService
import com.dicoding.bangkitcapstone.data.api.ScanApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.dicoding.bangkitcapstone.BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManager(context)
    }

    // Provide Retrofit for Image Scan API
    @Provides
    @Singleton
    @Named("scan")
    fun provideScanRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.dicoding.bangkitcapstone.BuildConfig.BASE_URL_SCAN)  // Use the scan endpoint from BuildConfig
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    // Provide ScanApiService
    @Provides
    @Singleton
    @Named("scan")
    fun provideScanApiService(@Named("scan") retrofit: Retrofit): ScanApiService {
        return retrofit.create(ScanApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("chatbot")
    fun provideChatbotRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.dicoding.bangkitcapstone.BuildConfig.BASE_URL_CHAT)  // Use the scan endpoint from BuildConfig
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    // Provide ChatBotService
    @Provides
    @Singleton
    @Named("chatbot")
    fun provideChatbotService(@Named("scan") retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }
}