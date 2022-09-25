package com.futuretech.common.utils.paging

data class ListWrapper<T>(
    var offset: Long,
    var list: ArrayList<T>? = null
)