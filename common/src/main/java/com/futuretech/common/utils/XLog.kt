package com.futuretech.common.utils

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.socks.library.klog.BaseLog
import com.socks.library.klog.FileLog
import com.socks.library.klog.JsonLog
import com.socks.library.klog.XmlLog
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by Stefan on 2/12/21.
 */
object XLog {
    private val gson by lazy {
        Gson()
    }
    const val NULL_TIPS = "Log with null object"
    private const val DEFAULT_MESSAGE = "execute"
    private const val PARAM = "Param"
    private const val NULL = "null"
    private const val TAG_DEFAULT = "XLog"
    private const val SUFFIX = ".java"
    const val JSON_INDENT = 4
    const val V = 0x1
    const val D = 0x2
    const val I = 0x3
    const val W = 0x4
    const val E = 0x5
    const val A = 0x6
    private const val JSON = 0x7
    private const val XML = 0x8
    private const val EX = 0x9
    const val STACK_TRACE_INDEX_6 = 6//for LogExt

    private const val STACK_TRACE_INDEX_5 = 6
    private const val STACK_TRACE_INDEX_4 = 4
    private var mGlobalTag: String? = null
    private var mIsGlobalTagEmpty = true
    private var IS_SHOW_LOG = true
    fun init(isShowLog: Boolean) {
        IS_SHOW_LOG = isShowLog
    }

