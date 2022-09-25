package com.futuretech.business_common.base

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.futuretech.common.base.appContext
import com.futuretech.common.ext.currentProcessName
import com.futuretech.common.lifecycle.KtxActivityLifecycleCallbacks
import com.hjq.toast.ToastUtils
import com.hjq.toast.style.WhiteToastStyle

class AppApplication : Application(), ViewModelStoreOwner {
    private lateinit var mAppViewModelStore: ViewModelStore
    private var mFactory: ViewModelProvider.Factory? = null

    override fun onCreate() {
        super.onCreate()
        // Toast 不延迟 init
        ToastUtils.init(this, WhiteToastStyle())
        mAppViewModelStore = ViewModelStore()
        val processName = currentProcessName
        if (processName == packageName) {
            //主进程初始化
            initAppTasks()
        }
    }

    private fun initAppTasks() {
        //注册全局的Activity生命周期管理
        appContext.registerActivityLifecycleCallbacks(KtxActivityLifecycleCallbacks())
    }

    private fun getAppFactory(): ViewModelProvider.Factory {
        if (mFactory == null) {
            mFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        }
        return mFactory as ViewModelProvider.Factory
    }

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore
    }

    override fun onTrimMemory(level: Int) {
        val bundle = Bundle()
        bundle.putInt("level", level)
//        FirebaseAnalytics.getInstance(this).logEvent("TrimMemoryApplication", bundle)
        super.onTrimMemory(level)
    }

}