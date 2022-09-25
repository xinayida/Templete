package com.futuretech.common.ext

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.location.Address
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.futuretech.common.R
import com.futuretech.common.base.appContext
import com.futuretech.common.network.base.BaseResponse
import com.futuretech.common.network.ext.msg
import com.futuretech.common.utils.XLog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import com.kunminx.architecture.ui.callback.UnPeekLiveData
import kotlinx.coroutines.*
import org.json.JSONObject
import rxhttp.wrapper.exception.HttpStatusCodeException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 作者　: hegaojian
 * 时间　: 2020/11/18
 * 描述　:一些常用的方法
 */


//fun Fragment.cropImage(data: CropAction) {
//    launchUI {
//        nav(R.id.cropImageFragment, bundleOf(NaviParams.CropUri to data), false)
//    }
//}
//
//fun Fragment.selectAlbum(data: TakePhotoAction) {
//    launchUI {
//        nav(R.id.selectAlbumFragment, bundleOf(NaviParams.SelectPicSO to data), withAnim = false)
//    }
//}

fun getType(raw: Class<*>, vararg args: Type) = object : ParameterizedType {
    override fun getActualTypeArguments(): Array<out Type> = args

    override fun getRawType(): Type = raw

    override fun getOwnerType(): Type? = null

}

/**
 * 有新的事件就延迟等待，触发最后一个事件
 */
fun <T> debounce(
    waitMs: Long? = 300L,
    scope: CoroutineScope,
    destinationFunction: (T) -> Unit,
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(waitMs!!)
            destinationFunction(param)
        }
    }
}

/**
 * 等待时间固定，触发第一个事件
 */
fun <T> throttleFirst(
    skipMs: Long? = 300L,
    scope: CoroutineScope,
    destinationFunction: (T) -> Unit,
): (T) -> Unit {
    var throttleJob: Job? = null
    return { param: T ->
        if (throttleJob?.isCompleted != false) {
            throttleJob = scope.launch {
                destinationFunction(param)
                delay(skipMs!!)
            }
        }
    }
}

/**
 * 等待时间固定，触发最后一个事件
 */
fun <T> throttleLatest(
    intervalMs: Long? = 300L,
    scope: CoroutineScope,
    destinationFunction: (T) -> Unit,
): (T) -> Unit {
    var throttleJob: Job? = null
    var latestParam: T
    return { param: T ->
        latestParam = param
        if (throttleJob?.isCompleted != false) {
            throttleJob = scope.launch {
                delay(intervalMs!!)
                latestParam.let(destinationFunction)
            }
        }
    }
}

inline fun <reified T> loadJson(str: String): T? {
    try {
        return Gson().fromJson(str, T::class.java)
    } catch (e: Exception) {
        XLog.ex(e)
    }
    return null
}

fun <T> loadJsonFromFile(fileName: String, type: Type): T? {
    var reader: BufferedReader? = null
    try {
        val inputStream = appContext.openFileInput(fileName)
        reader = BufferedReader(InputStreamReader(inputStream))
        val x = Gson().fromJson<T>(reader, type)
        XLog.d("load font from local: $x")
        return x
    } catch (e: Exception) {
        XLog.ex(e)
    } finally {
        reader?.close()
    }
    return null
}

fun <T> saveToJson(fileName: String, t: T) {
    val str = t.toJsonStr()
    val os = appContext.openFileOutput(fileName, MODE_PRIVATE)
    var writer: OutputStreamWriter? = null
    try {
        writer = OutputStreamWriter(os)
        writer.write(str)
    } catch (e: Exception) {
        XLog.ex(e)
    } finally {
        try {
            writer?.close()
            os.close()
        } catch (e: Exception) {

        }
    }
//    val file = appContext.getFileStreamPath("fileName")
//    XLog.d("size2 : ${file.length()}")
}


fun Address.getAddressList(): MutableList<String> {
    val result = mutableListOf<String>()
    val maxIndex = maxAddressLineIndex
    for (i in 0 until maxIndex) {
        result.add(getAddressLine(i))
    }
    return result
}

val exceptionHandler = CoroutineExceptionHandler { context, exception ->
//    XLog.ex(exception)
}

fun launchUI(block: suspend () -> Unit): Job {
    return GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
        block()
    }
}

fun LifecycleOwner.launchUI(block: suspend () -> Unit): Job {
    return lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
        block()
    }
}

fun ViewModel.launchUI(block: suspend () -> Unit): Job {
    return viewModelScope.launch(Dispatchers.Main + exceptionHandler) {
        block()
    }
}

