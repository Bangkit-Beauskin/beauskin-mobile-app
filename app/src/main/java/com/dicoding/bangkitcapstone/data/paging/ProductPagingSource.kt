package com.dicoding.bangkitcapstone.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.bangkitcapstone.data.api.ApiService
import com.dicoding.bangkitcapstone.data.local.TokenManager
import com.dicoding.bangkitcapstone.data.model.Item

class ProductPagingSource(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : PagingSource<Int, Item>() {

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            val token = tokenManager.getSessionToken() ?: tokenManager.getAccessToken()
            ?: return LoadResult.Error(Exception("No valid token found"))

            val response = apiService.getPagedProducts("Bearer $token", page, pageSize)

            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.data
                LoadResult.Page(
                    data = items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (items.isEmpty()) null else page + 1
                )
            } else {
                LoadResult.Error(Exception("Failed to load products: ${response.code()}"))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}