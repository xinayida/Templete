package com.futuretech.common.ext

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.Job

/**
 * 作者　: hegaojian
 * 时间　: 2020/11/18
 * 描述　:
 */

/**
 * 设置防止重复点击事件
 * @param views 需要设置点击事件的view
 * @param interval 时间间隔 默认0.5秒
 * @param onClick 点击触发的方法
 */
fun setOnclickNoRepeat(vararg views: View?, interval: Long = 500, onClick: (View) -> Unit) {
    views.forEach {
        it?.clickNoRepeat(interval = interval) { view ->
            onClick.invoke(view)
        }
    }
}

/**
 * 防止重复点击事件 默认0.5秒内不可重复点击
 * @param interval 时间间隔 默认0.5秒
 * @param action 执行方法
 */
var lastClickTime = 0L
fun View.clickNoRepeat(interval: Long = 500, action: (view: View) -> Unit) {
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (lastClickTime != 0L && (currentTime - lastClickTime < interval)) {
            return@setOnClickListener
        }
        lastClickTime = currentTime
        action.invoke(it)
    }
}

/**
 * 设置点击事件
 * @param views 需要设置点击事件的view
 * @param onClick 点击触发的方法
 */
fun setOnClick(vararg views: View?, onClick: (View) -> Unit) {
    views.forEach {
        it?.setOnClickListener { view ->
            onClick.invoke(view)
        }
    }
}

private var multiLastClickTime = 0L
private var delayJob: Job? = null
private var lastClickViewID: Int? = null
private var clickCount = 0

/**
 * 连击
 * @param type 0: 单击 1: 连击
 */
@SuppressLint("ClickableViewAccessibility")
fun View.multiClick(
    interval: Long = 300L,
    onFinish: ((finalCount: Int) -> Unit)? = null,
    onClick: (x: Float, y: Float, view: View, type: Int) -> Unit,
) {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val rect = Rect()
                getGlobalVisibleRect(rect)
                val tx = event.x + rect.left
                val ty = event.y + rect.top
//                XLog.d("Stefan", "tx:$tx ty:${event.y} - ${rect.top}")
                val sameView = lastClickViewID == v.id
                lastClickViewID = v.id
                delayJob?.cancel()
                if (System.currentTimeMillis() - multiLastClickTime < interval && sameView) {
                    clickCount += 1
                    delayJob = delay(interval) {
                        onFinish?.invoke(clickCount)
                        clickCount = 0
                    }
                    onClick(tx, ty, this, 1)
                    multiLastClickTime = System.currentTimeMillis()
                    return@setOnTouchListener true
                } else {
                    clickCount = 0
                    multiLastClickTime = System.currentTimeMillis()
                    delayJob = delay(interval) {
                        onClick(tx, ty, this, 0)
                    }
                }
            }
        }
        true
    }
}


@SuppressLint("ClickableViewAccessibility")
fun View.setOnLongTouchListener(spacing: Long = 500, action: () -> Unit = {}) {
    val callback = Runnable {
        action.invoke()
    }
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.removeCallbacks(callback)
                v.postDelayed(callback, spacing)
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                v.removeCallbacks(callback)
            }
        }
        true
    }
}