package com.futuretech.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.futuretech.common.ext.getGenericParam
import java.lang.reflect.Method

/**
 * ViewModelFragment 基类
 */
abstract class BaseVmFragment<VB : ViewBinding, VM : BaseViewModel> : BaseFragment() {
    protected var rootViewBinding: VB? = null

    //当前Fragment绑定的泛型类ViewModel
    lateinit var viewModel: VM

    // 仅在 onCreateView 和 onDestroyView 之间有效
    val viewBinding: VB
        get() = rootViewBinding!!

    val isViewInitialized: Boolean
        get() = rootViewBinding != null

    // 该方法用来替代viewBinding属性
    inline fun viewHolder(action: VB.() -> Unit) {
        if (!isViewInitialized) {
            return
        }
        viewBinding.apply(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        try {
            rootViewBinding = createViewBinding()
        } catch (e: Exception) {
//            Firebase.crashlytics.log("onCreateView rootViewBinding create error: ${e.message}")
//            Firebase.crashlytics.recordException(e)
            e.printStackTrace()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 下面四个方法顺序不要调换
        viewModel = createViewModel()
        arguments?.let { initArguments(it) }
        initViewObserver()
        viewModel.onViewCreate(arguments ?: Bundle.EMPTY)

        super.onViewCreated(view, savedInstanceState)
        viewBinding.initViewListener()
    }

    private fun createViewBinding(): VB {
        val clazz: Class<*> = getGenericParam(0, this)
        val inflate: Method = clazz.getMethod("inflate", LayoutInflater::class.java)
        return inflate.invoke(null, layoutInflater) as VB
    }

    /**
     * 创建viewModel
     */
    protected open fun createViewModel(): VM {
        val clazz: Class<*> = getGenericParam(1, this)
        return if (IShareViewModel::class.java.isAssignableFrom(clazz)) {
            ViewModelProvider(requireActivity()).get(getGenericParam(1, this))
        } else {
            ViewModelProvider(this).get(getGenericParam(1, this))
        }
    }

    protected open fun initArguments(arguments: Bundle) {}
    protected open fun VB.initViewListener() {}

    /**
     * LiveData 绑定 viewLifecycleOwner
     * 注意，在 onDestroyView 会解绑
     */
    protected open fun initViewObserver() {}

    override fun getVM(): BaseViewModel {
        return viewModel
    }

    override fun getRootView(): View {
        return viewBinding.root
    }

    override fun onDestroyView() {
        viewModel.onViewDestroy()
        super.onDestroyView()
        rootViewBinding = null
    }

}

