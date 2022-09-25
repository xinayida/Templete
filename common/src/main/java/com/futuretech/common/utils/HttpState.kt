package com.futuretech.common.utils

import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Exception

sealed class HttpState<out T> {
    object Loading : HttpState<Nothing>()
    object Normal : HttpState<Nothing>()

    data class Success<T>(val result: T) : HttpState<T>() {
        val isEmpty: Boolean
            get() {
                return (result is List<*>) && result.isEmpty()
            }
    }

    data class Error(val exception: Exception) : HttpState<Nothing>()
    data class Result<T>(val result: T?, val exception: Exception?) : HttpState<T>() {
        val isEmpty: Boolean
            get() {
                return (result is List<*>) && result.isEmpty()
            }
    }
}

fun <T> httpStateFlow() = MutableStateFlow<HttpState<T>>(HttpState.Normal)