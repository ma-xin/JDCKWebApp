package com.mxin.jdweb.ui.setting

import android.app.Activity
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.common.Constants
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.ui.ql.FormEditModel
import com.mxin.jdweb.ui.ql.QlServerSettingActivity
import com.mxin.jdweb.utils.kt.dp2px
import com.mxin.jdweb.utils.kt.toColorInt


open class WebSettingActivity: QlServerSettingActivity() {


    private val spUtil by lazy { App.getInstance().spUtil }

    override fun initSettingView(contentLayout:ViewGroup, modelList: MutableList<FormEditModel>){
        val url = spUtil.getString(SPConstants.Web_home_url, Constants.WebView_Home_Url_Default)
        val domainMode = FormEditModel("默认打开地址,示例: ${Constants.WebView_Home_Url_Default}", url)
            .save {
                spUtil.put(SPConstants.Web_home_url, it)
                setResult(Activity.RESULT_OK)
            }
        modelList.add(domainMode)
        contentLayout.addView(initFormEditView(domainMode, lines = 2))

        contentLayout.addView(getSpaceView(20.dp2px(), android.R.color.transparent.toColorInt()))
        val textView = initFormTextView("cookie抓取的域名地址是登录成功后，抓取页面的顶级域名地址，我只知道示例的地址，其他的我不知道！", R.color.org_tag.toColorInt(), 13f)
        contentLayout.addView(textView)
        contentLayout.addView(getSpaceView(10.dp2px(), android.R.color.transparent.toColorInt()))

        val cookie_domain = spUtil.getString(SPConstants.Web_cookie_domain,  Constants.WebView_Cookie_Domain_Default)
        val clientIdMode = FormEditModel("cookie抓取的域名地址,示例: ${Constants.WebView_Cookie_Domain_Default}", cookie_domain)
            .save { spUtil.put(SPConstants.Web_cookie_domain, it) }
        modelList.add(clientIdMode)
        contentLayout.addView(initFormEditView(clientIdMode, lines = 2))



    }


}