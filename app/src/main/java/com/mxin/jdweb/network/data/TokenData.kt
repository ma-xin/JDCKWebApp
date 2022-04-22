package com.mxin.jdweb.network.data

//{"code":200,"data":{"token":"fd1080eb-00c8-4a23-a98d-7abca29b9a56","token_type":"Bearer","expiration":1652944146}}
data class TokenData(
    val token:String,
    val token_type:String = "Bearer",
    val expiration:Long,
    // 账户登录 127.0.0.1:5700/api/....
    // 应用授权 127.0.0.1:5700/open/....
    var api:String = "api"
){

    val timestamp = System.currentTimeMillis()

}
