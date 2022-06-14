package com.mxin.jdweb.network.data

//{"token":"eyJhbGciOiJIUzM4NCIsInR5cCI6IkpXVCJ9.eyJkYXRhIjoiX1dfdmtXZml5a1N2MlhLQ3ZwczNlNkJZbXlxelVMc01ua3RKcGV2QVY3QTlidDgyQmp5emxwTFBRUzFIb2xCUEhaTDFhMFdsS045SnB6WlNSYTI5YWI4TUR2UkpscCIsImlhdCI6MTY1MDMzMDI3MSwiZXhwIjoxNjUwNTg5NDcxfQ.Xt0Caxt9VHFyxsF4iCuCh6dMPMYH-0fzJp8jBLPscFEa9gXctWeu2kt2kf_evrt8",
// "lastip":" 183.11.69.121","lastaddr":" 广东省深圳市 | 电信","lastlogon":1650329541558,"retries":0,"platform":"desktop"}
data class LoginData(
    val token:String?,
    val lastip:String?,
    val lastaddr:String?,
    val lastlogon:Long?,
    val retries:Int?,
    val platform:String?

)
