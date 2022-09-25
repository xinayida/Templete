package com.futuretech.common.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.futuretech.common.base.appVM
import com.futuretech.common.ext.addActivity
import com.futuretech.common.ext.removeActivity
import com.futuretech.common.utils.XLog

/**
 * 管理Activity栈
 */
private var foregroundCount = 0

class KtxActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        XLog.d(activity.javaClass.simpleName)
        addActivity(activity)
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStarted(p0: Activity) {
        if (foregroundCount <= 0) {
            appVM.appShowOrHide.value = true
        }
        foregroundCount++
    }

    override fun onActivityResumed(p0: Activity) {

    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityStopped(p0: Activity) {
        foregroundCount--
        if (foregroundCount <= 0) {
            appVM.appShowOrHide.value = false
        }
    }


    override fun onActivityDestroyed(activity: Activity) {
        removeActivity(activity)
    }


}