package com.futuretech.common.base

import android.app.Application
import com.futuretech.common.event.AppVM
import com.futuretech.common.utils.getApplicationByReflect

/**
 * Created by Stefan on 1/29/21.
 */

//界面通信ViewModel
val appVM: AppVM by lazy { AppVM() }
private var instance: Application? = null

//全局上下文
val appContext: Application
    get() {
        if (instance != null) {
            return instance!!
        }

        // 通过反射再次获取 application
        val application = getApplicationByReflect()
        if (application != null) {
            instance = application
            return application
        }
        throw NullPointerException("appContext is null!")
    }
