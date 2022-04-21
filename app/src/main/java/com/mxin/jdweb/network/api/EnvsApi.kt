package com.mxin.jdweb.network.api;

import com.mxin.jdweb.network.data.BaseResponse
import com.mxin.jdweb.network.data.EnvsData
import okhttp3.RequestBody
import retrofit2.http.*

interface EnvsApi {

    //查询环境变量列表
    @GET("api/envs")
    suspend fun list(@Query("searchValue")searchValue:String=""): BaseResponse<List<EnvsData>>


    //获取环境变量
    @GET("api/envs/{id}")
    suspend fun get(@Path("id")id:Long): BaseResponse<EnvsData?>


    //新增环境变量
    @POST("api/envs")
    suspend fun add(@Body body:RequestBody): BaseResponse<List<EnvsData>>


    //修改环境变量
    @PUT("api/envs")
    suspend fun update(@Body body:RequestBody): BaseResponse<EnvsData>


    //禁用环境变量
    @PUT("api/envs/disable")
    suspend fun disable(@Body body:RequestBody): BaseResponse<Any>


    //启用环境变量
    @PUT("api/envs/enable")
    suspend fun enable(@Body body:RequestBody): BaseResponse<Any>

    //删除环境变量
    @HTTP(method = "DELETE", path = "api/envs", hasBody = true)
    suspend fun delete(@Body body:RequestBody): BaseResponse<Any>
}
