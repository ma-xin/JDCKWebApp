package com.mxin.jdweb.ui.ql

//对比青龙的版本号大小
fun String.compareQLVersion(version:String):Boolean{
    val nvs = split(".")
    val ovs = version.split(".")
    repeat(kotlin.math.max(nvs.size, ovs.size)){
        var nv = nvs.getOrNull(it)?:"0"
        var ov = ovs.getOrNull(it)?:"0"
        if(nv.length != ov.length){
            val maxLength =  kotlin.math.max(nv.length, ov.length)
            nv.apply { if(nv.length < maxLength) repeat(maxLength-nv.length){ nv="0$nv"}  }
            ov.apply { if(ov.length < maxLength) repeat(maxLength-ov.length){ ov="0$ov"}  }
        }
        if( nv == ov ){
            return@repeat
        }
        return nv > ov
    }
    return true
}

