package com.futuretech.common.base

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.futuretech.common.R
import com.futuretech.common.ext.dismissLoading
import com.futuretech.common.ext.getColorExt
import com.futuretech.common.ext.getGenericParam
import com.futuretech.common.ext.showLoading
import com.futuretech.common.utils.XLog
import com.gyf.immersionbar.ImmersionBar
import com.gyf.immersionbar.ktx.immersionBar
import kotlinx.coroutines.Job
import java.io.File
import java.lang.reflect.Method


/**
 * ViewModelActivity 基类
 */
abstract class BaseVmActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity() {

    //toolbar 这个可替换成自己想要的标题栏
    var statusBar: View? = null

    //当前Activity绑定的 ViewModel
    lateinit var viewModel: VM
    lateinit var viewBinding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (statusBarId() != 0) {
            val toolbar = findViewById<View>(statusBarId())
            if (toolbar != null) {
                statusBar = toolbar
            }
        }
        //生成ViewBinding
        viewBinding = createViewBinding()
        setContentView(viewBinding.root)
        //生成ViewModel
        viewModel = createViewModel()
        viewModel.loadingChange.loading.observe(this) {
            if (it.isShow) {
                showLoading(it.loadingMessage)
            } else {
                dismissLoading()
            }
        }
        viewModel.onViewCreate(intent.extras ?: Bundle.EMPTY)
        //初始化 status View
        initStatusView(savedInstanceState)
        //注册界面响应事件
        initLoadingUiChange()
        //初始化绑定observer
        initObserver()
    }

    open fun initStatusView(savedInstanceState: Bundle?) {
        statusBar?.run {
            setBackgroundColor(getColorExt(R.color.colorBackGround))
            //是否隐藏标题栏
            isVisible = showToolBar()
        }
        initImmersionBar()
        viewBinding.root.post {
            initView(savedInstanceState)
        }
    }

    protected open fun statusBarId(): Int {
        return R.id.status_bar
    }

    //        ImmersionBar.with(this)
