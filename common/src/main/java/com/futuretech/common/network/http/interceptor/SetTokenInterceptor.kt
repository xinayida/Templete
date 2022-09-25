package com.futuretech.common.network.http.interceptor

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.futuretech.common.base.appContext
import com.futuretech.common.event.EventBus
import com.futuretech.common.utils.AppUtils
import com.futuretech.common.utils.DeviceUuidFactory
import com.futuretech.common.utils.showToast
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

class SetTokenInterceptor : Interceptor {

    private val tm = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val cc1 = encode(tm.networkCountryIso)
    private val cc2 = encode(tm.networkOperator)
    private val versionCode = AppUtils.getAppVersionCode(appContext).toString()
    private val tz: String = TimeZone.getDefault().id

    //    private val channel = AppUtils.getMetaData<String>(appContext, "CHANNEL")
    private val mid = DeviceUuidFactory.getMid(appContext)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

//        builder.addHeader("Authorization", globalViewModel.token ?: "")
//        builder.addHeader("uid", (globalViewModel.id ?: 0).toString())
        builder.addHeader("User-Agent", "gzip,${Build.MODEL},${Build.DEVICE},${Build.VERSION.SDK_INT}")
        builder.addHeader("mid", mid)
        builder.addHeader("cc1", cc1)
        builder.addHeader("cc2", cc2)
        builder.addHeader("tz", tz)
        builder.addHeader("client", "android")
//        builder.addHeader("channel", channel)
        builder.addHeader("cversion", versionCode)
        val response = chain.proceed(builder.build())
        if (response.code == 403 || response.code == 401) {
            EventBus.invokeEvent("AuthError")
            showToast("Need login")
        }
        return response
    }

    private fun encode(str: String): String {
        val temp = str.encodeToByteArray()
        val b: Byte = (0..255).random().toByte()
        val result = ByteArray(temp.size + 1)
        result[0] = b
        System.arraycopy(temp, 0, result, 1, temp.size)
        return android.util.Base64.encodeToString(result, android.util.Base64.NO_WRAP)
    }
}