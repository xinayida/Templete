package com.futuretech.common.ext

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import com.forjrking.lubankt.Luban
import com.futuretech.common.BuildConfig
import com.futuretech.common.base.appContext
import com.futuretech.common.utils.MD5Utils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*


/**
 * Created by Stefan on 1/31/21.
 */


/**
 * 反射获取、设置私有变量
 */
fun <T : Any> T.setAndReturnPrivateProperty(variableName: String, data: Any): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        field.set(this, data)
        return@let field.get(this)
    }
}

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

/**
 * 获取当前进程的名称，默认进程名称是包名
 */
val currentProcessName: String?
    get() {
        val pid = android.os.Process.myPid()
        val mActivityManager = appContext.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager
        try {
            for (appProcess in mActivityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    return appProcess.processName
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

/**
 * 获取packageName
 */
fun getPackageNameName(context: Context): String {
    try {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        return pi.packageName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}

/**
 * 获取versionName
 */
fun getAppVersion(context: Context): String {
    try {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        return pi.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}

private val activityList = LinkedList<Activity>()

//栈顶
val currentActivity: Activity? get() = if (activityList.isEmpty()) null else activityList.last

/**
 * 入栈
 */
fun addActivity(activity: Activity) {
    activityList.add(activity)
}

/**
 * 出栈
 */
fun removeActivity(activity: Activity) {
    if (!activity.isFinishing) {
        activity.finish()
    }
    activityList.remove(activity)
}

/**
 * 出栈
 */
fun removeActivity(cls: Class<*>) {
    if (activityList.isNullOrEmpty()) return
    val index = activityList.indexOfFirst { it.javaClass == cls }
    if (index == -1) return
    if (!activityList[index].isFinishing) {
        activityList[index].finish()
    }
    activityList.removeAt(index)
}

/**
 * 全部出栈
 */
fun removeAllActivity() {
    activityList.forEach {
        if (!it.isFinishing) {
            it.finish()
        }
    }
    activityList.clear()
}

fun registerNetWork(context: Context, callback: NetworkCallback) {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        cm.registerDefaultNetworkCallback(callback)
    } else {
        val builder = NetworkRequest.Builder()
        val request = builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        cm.requestNetwork(request, callback)
    }
}

fun unregisterNetWork(context: Context, callback: NetworkCallback?) {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.unregisterNetworkCallback(callback!!)
}


fun Activity.getAlbumPermission(callback: () -> Unit) {
    XXPermissions.with(this)
        .permission(arrayListOf(Permission.MANAGE_EXTERNAL_STORAGE))
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                callback()
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                ToastUtils.show("permission error: $permissions")
            }
        })
}

fun Context.hasPermission(permission: String): Boolean {
    return checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * 复制文件到沙盒目录
 */
fun Uri.copyToSandbox(
    saveDir: String,
    error: (String) -> Unit = {},
    success: (File) -> Unit = {}
) {
    File(saveDir).checkOrCreateDir()

    val uri = this
    launchUI {
        val file = withContext(Dispatchers.IO) {
            runCatching {
                val name = uri.path
                val fileMd5 = MD5Utils.getStringMD5(name)

                val parcelFileDescriptor = appContext.contentResolver
                    .openFileDescriptor(uri, "r")

                val fileDescriptor = parcelFileDescriptor?.fileDescriptor

                File(saveDir, fileMd5).apply {
                    saveFile(fileDescriptor, this)
                }
            }.getOrNull()
        }

        if (file == null || file.exists().not()) {
            error.invoke("文件复制错误")
        } else {
            success.invoke(file)
        }
    }
}

fun File.compressImage(
    outPutDir: String,
    error: (String) -> Unit = {},
    success: (File) -> Unit = {}
) {
    File(outPutDir).checkOrCreateDir()

    Luban.with()
        .load(this)
        .setOutPutDir(outPutDir)
        .concurrent(true)
        .useDownSample(true)
        .format(Bitmap.CompressFormat.JPEG)
        .ignoreBy(200)
        .quality(95)
        .filter {
            !it.name.endsWith(".gif")
        }
        .compressObserver {
            onStart = {}
            onCompletion = {}
            onSuccess = {
                success.invoke(it)
            }
            onError = { e, _ ->
                error.invoke(e.toString())
            }
        }.launch()
}


fun saveFile(fileDescriptor: FileDescriptor?, dest: File?) {
    var bis: BufferedInputStream? = null
    var bos: BufferedOutputStream? = null
    try {
        bis = BufferedInputStream(FileInputStream(fileDescriptor))
        bos = BufferedOutputStream(FileOutputStream(dest, false))
        val buf = ByteArray(1024)
        bis.read(buf)
        do {
            bos.write(buf)
        } while (bis.read(buf) != -1)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        try {
            bis?.close()
            bos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}


/**
 * 创建文件夹
 */
fun File.checkOrCreateDir() {
    if (this.exists()) {
        if (this.isFile) {
            this.delete()
        }
        this.mkdirs()
    } else {
        this.mkdirs()
    }
}

fun debugToast(text: String) {
    if (BuildConfig.DEBUG) {
        ToastUtils.show(text)
    }
}