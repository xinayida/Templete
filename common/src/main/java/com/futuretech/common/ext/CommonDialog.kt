package com.futuretech.common.ext

import android.app.Activity
import android.app.Dialog
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.futuretech.common.R
import kotlinx.coroutines.Job


/*****************************************loading框********************************************/
private var loadingDialog: Dialog? = null

fun showLoading() {
    currentActivity?.showLoading()
}

/**
 * 打开等待框
 */
private var delayDialogJob: Job? = null
fun Activity.showLoading(message: String? = null, callback: (() -> Unit)? = null) {
    if (!this.isFinishing) {
        if (loadingDialog == null) {
            //弹出loading时 把当前界面的输入法关闭
            this.hideOffKeyboard()
            val contentView = LayoutInflater.from(this)
                .inflate(R.layout.layout_custom_progress_dialog_view, null).apply {
                    this.findViewById<TextView>(R.id.loading_tips).text = message ?: ""
                }
            val lavLoading = contentView.findViewById<LottieAnimationView>(R.id.lav_loading)
            loadingDialog = Dialog(this, R.style.loadingDialogTheme).apply {
                setCancelable(true)
                setCanceledOnTouchOutside(false)
                setContentView(contentView)
            }
            loadingDialog?.setOnDismissListener {
                lavLoading.cancelAnimation()
                loadingDialog = null
            }
            loadingDialog?.setOnKeyListener { dialog, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    callback?.invoke()
                    lavLoading.cancelAnimation()
                    dialog.dismiss()
                }
                true
            }
            try {
                loadingDialog?.show()
            } catch (e: Exception) {
                loadingDialog?.dismiss()
            }
            delayDialogJob?.cancel()
        }
    }
}

/**
 * 打开等待框
 */
fun Fragment.showLoading(message: String? = null) {
    activity?.showLoading(message)
}

/**
 * 关闭等待框
 */
fun dismissLoading() {
    try {
        loadingDialog?.dismiss()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
