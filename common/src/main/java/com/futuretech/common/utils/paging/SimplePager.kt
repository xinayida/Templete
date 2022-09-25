package com.futuretech.common.utils.paging

import androidx.lifecycle.ViewModel
import androidx.paging.*
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.futuretech.common.base.appContext
import com.futuretech.common.utils.NetCheckUtil.isConnected
import com.futuretech.common.utils.showToast
import kotlinx.coroutines.flow.Flow

fun <T : Any> ViewModel.simplePager(
    config: AppPagingConfig = AppPagingConfig(),
    callAction: suspend (page: Long) -> ListWrapper<T>
): Flow<PagingData<T>> {
    return pager(config, 0L) {
        val page = it.key?.toLong() ?: 0L
        var nextPage = 0L
        val response = try {
            val result = callAction.invoke(page)
            nextPage = result.offset
            PageState.Success(callAction.invoke(page))
        } catch (e: Exception) {
            if (appContext.isConnected().not()) {
                showToast("No network, please retry later")
            } else {
                showToast("Request error，please retry later")
            }
            PageState.Error(e)
        }
        when (response) {
            is PageState.Success -> {
                val data = response.result
//                val hasNotNext = (data.list.size < it.loadSize) || (data.offset == -1L)
                PagingSource.LoadResult.Page(
                    data = data.list?: arrayListOf(),
                    prevKey = null,//if (page - 1 > 0) page - 1 else null,
                    nextKey = nextPage//if (hasNotNext) null else page + 1
                )
            }
            is PageState.Error -> {
                PagingSource.LoadResult.Error(response.exception)
            }
        }
    }
}

fun <K : Any, V : Any> ViewModel.pager(
    config: AppPagingConfig = AppPagingConfig(),
    initialKey: K? = null,
    loadData: suspend (PagingSource.LoadParams<K>) -> PagingSource.LoadResult<K, V>
): Flow<PagingData<V>> {
    val baseConfig = PagingConfig(
        config.pageSize,
        initialLoadSize = config.initialLoadSize,
        prefetchDistance = config.prefetchDistance,
        maxSize = config.maxSize,
        enablePlaceholders = config.enablePlaceholders
    )
    return Pager(
        config = baseConfig,
        initialKey = initialKey
    ) {
        object : PagingSource<K, V>() {
            override suspend fun load(params: LoadParams<K>): LoadResult<K, V> {
                return loadData.invoke(params)
            }

            override fun getRefreshKey(state: PagingState<K, V>): K? {
                return initialKey
            }

        }
    }.flow.cachedIn(viewModelScope)
}