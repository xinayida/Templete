package com.futuretech.common.utils.paging

import java.lang.Exception

sealed class PageState<out T> {
    data class Success<T>(val result: T) : PageState<T>()
    data class Error(val exception: Exception) : PageState<Nothing>()
}