//            .transparentStatusBar()  //透明状态栏，不写默认透明色
//            .transparentNavigationBar()  //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
//            .transparentBar()             //透明状态栏和导航栏，不写默认状态栏为透明色，导航栏为黑色（设置此方法，fullScreen()方法自动为true）
//            .statusBarColor(R.color.colorPrimary)     //状态栏颜色，不写默认透明色
//            .navigationBarColor(R.color.colorPrimary) //导航栏颜色，不写默认黑色
//            .barColor(R.color.colorPrimary)  //同时自定义状态栏和导航栏颜色，不写默认状态栏为透明色，导航栏为黑色
//            .statusBarAlpha(0.3f)  //状态栏透明度，不写默认0.0f
//            .navigationBarAlpha(0.4f)  //导航栏透明度，不写默认0.0F
//            .barAlpha(0.3f)  //状态栏和导航栏透明度，不写默认0.0f
//            .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
//            .navigationBarDarkIcon(true) //导航栏图标是深色，不写默认为亮色
//            .autoDarkModeEnable(true) //自动状态栏字体和导航栏图标变色，必须指定状态栏颜色和导航栏颜色才可以自动变色哦
//            .autoStatusBarDarkModeEnable(true,0.2f) //自动状态栏字体变色，必须指定状态栏颜色才可以自动变色哦
//            .autoNavigationBarDarkModeEnable(true,0.2f) //自动导航栏图标变色，必须指定导航栏颜色才可以自动变色哦
//            .flymeOSStatusBarFontColor(R.color.btn3)  //修改flyme OS状态栏字体颜色
//            .fullScreen(true)      //有导航栏的情况下，activity全屏显示，也就是activity最下面被导航栏覆盖，不写默认非全屏
//            .hideBar(BarHide.FLAG_HIDE_BAR)  //隐藏状态栏或导航栏或两者，不写默认不隐藏
//            .addViewSupportTransformColor(toolbar)  //设置支持view变色，可以添加多个view，不指定颜色，默认和状态栏同色，还有两个重载方法
//            .titleBar(view)    //解决状态栏和布局重叠问题，任选其一
//            .titleBarMarginTop(view)     //解决状态栏和布局重叠问题，任选其一
//            .statusBarView(view)  //解决状态栏和布局重叠问题，任选其一
//            .fitsSystemWindows(true)    //解决状态栏和布局重叠问题，任选其一，默认为false，当为true时一定要指定statusBarColor()，不然状态栏为透明色，还有一些重载方法
//            .supportActionBar(true) //支持ActionBar使用
//            .statusBarColorTransform(R.color.orange)  //状态栏变色后的颜色
//            .navigationBarColorTransform(R.color.orange) //导航栏变色后的颜色
//            .barColorTransform(R.color.orange)  //状态栏和导航栏变色后的颜色
//            .removeSupportView(toolbar)  //移除指定view支持
//            .removeSupportAllView() //移除全部view支持
//            .navigationBarEnable(true)   //是否可以修改导航栏颜色，默认为true
//            .navigationBarWithKitkatEnable(true)  //是否可以修改安卓4.4和emui3.x手机导航栏颜色，默认为true
//            .navigationBarWithEMUI3Enable(true) //是否可以修改emui3.x手机导航栏颜色，默认为true
//            .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
//            .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)  //单独指定软键盘模式
//            .setOnKeyboardListener(new OnKeyboardListener() {    //软键盘监听回调，keyboardEnable为true才会回调此方法
//                @Override
//                public void onKeyboardChange(boolean isPopup, int keyboardHeight) {
//                    LogUtils.e(isPopup);  //isPopup为true，软键盘弹出，为false，软键盘关闭
//                }
//            })
//            .setOnNavigationBarListener(onNavigationBarListener) //导航栏显示隐藏监听，目前只支持华为和小米手机
//            .setOnBarListener(OnBarListener) //第一次调用和横竖屏切换都会触发，可以用来做刘海屏遮挡布局控件的问题
//            .addTag("tag")  //给以上设置的参数打标记
//            .getTag("tag")  //根据tag获得沉浸式参数
//            .reset()  //重置所有沉浸式参数
//            .init();  //必须调用方可应用以上所配置的参数
    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected open fun initImmersionBar() {
        //设置共同沉浸式样式
//        if (showToolBar()) {
//            mToolbar?.let {
//                setSupportActionBar(it)
//                it.setBackgroundColor(getColorExt(R.color.colorPrimary))
//                ImmersionBar.with(this).titleBar(it).init()
//            }
//        }
        immersionBar {
            if (statusBar != null) {
                statusBarView(statusBar)
                statusBar!!.setBackgroundColor(getColorExt(R.color.colorPrimary))
                navigationBarEnable(false)
//                hideBar(BarHide.FLAG_HIDE_BAR)
            } else {
//                hideBar(BarHide.FLAG_HIDE_BAR)
//                fitsSystemWindows(true)
                statusBarColor(R.color.background)
                autoStatusBarDarkModeEnable(true, 0.2f)
//                fullScreen(true)
                statusBarDarkFont(true, getStatusBarAlpha())
                keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)  //单独指定软键盘模式
            }

//            keyboardEnable(true)
        }
    }

    protected fun getStatusBarAlpha(): Float {
        var statusAlpha = 0f
        if (!ImmersionBar.isSupportStatusBarDarkFont()) {
            statusAlpha = 0.5f
        }
        return statusAlpha
    }

    /**
     * 初始化view
     */
    open fun initView(savedInstanceState: Bundle?) {}

    /**
     * 创建观察者
     */
    open fun initObserver() {}

    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getGenericParam(1, this))
    }

    private fun createViewBinding(): VB {
        val clazz: Class<*> = getGenericParam(0, this)
        val inflate: Method = clazz.getMethod("inflate", LayoutInflater::class.java)
        return inflate.invoke(null, layoutInflater) as VB
    }

    /**
     * 是否隐藏 标题栏 默认显示
     */
    open fun showToolBar(): Boolean {
        return true
    }


    /**
     * 注册 UI 事件
     */
    private fun initLoadingUiChange() {
        viewModel.loadingChange.run {
            loading.observe(this@BaseVmActivity) {
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

    private var takePhotoPath: File? = null

    private var localUri: Uri? = null//本地路径
    private var uploadJob: Job? = null//上傳任務

    /**
     * 初始化获取图片参数
     */
    private fun initTakePicParams() {
        takePhotoPath = null
        localUri = null
    }

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.cacheDir?.let {
            File(it, "livepic").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    private fun getOutputDirectory(context: Context, userName: String): File {
        val dir = getOutputDirectory(context)
        return File(dir, userName).apply { mkdir() }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onViewDestroy()
    }
}