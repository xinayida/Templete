package com.futuretech.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Build.VERSION
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.futuretech.common.base.appContext

/**
 *  @description:
 *  @author xcl qq:244672784
 *  @Date 2020/7/1
 **/
/************************************** 单位转换*********************************************** */
/**
 * 像素密度
 */


fun getDisplayMetrics() = appContext.resources.displayMetrics.density

/**
 * dp 转成为 px
 */
fun dp2px(dpValue: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpValue,
        appContext.resources.displayMetrics
    ).toInt()
}

val Float.dp get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, appContext.resources.displayMetrics)

val Int.dp get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), appContext.resources.displayMetrics).toInt()

/**
 * px 转成为 dp
 */
fun px2dp(pxValue: Float) = (pxValue / getDisplayMetrics() + 0.5f).toInt()

/**
 * sp转px
 */
fun sp2px(spVal: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        spVal,
        appContext.resources.displayMetrics
    ).toInt()
}

/**
 * px转sp
 */
fun px2sp(pxVal: Float) = pxVal / appContext.resources.displayMetrics.scaledDensity

/************************************** 屏幕宽高*********************************************** */


fun getScreenSize(): IntArray {
    val wm: WindowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (wm == null) {
        intArrayOf(0, 0)
    } else {
        var display: Display? = null
        try {
            display = wm.defaultDisplay
        } catch (var8: Throwable) {
        }
        val dm: DisplayMetrics
        if (display == null) {
            try {
                dm = appContext.resources.displayMetrics
                intArrayOf(dm.widthPixels, dm.heightPixels)
            } catch (var5: Throwable) {
                intArrayOf(0, 0)
            }
        } else if (VERSION.SDK_INT < 13) {
            try {
                dm = DisplayMetrics()
                display.getMetrics(dm)
                intArrayOf(dm.widthPixels, dm.heightPixels)
            } catch (var6: Throwable) {
                intArrayOf(0, 0)
            }
        } else {
            try {
                val size = Point()
                val method = display.javaClass.getMethod("getRealSize", Point::class.java)
                method.isAccessible = true
                method.invoke(display, size)
                intArrayOf(size.x, size.y)
            } catch (var7: Throwable) {
                intArrayOf(0, 0)
            }
        }
    }
}

/**
 * 获取屏幕分辨率
 */
fun getRealScreenSize(): Point? {
    var screenSize: Point? = null
    try {
        screenSize = Point()
        val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val defaultDisplay = windowManager.defaultDisplay
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            defaultDisplay.getRealSize(screenSize)
        } else {
            try {
                val mGetRawW = Display::class.java.getMethod("getRawWidth")
                val mGetRawH = Display::class.java.getMethod("getRawHeight")
                screenSize[(mGetRawW.invoke(defaultDisplay) as Int)] = (mGetRawH.invoke(defaultDisplay) as Int)
            } catch (e: java.lang.Exception) {
                screenSize[defaultDisplay.width] = defaultDisplay.height
                e.printStackTrace()
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return screenSize
}

/**
 * 获取屏幕宽
 */
fun getScreenWidth(): Int {
    val metric = DisplayMetrics()
    (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        .getMetrics(metric)
    return metric.widthPixels
}

/**
 * 获取屏幕高，包含状态栏，但不包含虚拟按键，如1920屏幕只有1794
 */
fun getScreenHeight(): Int {
    val metric = DisplayMetrics()
    (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        .getMetrics(metric)
    return metric.heightPixels
}

///**
// * 获取屏幕宽
// */
//fun getScreenWidth2(): Int {
//    val point = Point()
//    (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
//        .getSize(point)
//    return point.x
//}
///**
// * 获取屏幕高，包含状态栏，但不包含某些手机最下面的【HOME键那一栏】，如1920屏幕只有1794
// */
//fun getScreenHeight2(): Int {
//    val point = Point()
//    (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
//        .getSize(point)
//    return point.y
//}

/**
 * 获取屏幕原始尺寸高度，包括状态栏以及虚拟功能键高度
 */
fun getAllScreenHeight(): Int {
    val display =
        (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    try {
        val displayMetrics = DisplayMetrics()
        val method =
            Class.forName("android.view.Display").getMethod(
                "getRealMetrics",
                DisplayMetrics::class.java
            )
        method.invoke(display, displayMetrics)
        return displayMetrics.heightPixels
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}

/*************************** 状态栏、标题栏、虚拟按键**************************************** */

/**
 * 状态栏高度，单位px，一般为25dp
 */
fun getStatusBarHeight(): Int {
    var height = 0
    val resourceId =
        appContext.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        height = appContext.resources.getDimensionPixelSize(resourceId)
    }
    return height
}

/**
 * 状态栏高度，单位px，【注意】要在onWindowFocusChanged中获取才可以
 */
fun getStatusBarHeight2(activity: Activity): Int {
    val rect = Rect()
    //DecorView是Window中的最顶层view，可以从DecorView获取到程序显示的区域，包括标题栏，但不包括状态栏。所以状态栏的高度即为显示区域的top坐标值
    activity.window.decorView.getWindowVisibleDisplayFrame(rect)
    return rect.top
}

/**
 * 标题栏的高度，【注意】要在onWindowFocusChanged中获取才可以
 */
fun getTitleBarHeight(activity: Activity): Int {
    val contentTop =
        activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT)
            .top
    return contentTop - getStatusBarHeight()
}

/**
 * 获取 虚拟按键的高度
 */
fun getBottomBarHeight() = getAllScreenHeight() - getScreenHeight()