    fun init(isShowLog: Boolean, tag: String?) {
        IS_SHOW_LOG = isShowLog
        mGlobalTag = tag
        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag)
    }

    fun v() {
        printLog(V, TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    fun v(msg: Any?) {
        printLog(V, TAG_DEFAULT, msg)
    }

    fun v(tag: String?, objects: Any) {
        printLog(V, tag, objects)
    }

    fun d() {
        printLog(D, TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun d(msg: Any?) {
        printLog(D, TAG_DEFAULT, msg)
    }

    fun d(tag: String?, objects: Any?) {
        printLog(D, tag, objects)
    }

    fun log(msg: Any?) {
        printLog(D, Thread.currentThread().name, msg)
    }

    fun i() {
        printLog(I, TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    fun i(msg: Any?) {
        printLog(I, TAG_DEFAULT, msg)
    }

    fun i(tag: String?, objects: Any?) {
        printLog(I, tag, objects)
    }

    fun w() {
        printLog(W, TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    fun w(msg: Any?) {
        printLog(W, TAG_DEFAULT, msg)
    }

    fun w(tag: String?, objects: Any?) {
        printLog(W, tag, objects)
    }

    fun e() {
        printLog(E, TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun e(msg: Any?) {
        printLog(E, TAG_DEFAULT, msg)
    }

    fun e(tag: String?, objects: Any?) {
        printLog(E, tag, objects)
    }

    fun ex(e: Throwable) {
        printException(TAG_DEFAULT, null, e)
    }

    fun ex(message: String?, e: Throwable) {
        printException(TAG_DEFAULT, message, e)
    }

    fun ex(tag: String?, message: String?, e: Throwable?) {
        printException(tag, message, e)
    }

    fun a() {
        printLog(A, TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    fun a(msg: Any?) {
        printLog(A, TAG_DEFAULT, msg)
    }

    fun a(tag: String?, objects: Any) {
        printLog(A, tag, objects)
    }

    fun json(jsonFormat: String?) {
        printLog(JSON, TAG_DEFAULT, jsonFormat)
    }

    fun json(tag: String?, jsonFormat: String?) {
        printLog(JSON, tag, jsonFormat)
    }

    fun xml(xml: String?) {
        printLog(XML, TAG_DEFAULT, xml)
    }

    fun xml(tag: String?, xml: String?) {
        printLog(XML, tag, xml)
    }

    fun file(targetDirectory: File, msg: Any) {
        printFile(TAG_DEFAULT, targetDirectory, null, msg)
    }

    fun file(tag: String?, targetDirectory: File, msg: Any) {
        printFile(tag, targetDirectory, null, msg)
    }

    fun file(tag: String?, targetDirectory: File, fileName: String?, msg: Any) {
        printFile(tag, targetDirectory, fileName, msg)
    }

    fun debug() {
        printDebug(TAG_DEFAULT, DEFAULT_MESSAGE)
    }

    fun debug(msg: Any?) {
        printDebug(TAG_DEFAULT, msg)
    }

    fun debug(tag: String?, objects: Any?) {
        printDebug(tag, objects)
    }

    fun trace() {
        printStackTrace()
    }

    private fun printStackTrace() {
        if (!IS_SHOW_LOG) {
            return
        }
        val tr = Throwable()
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        val message = sw.toString()
        val traceString = message.split("\\n\\t".toRegex()).toTypedArray()
        val sb = StringBuilder()
        sb.append("\n")
        for (trace in traceString) {
            if (trace.contains("at com.socks.library.SLog") || trace.contains("at com.socks.library.KLog")) {
                continue
            }
            sb.append(trace).append("\n")
        }
        val contents = wrapperContent(STACK_TRACE_INDEX_4, null, sb.toString())
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        BaseLog.printDefault(D, tag, headString + msg)
    }

    fun printLog(type: Int, tagStr: String?, objects: Any?, index: Int? = STACK_TRACE_INDEX_5) {
        if (!IS_SHOW_LOG) {
            return
        }
        val contents = wrapperContent(index!!, tagStr, objects)
        val tag = tagStr ?: contents[0]
        val msg = contents[1]
        val headString = contents[2]
        when (type) {
            V, D, I, W, E, A -> BaseLog.printDefault(type, tag, headString + msg)
            JSON -> JsonLog.printJson(tag, msg, headString)
            XML -> XmlLog.printXml(tag, msg, headString)
        }
    }

    fun printDebug(tagStr: String?, objects: Any?, index: Int? = STACK_TRACE_INDEX_5) {
        val contents = wrapperContent(index!!, tagStr, objects)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        BaseLog.printDefault(D, tag, headString + msg)
    }

    private fun printFile(tagStr: String?, targetDirectory: File, fileName: String?, objectMsg: Any) {
        if (!IS_SHOW_LOG) {
            return
        }
        val contents = wrapperContent(STACK_TRACE_INDEX_5, tagStr, objectMsg)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        FileLog.printFile(tag, targetDirectory, fileName, headString, msg)
    }

    private fun printException(tag: String?, message: String?, e: Throwable?) {
        var tagStr = tag
        if (TextUtils.isEmpty(tagStr)) {
            if (mIsGlobalTagEmpty) {
                tagStr = TAG_DEFAULT
            } else if (!mIsGlobalTagEmpty) {
                tagStr = mGlobalTag
            }
        }
        Log.e(tagStr, if (TextUtils.isEmpty(message)) DEFAULT_MESSAGE else message, e)
//        e?.let { Firebase.crashlytics.recordException(it) }
    }

    private fun wrapperContent(stackTraceIndex: Int, tagStr: String?, objects: Any?): Array<String?> {
        var headString = ""
        var className = ""
        if (stackTraceIndex >= 0) {
            val stackTrace = Thread.currentThread().stackTrace
            val targetElement = stackTrace[stackTraceIndex]
            className = targetElement.className
            val classNameInfo = className.split("\\.".toRegex()).toTypedArray()
            if (classNameInfo.size > 0) {
                className = classNameInfo[classNameInfo.size - 1] + SUFFIX
            }
            if (className.contains("$")) {
                className = className.split("\\$".toRegex()).toTypedArray()[0] + SUFFIX
            }
            val methodName = targetElement.methodName
            var lineNumber = targetElement.lineNumber
            if (lineNumber < 0) {
                lineNumber = 0
            }
            headString = "[ ($className:$lineNumber)#$methodName ] "
        }
        var tag: String? = tagStr ?: className
        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag
        }
        val msg = objects?.let { getObjectsString(it) } ?: NULL_TIPS
        return arrayOf(tag, msg, headString)
    }


    fun printCallStack(tag: String?) {
        val ex = Throwable()
        val stackElements = ex.stackTrace
        if (stackElements != null) {
            var headString = ""
            var className = ""
            for (i in stackElements.indices) {
                val targetElement = stackElements[i]
                className = targetElement.className
                val classNameInfo = className.split("\\.".toRegex()).toTypedArray()
                if (classNameInfo.size > 0) {
                    className = classNameInfo[classNameInfo.size - 1] + SUFFIX
                }
                if (className.contains("$")) {
                    className = className.split("\\$".toRegex()).toTypedArray()[0] + SUFFIX
                }
                val methodName = targetElement.methodName
                var lineNumber = targetElement.lineNumber
                if (lineNumber < 0) {
                    lineNumber = 0
                }
                headString = "[ ($className:$lineNumber)#$methodName ] "
                e(tag, headString)
            }
        }
    }

    private fun getObjectsString(msg: Any?): String? {
        return if (msg is String) msg else gson.toJson(msg)
    }
}