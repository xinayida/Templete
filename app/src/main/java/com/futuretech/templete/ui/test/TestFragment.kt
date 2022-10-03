package com.futuretech.templete.ui.test

import android.os.Bundle
import com.futuretech.common.base.BaseViewModel
import com.futuretech.common.base.BaseVmFragment
import com.futuretech.common.event.EventBus
import com.futuretech.common.ext.navBack
import com.futuretech.common.utils.DataStoreUtils
import com.futuretech.common.utils.XLog
import com.futuretech.templete.databinding.FragmentTestBinding

class TestFragment : BaseVmFragment<FragmentTestBinding, TestVM>() {
    override fun initView(savedInstanceState: Bundle?) {
        viewBinding.Btn.setOnClickListener {
//            EventBus.invokeEvent("Test", "456")
            DataStoreUtils.putSyncData("Long", 100L)
            navBack()
        }
    }
}

class TestVM : BaseViewModel() {

}