fun launchIO(block: suspend () -> Unit): Job {
    return GlobalScope.launch(Dispatchers.IO + exceptionHandler) {
        block()
    }
}

fun LifecycleOwner.launchIO(block: suspend () -> Unit): Job {
    return if (this is Fragment) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
            block()
        }
    } else {
        lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
            block()
        }
    }
}

fun ViewModel.launchIO(block: suspend () -> Unit): Job {
    return viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
        block()
    }
}

fun bindSwitch(btn1: View, btn2: View, view1: View, view2: View, callback: (() -> Unit)? = null) {
    btn1.setOnClickListener {
        view1.visibility = View.GONE
        view2.visibility = View.VISIBLE
        callback?.invoke()
    }
    btn2.setOnClickListener {
        view1.visibility = View.VISIBLE
        view2.visibility = View.GONE
    }
}

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


//fun logFirebaseNetError(e: Throwable, type: String? = FirebaseConstant.lh_network_error, path: String? = null) {
//    val address = WMServiceManager.getCommonService().lastAddress
//    val bundle =
//        bundleOf(
//            FirebaseConstant.param_host_reason to "${Api.domain()}(${e.cause?.javaClass?.simpleName}-${e.cause?.message})",
//            FirebaseConstant.param_net_type to NetworkUtil.getConnectedType(
//                appContext
//            )
//        )
//    val front = WMServiceManager.getCommonService().isAppInFront()
//    val network = NetworkUtil.getConnectedType(appContext)
//    bundle.putString(
//        FirebaseConstant.param_id_path_reason,
//        "${self?.id.toString()}#$path#${e.cause?.javaClass?.simpleName}-${e.cause?.message}#${front}#${network}"
//    )
//    if (address != null) {
//        bundle.putString(FirebaseConstant.param_loc, "${address.locality}, ${address.countryName}")
//    }
////    logFirebase(type!!, bundle)
//}

fun handleNetError(
    e: Throwable,
    onError: ((Throwable) -> Unit)? = null,
    showErrorToast: Boolean? = true,
) {
//    when (e) {
//        is LHParseException -> {
////            launchNet {
////                CommonRepository.postApiFailResponse(e.url, e.response)
////            }
//        }
//        is LHNetworkException -> {
////            logFirebaseNetError(e, FirebaseConstant.lh_network_error, e.path)
//        }
//    }
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

fun Throwable.toResult(): BaseResponse? {
    if (this is HttpStatusCodeException) {
        try {
            return this.result.toBean(BaseResponse::class.java)
        } catch (e: Exception) {
        }
    }
    return null
}

fun Throwable.toLogStr(): String {
    return if (this is HttpStatusCodeException) {
        return toResult().toString()
    } else {
        Log.getStackTraceString(this)
    }
}

fun <T> String.toBean(clz: Class<T>): T {
    return Gson().fromJson(this, clz)
}

fun <T> load(loader: suspend () -> T): Deferred<T> {
    return GlobalScope.async(context = Dispatchers.IO, start = CoroutineStart.LAZY, block = {
        loader()
    })
}


fun <T> LifecycleOwner.load(loader: suspend () -> T): Deferred<T> {
    return lifecycleScope.async(context = Dispatchers.IO, start = CoroutineStart.LAZY, block = {
        loader()
    })
}

fun delay(time: Long, func: (() -> Unit)): Job {
    return load {
        delay(time)
    } then {
        func.invoke()
    }
}

fun LifecycleOwner.delay(time: Long, func: (() -> Unit)): Job {
    return load {
        delay(time)
    } then {
        func.invoke()
    }
}

fun Fragment.delay(time: Long, func: (() -> Unit)): Job {
    return viewLifecycleOwner.load {
        delay(time)
    } then {
        func.invoke()
    }
}

fun parseError(it: Throwable): BaseResponse? {
    if (it is HttpStatusCodeException) {
        return Gson().fromJson(it.result, BaseResponse::class.java)
    }
    return null
}

infix fun <T> Deferred<T>.then(block: suspend (T) -> Unit): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        try {
            block(this@then.await())
        } catch (e: Exception) {
            dismissLoading()
            XLog.e("Stefan", Log.getStackTraceString(e))
        }
    }
}

infix fun <T> Deferred<T>.thenWithError(block: suspend (T?, Exception?) -> Unit): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        try {
            block(this@thenWithError.await(), null)
        } catch (e: Exception) {
            block(null, e)
        }
    }
}

fun Any?.toJsonStr(): String {
    return GsonUtils.gson.toJson(this)
}

fun Uri?.toJson(): JSONObject? {
    if (this == null) return null
    val json = JSONObject()
    queryParameterNames.forEach {
        json.put(it, getQueryParameter(it))
    }
    return json
}

