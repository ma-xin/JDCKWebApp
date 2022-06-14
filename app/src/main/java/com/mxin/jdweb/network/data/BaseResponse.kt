package com.mxin.jdweb.network.data

data class BaseResponse<T>(
    val code:Int,
    val data:T?,
    val message:String?,
)
