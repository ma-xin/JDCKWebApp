package com.mxin.jdweb.network.api;

import com.mxin.jdweb.network.data.BaseResponse
import com.mxin.jdweb.network.data.EnvsData
import com.mxin.jdweb.network.data.VersionData
import okhttp3.RequestBody
import retrofit2.http.*

interface SystemApi {

    //获取系统版本号
    @GET("api/system")
    suspend fun version(): BaseResponse<VersionData?>


}
