package com.futuretech.common.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

object EventBus {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _events = MutableSharedFlow<AppEventWrap>()
    private val events = _events.asSharedFlow()

    fun invokeEvent(event: String, args: Any? = null) = scope.launch { _events.emit(AppEventWrap(event, args)) }

    fun handleEvent(event: String, soc: CoroutineScope? = scope, onCall: (Any?) -> Unit) {
        soc!!.launch {
            events.filter { e -> e.event == event }.collectLatest {
                onCall.invoke(it.args)
            }
        }
    }
}

data class AppEventWrap(val event: String, val args: Any? = null)