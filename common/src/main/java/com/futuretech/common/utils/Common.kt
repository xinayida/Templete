package com.futuretech.common.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import com.futuretech.common.base.appContext
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.tencent.mmkv.MMKV

/**
 * 获取MMKV
 */
val mmkv: MMKV by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    if (MMKV.getRootDir() == null) {
        MMKV.initialize(appContext, appContext.filesDir.absolutePath + "/mmkv")
    }
    MMKV.mmkvWithID("futureTech")
}

/**
 * 根据Key 删除
 */
fun MMKV.remove(@NonNull key: String) {
    mmkv.remove(key)
}

/**
 * 全部删除
 */
fun MMKV.clear() {
    mmkv.clearAll()
}

//@Composable
//inline fun <reified T : ViewModel> NavHostController.getRouteViewModel(routeName: String): T {
//    val loginBackStackEntry = remember { getBackStackEntry(routeName) }
//    return viewModel(loginBackStackEntry)
//}

fun getAppVersion(): String? {
    return try {
        val pInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }
}

/**
 * 请求权限
 */
fun requestPermission(context: Activity, vararg permissions: String, force: Boolean = false, onGranted: () -> Unit) {
    XXPermissions.with(context)
        .permission(permissions)
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                if (all) {
                    onGranted()
                }
            }

            override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                val tipStr = if (permissions.size == 1) "Permission" else "Permissions"
                showToast("Need $tipStr $permissions")
                if (never && force) {
                    XXPermissions.startPermissionActivity(context, permissions)
                }
            }
        })
}