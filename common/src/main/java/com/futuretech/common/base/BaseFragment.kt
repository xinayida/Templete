package com.futuretech.common.base

import android.animation.Animator
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.futuretech.common.R
import com.futuretech.common.ext.dismissLoading
import com.futuretech.common.ext.navBack
import com.futuretech.common.ext.showLoading
import com.futuretech.common.helper.FragmentAnimationHelper
import com.futuretech.common.utils.XLog
import com.gyf.immersionbar.components.SimpleImmersionOwner
import com.gyf.immersionbar.components.SimpleImmersionProxy
import com.gyf.immersionbar.ktx.immersionBar


abstract class BaseFragment : Fragment(), SimpleImmersionOwner {
    private var enterTime = 0L

    /**
     * 初始化view操作
     */
    protected abstract fun initView(savedInstanceState: Bundle?)
    protected abstract fun getVM(): BaseViewModel
    protected abstract fun getRootView(): View?

    /**
     * 是否可以实现沉浸式，当为true的时候才可以执行initImmersionBar方法
     * Immersion bar enabled boolean.
     *
     * @return the boolean
     */
    override fun immersionBarEnabled(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return getRootView()
    }


    //父类activity
    lateinit var activity: AppCompatActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as AppCompatActivity
    }

    override fun initImmersionBar() {
        immersionBar {
            statusBarDarkFont(false)
        }
    }

    private val mSimpleImmersionProxy = SimpleImmersionProxy(this)

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mSimpleImmersionProxy.isUserVisibleHint = isVisibleToUser
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSimpleImmersionProxy.onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mSimpleImmersionProxy.onDestroy()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mSimpleImmersionProxy.onHiddenChanged(hidden)
    }


    private fun initBackBtn() {
        val navBackBtn = view?.findViewById<View>(R.id.nav_back)
        navBackBtn?.setOnClickListener {
            navBackImpl()
        }
    }

    /**
     * 初始化页面数据
     *
     * 该方法仅会执行一次
     */
    open fun initData() {}

    /**
     * 用于抽象子Fragment扩展
     *
     * 该方法仅会执行一次
     */
    open fun initDataExt() {}

    open fun initViewExt() {}

    private var pageStartTS: Long = 0
    override fun onResume() {
        super.onResume()
        pageStartTS = System.currentTimeMillis()
        onVisible()
    }

    override fun onPause() {
        super.onPause()
        if (requireActivity().currentFocus != null) {
            val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
    }

    open fun getEventPageName(): String {
        return javaClass.simpleName
    }

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && !getVM().loadingFinished) {
            view?.post {
                initData()
                initDataExt()
                getVM().loadingFinished = true
            }
        } else {
            onVisibleAgain()
            onResult(findNavController().currentBackStackEntry?.savedStateHandle)
        }

    }

    protected open fun onVisibleAgain() {}
    protected open fun onResult(savedStateHandle: SavedStateHandle?) {}

    /**
     * 注册 UI 事件
     */
    private fun initLoadingUiChange() {
        getVM().loadingChange.run {
            loading.observe(viewLifecycleOwner) {
                if (it.loadingType == LoadingType.LOADING_DIALOG) {
                    if (it.isShow) {
                        showLoading(it.loadingMessage)
                    } else {
                        dismissLoading()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTime = System.currentTimeMillis()
        initBackBtn()
        initView(savedInstanceState)
        initLoadingUiChange()
        initViewExt()

        requireActivity().onBackPressedDispatcher.addCallback(owner = this) {
            if (System.currentTimeMillis() - enterTime < 500) {
                XLog.d("Stefan", "return too fast")
            } else {
                navBackImpl()
            }
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter.not()) {
            return null
        }
        return FragmentAnimationHelper.createAnimation(requireActivity(), nextAnim,
            onStart = {
                onAnimationStart()
            },
            onEnd = {
                onAnimationEnd()
            })?.animation
    }

    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        if (enter.not()) {
            return null
        }
        return FragmentAnimationHelper.createAnimation(requireActivity(), nextAnim,
            onStart = {
                onAnimationStart()
            },
            onEnd = {
                onAnimationEnd()
            })?.animator
    }

    /**
     * Fragment 动画开始
     */
    protected open fun onAnimationStart() {}

    /**
     * Fragment 动画完成
     */
    protected open  fun onAnimationEnd() {}

    /**
     * 返回操作实现
     */
    fun navBackImpl() {
        val backPressed = onBackPressed()
        XLog.e("onBackPressed：${javaClass.simpleName} 是否拦截：$backPressed")

        if (backPressed.not()) {
            navBack()
        }
    }

    /**
     * Navigation 框架下的返回键拦截，不支持 ViewPager 内嵌的 Fragment
     *
     * @return 子类是否消耗了返回键事件
     */
    protected open fun onBackPressed(): Boolean {
        return false
    }
}