package com.mxin.jdweb.network.data

//{"code":200,"data":{"token":"fd1080eb-00c8-4a23-a98d-7abca29b9a56","token_type":"Bearer","expiration":1652944146}}
data class AuthTokenData(
    val token:String,
    val token_type:String,
    val expiration:Long
)
