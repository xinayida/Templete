package com.futuretech.common.utils

import android.app.Application
import android.util.Log
import java.lang.reflect.Field

/**
 * ApplicationKt
 *
 * @author why
 * @since 2022/6/21
 */
fun getApplicationByReflect(): Application? {
    try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val thread: Any = getActivityThread() ?: return null
        val app = activityThreadClass.getMethod("getApplication").invoke(thread) ?: return null
        return app as Application
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun getActivityThread(): Any? {
    val activityThread = getActivityThreadInActivityThreadStaticField()
    return activityThread ?: getActivityThreadInActivityThreadStaticMethod()
}

fun getActivityThreadInActivityThreadStaticField(): Any? {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val sCurrentActivityThreadField: Field =
            activityThreadClass.getDeclaredField("sCurrentActivityThread")
        sCurrentActivityThreadField.isAccessible = true
        sCurrentActivityThreadField.get(null)
    } catch (e: Exception) {
        Log.e(
            "UtilsActivityLifecycle",
            "getActivityThreadInActivityThreadStaticField: " + e.message
        )
        null
    }
}

fun getActivityThreadInActivityThreadStaticMethod(): Any? {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        activityThreadClass.getMethod("currentActivityThread").invoke(null)
    } catch (e: Exception) {
        Log.e(
            "UtilsActivityLifecycle",
            "getActivityThreadInActivityThreadStaticMethod: " + e.message
        )
        null
    }
}

