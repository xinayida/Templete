package com.futuretech.common.event

import androidx.lifecycle.MutableLiveData
import com.futuretech.common.base.BaseViewModel

class AppVM: BaseViewModel() {
    val appShowOrHide = MutableLiveData<Boolean>()//app前后台切换
}