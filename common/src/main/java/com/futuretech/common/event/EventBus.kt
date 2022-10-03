package com.futuretech.common.event

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object EventBus {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _events = MutableSharedFlow<AppEventWrap>()
    private val events = _events.asSharedFlow()
    val eventBus: SharedFlow<AppEventWrap>
        get() = events

    fun invokeEvent(event: String, args: Any? = null) = scope.launch { _events.emit(AppEventWrap(event, args)) }

    init {
        GlobalScope.launch {
            //日志打印当前订阅的订阅者数量
            _events.subscriptionCount.collect {
                Log.d("flow", "subscriptionCount $it")
            }
        }
    }

    fun LifecycleOwner.handleEvent(event: String, onCall: (Any?) -> Unit) {
        handleEvent(event, this.lifecycleScope, onCall)
    }

    fun handleEvent(event: String, soc: CoroutineScope? = scope, onCall: (Any?) -> Unit) {
        soc!!.launch {
            events.filter { e -> e.event == event }.collectLatest {
                onCall.invoke(it.args)
            }
        }
    }
}

data class AppEventWrap(val event: String, val args: Any? = null)