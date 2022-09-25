package com.futuretech.common.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.lang.Exception

object AppUtils {
    /**
     * 获取当前app version code
     */
    fun getAppVersionCode(context: Context): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            println(e.message)
        }
        return appVersionCode
    }

    /**
     * 获取当前app version name
     */
    fun getAppVersionName(context: Context): String {
        var appVersionName = ""
        try {
            val packageInfo = context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            println(e.message)
        }
        return appVersionName
    }

    fun <T> getMetaData(context: Context, key: String?): T? {
        return try {
            // ---get the package info---
            val pm = context.packageManager
            // 这里的context.getPackageName()可以换成你要查看的程序的包名
            val pi = pm.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val metaData = pi.metaData ?: return null
            metaData[key] as T?
        } catch (e: Exception) {
            println("getMetaData Exception: " + e.message)
            null
        }
    }
}