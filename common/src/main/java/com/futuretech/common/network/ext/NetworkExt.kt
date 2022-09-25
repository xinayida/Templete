package com.futuretech.common.network.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.futuretech.common.BuildConfig
import com.futuretech.common.ext.dismissLoading
import com.futuretech.common.ext.launchUI
import com.futuretech.common.ext.showLoading
import com.futuretech.common.utils.XLog
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.*
import rxhttp.wrapper.exception.HttpStatusCodeException
import rxhttp.wrapper.exception.ParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException


fun launchNet(
    showLoading: Boolean? = false,
    onError: ((Throwable) -> Unit)? = null,
    onFinally: (() -> Unit)? = null,
    showErrorToast: Boolean? = false,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return GlobalScope.launchNet(showLoading, showErrorToast, onError, onFinally, block)
}

fun LifecycleOwner.launchNet(
    showLoading: Boolean? = false,
    showErrorToast: Boolean? = false,
    onError: ((Throwable) -> Unit)? = null,
    onFinally: (() -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return lifecycleScope.launchNet(showLoading, showErrorToast, onError, onFinally, block)
}

fun CoroutineScope.launchNet(
    showLoading: Boolean? = false,
    showErrorToast: Boolean? = false,
    onError: ((Throwable) -> Unit)? = null,
    onFinally: (() -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return launch(Dispatchers.Main.immediate) {
        if (showLoading == true) {
            showLoading()
        }
        try {
            block()
        } catch (e: Throwable) {
            e.printStackTrace()
            if (isActive) {
                try {
                    handleNetError(e, onError, showErrorToast)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } finally {
            if (showLoading == true) {
                dismissLoading()
            }
            onFinally?.invoke()
        }
    }
}

fun handleNetError(
    e: Throwable,
    onError: ((Throwable) -> Unit)? = null,
    showErrorToast: Boolean? = true,
) {
    if (showErrorToast == true) {
        e.msg?.let { msg ->
            launchUI {
                ToastUtils.show(msg)
            }
        }
    }
    if (onError != null) {
        try {
            onError(e)
        } catch (e: Throwable) {
        }
    }
    XLog.ex("LiveNet", null, e)
}

open class ErrorResult(
    var code: String = "",
    var desc: String = ""
)

val Throwable.msg: String?
    get() {
        return if (this is HttpStatusCodeException) {
            try {
                Gson().fromJson(this.result, ErrorResult::class.java).desc
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) toString() else "request failed"
            }
        } else if (this is IOException)  //okHttp全局设置超时
        {
            "network error"
        } else if (this is TimeoutException     //方法超时
            || this is TimeoutCancellationException  //协程超时
            || this is SocketTimeoutException// )
        ) {
            "request time out"
        } else if (this is JsonSyntaxException || this is ParseException) {
            null
        } else {
            null
        }
    }