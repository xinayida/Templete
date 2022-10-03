package com.futuretech.templete.ui.main

import android.os.Bundle
import com.futuretech.common.base.BaseVmFragment
import com.futuretech.common.event.EventBus.handleEvent
import com.futuretech.common.ext.clickNoRepeat
import com.futuretech.common.ext.nav
import com.futuretech.common.utils.DataStoreUtils
import com.futuretech.common.utils.XLog
import com.futuretech.templete.R
import com.futuretech.templete.databinding.FragmentMainBinding
import kotlinx.coroutines.flow.collectLatest

class MainFragment : BaseVmFragment<FragmentMainBinding, MainViewModel>() {
    override fun initView(savedInstanceState: Bundle?) {
        viewBinding.btnNext.clickNoRepeat {
            nav(R.id.testFragment)
        }
    }

    override fun initData() {
        val data = DataStoreUtils.getSyncData("Long",0L)
        XLog.d("data: $data")
        handleEvent("Test") {
            XLog.d("handle event $it")
        }
    }

//    override fun onBackPressed(): Boolean {
//        navController.backQueue.forEachIndexed { index, navBackStackEntry ->
//            XLog.d("Stefan", "$index ${navBackStackEntry.destination.displayName}  ${navBackStackEntry.destination.navigatorName}")
//        }
//        activity.finish()
//        return true
//    }

}