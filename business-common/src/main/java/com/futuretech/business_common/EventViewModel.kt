package com.futuretech.business_common

import androidx.annotation.Keep
import com.futuretech.common.base.BaseViewModel


//界面通信ViewModel
val eventVM: EventViewModel by lazy { EventViewModel() }

@Keep
class EventViewModel : BaseViewModel() {

}