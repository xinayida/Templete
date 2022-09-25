package com.futuretech.templete.ui.main

import android.os.Bundle
import com.futuretech.common.base.BaseVmFragment
import com.futuretech.templete.databinding.FragmentMainBinding

class MainFragment : BaseVmFragment<FragmentMainBinding, MainViewModel>() {
    override fun initView(savedInstanceState: Bundle?) {

    }

//    override fun onBackPressed(): Boolean {
//        navController.backQueue.forEachIndexed { index, navBackStackEntry ->
//            XLog.d("Stefan", "$index ${navBackStackEntry.destination.displayName}  ${navBackStackEntry.destination.navigatorName}")
//        }
//        activity.finish()
//        return true
//    }

}