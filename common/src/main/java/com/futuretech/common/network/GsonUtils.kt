package com.futuretech.common.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder

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