@file:Suppress("MemberVisibilityCanBePrivate")

package com.futuretech.common.utils

import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

val String.md5: String
    get() = MD5Utils.getStringMD5(this)

object MD5Utils {

    fun getStringMD5(value: String?): String {
        if (value.isNullOrBlank()) {
            return ""
        }
        return getMD5(value.toByteArray(StandardCharsets.UTF_8))
    }

    fun getMD5(source: ByteArray?): String {
        val md5: MessageDigest
        return try {
            md5 = MessageDigest.getInstance("MD5")
            val digest = md5.digest(source ?: return "")
            String.format("%032x", BigInteger(1, digest))
        } catch (e: NoSuchAlgorithmException) {
            ""
        }
    }


    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */
    fun getFileMD5(file: File): String {
        if (!file.isFile) {
            return ""
        }
        var digest: MessageDigest? = null
        var `in`: FileInputStream? = null
        val buffer = ByteArray(1024)
        var len: Int
        try {
            digest = MessageDigest.getInstance("MD5")
            `in` = FileInputStream(file)
            while (`in`.read(buffer, 0, 1024).also { len = it } != -1) {
                digest.update(buffer, 0, len)
            }
            `in`.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
        val bigInt = BigInteger(1, digest.digest())
        return bigInt.toString(16)
    }

}