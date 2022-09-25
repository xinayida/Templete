package com.futuretech.common.utils

import android.content.*
import androidx.core.content.ContextCompat
import com.futuretech.common.base.appContext

fun share(context: Context, msg: String, type: ShareType? = ShareType.MORE) {
    if (type == ShareType.COPY) {
        copyStringToClipBoard(appContext, msg)
    } else {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        intent.type = "text/plain"
        type?.let {
            intent.setPackage(getPackageName(it))
        }
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            if (e is ActivityNotFoundException) {
                showToast("App not found")
            }
        }
    }
}

enum class ShareType {
    SNAPCHAT, SMS, CONTACT, TIKTOK, MORE, COPY
}

private fun getPackageName(shareType: ShareType): String? {
    return when (shareType) {
        ShareType.SNAPCHAT -> "com.snapchat.android"
        ShareType.CONTACT -> "com.android.contacts"
        ShareType.SMS -> "com.android.mms"
        ShareType.TIKTOK -> "com.zhiliaoapp.musically"
        else -> null
    }
}

/**
 * 获取剪切板数据
 *
 * @return
 */
fun getStringFromClipBoard(context: Context): String? {
    var clipResult: String? = null
    val c = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val primaryClip = c.primaryClip
    var itemAt: ClipData.Item? = null
    if (primaryClip != null && primaryClip.itemCount > 0) {
        itemAt = primaryClip.getItemAt(0)
    }
    if (itemAt != null && itemAt.text != null) {
        val trim = itemAt.text.toString().trim { it <= ' ' }
        clipResult = trim
    }
    return clipResult
}

/**
 * 复制文本到剪切板
 *
 * @param text
 * @return
 */
fun copyStringToClipBoard(context: Context?, text: String?): Boolean {
    try {
        val c = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //            c.setPrimaryClip(ClipData.newPlainText("LiveHouse", text));
        c.text = text
        showToast("Copied successfully")
    } catch (e: Exception) {
        return false
    }
    return true
}
