package com.futuretech.common.ext

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.futuretech.common.R
import com.futuretech.common.utils.XLog
import java.lang.reflect.ParameterizedType

/**
 * Activity findNavController
 */
val FragmentActivity.navController: NavController
    get() = findNavController(R.id.host_fragment)

/**
 * Fragment findNavController
 */
val Fragment.navController: NavController
    get() = runCatching { findNavController() }.getOrNull() ?: requireActivity().navController

fun FragmentActivity.nav(
    @IdRes resId: Int,
    @Nullable args: Bundle? = null,
    withAnim: Boolean? = true,
    @IdRes popToId: Int? = null,
    ic: Boolean? = false,
) {
    commonNav(navController, resId, args, withAnim, popToId, ic)
}

/**
 * Fragment 返回上一页
 */
fun Fragment.navBack(@Nullable key: String? = null, @Nullable value: String? = null) {
    try {
        key?.let { navController.previousBackStackEntry?.savedStateHandle?.set(key, value) }
        // 返回栈没有或一个 Fragment，直接结束 Activity
        if (!navController.popBackStack()) {
            activity?.finish()
        }
    } catch (e: Exception) {
        XLog.ex(e)
    }
}

fun <T> Fragment.navBack(@Nullable key: String? = null, @Nullable value: T? = null) {
    try {
        key?.let { navController.previousBackStackEntry?.savedStateHandle?.set(key, value) }
        navController.popBackStack()
    } catch (e: Exception) {
        XLog.ex(e)
    }
}


private var enterTime: Long = 0L

/**
 * @param inclusive: Whether the popUpTo destination should be popped from the back stack.
 */
fun commonNav(
    nc: NavController?,
    @IdRes resId: Int,
    @Nullable args: Bundle? = null,
    withAnim: Boolean? = true,
    @IdRes popToId: Int? = null,
    inclusive: Boolean? = false,
    singleTop: Boolean? = false,
) {
    enterTime = System.currentTimeMillis()
    nc?.navigate(resId, args, navOptions {
        launchSingleTop = singleTop!!
        if (withAnim == true) {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
        popToId?.let {
            popUpTo(it) {
                this.inclusive = inclusive!!
            }
        }
    })
}

/**
 * 跳转页面
 * @param inclusive true: 跳转时在回退栈中移除popToId页面, popToId不填时移除当前页面， false: 不移除
 */
fun Fragment.nav(
    @IdRes resId: Int,
    @Nullable args: Bundle? = null,
    withAnim: Boolean? = true,
    @IdRes popToId: Int? = null,
    inclusive: Boolean? = false,
    singleTop: Boolean? = false,
) {
    val popId = if (inclusive == true && popToId == null) {
        navController.currentBackStackEntry?.destination?.id
    } else {
        popToId
    }
    commonNav(navController, resId, args, withAnim, popId, inclusive, singleTop)
}

fun <Type> getGenericParam(index: Int, obj: Any): Type {
    var types = (obj.javaClass.genericSuperclass as? ParameterizedType)?.actualTypeArguments
    if (types.isNullOrEmpty()) {
        types = (obj.javaClass.superclass.genericSuperclass as ParameterizedType).actualTypeArguments
    }
    if (types.isNullOrEmpty()) {
        types = (obj.javaClass.superclass.superclass.genericSuperclass as ParameterizedType).actualTypeArguments
    }
    return types!![index] as Type
}