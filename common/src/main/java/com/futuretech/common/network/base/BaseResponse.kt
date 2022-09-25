package com.futuretech.common.network.base

import java.io.Serializable

/**
 * Created by Stefan on 2/5/21.
 */
open class BaseResponse(
    open var code: String = "",
    open var desc: String = ""
//    open var header: Map<String, String> = mapOf()
) : Serializable {
    override fun toString(): String {
        return "code='$code', desc='$desc'"
    }
}