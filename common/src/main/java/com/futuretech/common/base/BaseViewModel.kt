package com.futuretech.common.base

import android.os.Bundle
import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futuretech.common.network.ext.launchNet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


/**
 * ViewModel基类
 */
@Keep
open class BaseViewModel : ViewModel() {

    val loadingChange: UiLoadingChange by lazy { UiLoadingChange() }
    var loadingFinished = false

    open fun onViewCreate(arguments: Bundle) {}
    open fun onViewDestroy() {}

    /**
     * 内置封装好的可通知Activity/fragment 显示隐藏加载框 因为需要跟网络请求显示隐藏loading配套才加的
     */
    //显示加载框
    class UiLoadingChange {
        //请求时loading
        val loading by lazy { MutableLiveData<LoadingEntity>() }
    }

    protected fun showLoading() {
        loadingChange.loading.value = LoadingEntity(isShow = true)
    }

    protected fun dismissLoading() {
        loadingChange.loading.value = LoadingEntity(isShow = false)
    }

    fun request(
        showLoading: Boolean? = false,
        showErrorToast: Boolean? = null,
        request: suspend CoroutineScope.() -> Unit,
    ): Job {
        return requestError(
            showLoading = showLoading,
            showErrorToast = showErrorToast,
            null,
            null,
            request
        )
    }

    protected fun requestError(
        showLoading: Boolean? = false,
        showErrorToast: Boolean? = null,
        onError: ((it: Throwable) -> Unit)? = null,
        onFinally: (() -> Unit)? = null,
        request: suspend CoroutineScope.() -> Unit,
    ): Job {
        return viewModelScope.launchNet(
            showLoading, showErrorToast == true || (showErrorToast == null && showLoading!!),
            onError, onFinally, request
        )
    }

//    fun getUserBrief(uid: Long, callback: (user: UserBriefFollowSO) -> Unit) {
//        request {
//            callback.invoke(CommonRepository.getUserBrief(uid).await())
//        }
//    }
}

/**
 * 用于加载中页面显示
 * Created by Stefan on 1/31/21.
 */
data class LoadingEntity(
    @LoadingType var loadingType: Int = LoadingType.LOADING_DIALOG,
    var loadingMessage: String = "",
    var isShow: Boolean = false
)

@IntDef(LoadingType.LOADING_NULL, LoadingType.LOADING_DIALOG, LoadingType.LOADING_XML)
@Retention(AnnotationRetention.SOURCE)
annotation class LoadingType {
    companion object {
        //请求时不需要Loading
        const val LOADING_NULL = 0

        //请求时弹出 Dialog弹窗Loading
        const val LOADING_DIALOG = 1

        //请求时 界面 Loading Error Empty
        const val LOADING_XML = 2
    }
}