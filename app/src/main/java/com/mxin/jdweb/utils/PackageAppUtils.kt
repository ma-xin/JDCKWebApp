package cn.vove7.andro_accessibility_api.demo.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

class PackageAppUtils {

    companion object{

        /**
         * 获取手机已安装应用列表
         * @param ctx
         * @param isFilterSystem 是否过滤系统应用
         * @return
         */
        fun getAllAppInfo(context: Context, isFilterSystem: Boolean):List<AppInfo>{
            val appList = mutableListOf<AppInfo>()
            val packageManager = context.packageManager
            val packageInfoList = packageManager.getInstalledPackages(0)
            packageInfoList.forEach {
                val icon = it.applicationInfo.loadIcon(packageManager)
                val label = packageManager.getApplicationLabel(it.applicationInfo).toString()
                val packageName = it.applicationInfo.packageName
                val flags = it.applicationInfo.flags
                // 判断是否是属于系统的apk
                if ((flags.and(ApplicationInfo.FLAG_SYSTEM)) != 0 && isFilterSystem) {
//                bean.setSystem(true);
                } else {
                    appList.add(AppInfo(label, packageName, icon))
                }
            }
            return appList
        }

    }

}


data class AppInfo(
//    val uid:Int,
    val label:String,
    val packageName:String,
    val icon:Drawable
)