fun Any?.toJsonPrettyStr(): String {
    return GsonUtils.gsonPretty.toJson(this)
}

fun Any?.toJsonElement(): JsonElement {
    return JsonParser.parseString(this.toJsonStr())
}

object GsonUtils {
    private val gsonBuilder: GsonBuilder by lazy { GsonBuilder() }
    val gson: Gson by lazy { gsonBuilder.create() }
    val gsonPretty: Gson by lazy { GsonBuilder().setPrettyPrinting().create() }
    fun toJson(paramObject: Any?): String = gson.toJson(paramObject)

    // 从字符串中获取json对象
    inline fun <reified T> String?.fromJson(): T? {
        this ?: return null
        return try {
            gson.fromJson(this, T::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

inline fun <reified T> String.safeParseObj(): T? {
    runCatching {
        return GsonUtils.gson.fromJson(this, T::class.java)
    }.onFailure { it.printStackTrace() }
    return null
}

fun <T> String.safeParseType(type: Type): T? {
    runCatching {
        return GsonUtils.gson.fromJson(this, type)
    }.onFailure { it.printStackTrace() }
    return null
}

fun Any?.toast() {
    ToastUtils.show(this)
}

fun <T> List<T>?.safeGet(index: Int): T? {
    if (this == null || this.isEmpty()) return null
    return get(index)
}

/**
 * 关闭键盘
 */
fun EditText.hideKeyboard() {
    val imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(
        this.windowToken,
        0
    )
}

/**
 * 打开键盘
 */
fun EditText.showKeyboard() {
    this.apply {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }
    val inputManager =
        appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.showSoftInput(this, 0)
}

/**
 * 关闭键盘焦点
 */
fun Activity.hideOffKeyboard() {
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive && this.currentFocus != null) {
        if (this.currentFocus?.windowToken != null) {
            imm.hideSoftInputFromWindow(
                this.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

fun toStartActivity(@NonNull clz: Class<*>) {
    val intent = Intent(appContext, clz)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    appContext.startActivity(intent)
}

fun toStartActivity(@NonNull clz: Class<*>, @NonNull bundle: Bundle) {
    val intent = Intent(appContext, clz)
    intent.apply {
        putExtras(bundle)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    appContext.startActivity(intent)
}

fun toStartActivity(
    activity: Activity,
    @NonNull clz: Class<*>,
    code: Int,
    @NonNull bundle: Bundle,
) {
    activity.startActivityForResult(Intent(appContext, clz).putExtras(bundle), code)
}

fun toStartActivity(
    fragment: Fragment,
    @NonNull clz: Class<*>,
    code: Int,
    @NonNull bundle: Bundle,
) {
    fragment.startActivityForResult(Intent(appContext, clz).putExtras(bundle), code)
}

fun toStartActivity(activity: Activity, @NonNull intent: Intent, code: Int) {
    activity.startActivityForResult(intent, code)
}

fun toStartActivity(
    @NonNull type: Any,
    @NonNull clz: Class<*>,
    code: Int,
    @NonNull bundle: Bundle,
) {
    if (type is Activity) {
        toStartActivity(type, clz, code, bundle)
    } else if (type is Fragment) {
        toStartActivity(type, clz, code, bundle)
    }
}

/**
 * 隐藏状态栏
 */
fun hideStatusBar(activity: Activity) {
    val attrs = activity.window.attributes
    attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
    activity.window.attributes = attrs
}

/**
 * 显示状态栏
 */
fun showStatusBar(activity: Activity) {
    val attrs = activity.window.attributes
    attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
    activity.window.attributes = attrs
}

/**
 * 横竖屏
 */
fun isLandscape(context: Context) =
    context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * 应用商店
 */
fun gotoStore() {
    val uri =
        Uri.parse("market://details?id=" + appContext.packageName)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
        goToMarket.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(goToMarket)
    } catch (ignored: ActivityNotFoundException) {
    }
}

/**
 * 字符串相等
 */
fun isEqualStr(value: String?, defaultValue: String?) =
    if (value.isNullOrEmpty() || defaultValue.isNullOrEmpty()) false else TextUtils.equals(
        value,
        defaultValue
    )

/**
 * Int类型相等
 *
 */
fun isEqualIntExt(value: Int, defaultValue: Int) = value == defaultValue

fun getDrawableExt(id: Int): Drawable? = ContextCompat.getDrawable(appContext, id)

fun getColorExt(id: Int): Int = ContextCompat.getColor(appContext, id)

fun getStringExt(id: Int) = appContext.resources.getString(id)

fun getStringExt(id: Int, vararg: Any) = appContext.resources.getString(id, vararg)

fun getStringArrayExt(id: Int): Array<String> = appContext.resources.getStringArray(id)

fun getIntArrayExt(id: Int) = appContext.resources.getIntArray(id)

fun getDimensionExt(id: Int) = appContext.resources.getDimension(id)


fun <T> UnPeekLiveData<T>.set(t: T?) {
    launchUI {
        value = t
    }
}


fun <T> MutableLiveData<T>.set(t: T?) {
    launchUI {
        value = t
    }
}


//fun warpMedia(file: AlbumFile): Media {
//    val media = Media()
//    media.localPath = file.path
//    if (file.thumbPath != null) {
//        val thumbFile = File(file.thumbPath)
//        val thumbPath = "${file.thumbPath.removeSuffix(thumbFile.extension)}jpg"
//        thumbFile.renameTo(File(thumbPath))
//        media.localThumbPath = thumbPath
//    }
//    media.size = file.size
//    if (file.mediaType == AlbumFile.TYPE_VIDEO) {
//        media.type = "video"
//        media.duration = (file.duration / 1000).toInt()
//    } else {
//        if (file.mimeType.contains("gif", true)) {
//            media.type = "gif"
//        } else {
//            media.type = "image"
//        }
//    }
//    return media
//}

var cameraPermanentDenied = false
fun Fragment.requestCameraPermission(
    onDenied: (permanent: Boolean) -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    requireActivity().requestCameraPermission(onDenied, onSuccess)
}

fun Activity.requestCameraPermission(
    onDenied: (permanent: Boolean) -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    val isGrantedReadContacts =
        XXPermissions.isGranted(this@requestCameraPermission, Permission.CAMERA)
    if (isGrantedReadContacts) {
        onSuccess.invoke()
    } else {
        XXPermissions.with(this)
            .permission(arrayListOf(Permission.CAMERA))//, Permission.MANAGE_EXTERNAL_STORAGE
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    onSuccess.invoke()
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    ToastUtils.show(getString(R.string.camera_permission_error))
                    cameraPermanentDenied =
                        XXPermissions.isPermanentDenied(
                            this@requestCameraPermission,
                            Permission.CAMERA
                        )
                    onDenied.invoke(cameraPermanentDenied)
                }
            })
    }
}

fun Fragment.requestCameraPermission(
    onDenied: (permanent: Boolean) -> Unit = {},
    onSuccess: () -> Unit = {},
    onGranted: () -> Unit,
) {
    requireActivity().requestCameraPermission(onDenied, onSuccess, onGranted)
}

fun Activity.requestCameraPermission(
    onDenied: (permanent: Boolean) -> Unit = {},
    onSuccess: () -> Unit = {},
    onGranted: () -> Unit,
) {
    val isGrantedReadContacts =
        XXPermissions.isGranted(this@requestCameraPermission, Permission.CAMERA)
    if (isGrantedReadContacts) {
        onGranted.invoke()
    } else {
        XXPermissions.with(this)
            .permission(arrayListOf(Permission.CAMERA))//, Permission.MANAGE_EXTERNAL_STORAGE
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    onSuccess.invoke()
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    ToastUtils.show(getString(R.string.camera_permission_error))
                    cameraPermanentDenied =
                        XXPermissions.isPermanentDenied(
                            this@requestCameraPermission,
                            Permission.CAMERA
                        )
                    onDenied.invoke(cameraPermanentDenied)
                }
            })
    }
}

/**
 * 请求录音权限
 */
fun Fragment.requestAudioPermission(
    onDenied: (Boolean) -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    val audioPermission = XXPermissions.isGranted(activity, Permission.RECORD_AUDIO)
    if (audioPermission) {
        onSuccess.invoke()
    } else {
        XXPermissions.with(this)
            .permission(arrayListOf(Permission.RECORD_AUDIO))
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    onSuccess.invoke()
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    onDenied.invoke(never)
                }
            })
    }
}

/**
 * 请求通讯录权限
 */
fun Fragment.requestContactPermission(
    onDenied: (Boolean) -> Unit = {},
    onSuccess: () -> Unit = {},
) {
    val isGrantedReadContacts =
        XXPermissions.isGranted(activity, Permission.READ_CONTACTS)
    if (isGrantedReadContacts) {
        onSuccess.invoke()
    } else {

        XXPermissions.with(this)
            .permission(arrayListOf(Permission.READ_CONTACTS))
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    onSuccess.invoke()
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    onDenied.invoke(never)
                }
            })
    }
}