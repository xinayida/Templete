package com.futuretech.common.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * 进行繁忙任务
 */
fun <T> ViewModel.request(
    state: MutableStateFlow<HttpState<T>>,
    work: suspend CoroutineScope.() -> T
) = viewModelScope.launch {
    state.emit(HttpState.Loading)
    try {
        val result = work()
        state.tryEmit(HttpState.Result(result, null))
    } catch (t: Exception) {
        state.tryEmit(HttpState.Result(null, t))
        t.printStackTrace()
    }
}

fun ViewModel.tryLaunch(
    work: suspend CoroutineScope.() -> Unit
) = viewModelScope.launch {
    try {
        work()
    } catch (t: Exception) {
        t.printStackTrace()
    }
}

fun <T> ViewModel.requestCustom(
    state: MutableStateFlow<HttpState<T>>,
    work: suspend CoroutineScope.() -> HttpState.Result<T>
) = viewModelScope.launch {
    state.emit(HttpState.Loading)
    try {
        state.tryEmit(work())
    } catch (t: Exception) {
        state.tryEmit(HttpState.Result(null, t))
        t.printStackTrace()
    }
}