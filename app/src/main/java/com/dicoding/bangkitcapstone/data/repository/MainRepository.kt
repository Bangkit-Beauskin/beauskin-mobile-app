package com.dicoding.bangkitcapstone.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.Item
import com.dicoding.bangkitcapstone.data.paging.ProductPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun getPagedProducts(): Flow<PagingData<Item>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                enablePlaceholders = false,
                initialLoadSize = 3
            ),
            pagingSourceFactory = {
                ProductPagingSource(apiService, tokenManager)
            }
        ).flow
    }
}