package com.mxin.jdweb.ui.ql.me

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.widget.Toolbar
import com.mxin.jdweb.R
import com.mxin.jdweb.base.BaseActivity

class OpenWebActivity : BaseActivity() {
    private lateinit var webView: WebView
    private val TAG = "OpenWeb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        initView()
        initData()
    }

    override fun showLoadingView(): View? {
        return webView
    }

    override fun onLoadRetry() {
        webView.loadUrl(intent.data.toString())
    }

    private fun initData() {
        val uri = intent.data
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.GONE

        webView = findViewById(R.id.webView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webChromeClient = object: WebChromeClient(){

            override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
            ): Boolean {
                return super.onJsAlert(view, url, message, result)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if(newProgress>=95){
                    showLoadSuccess()
                }
            }
        }

        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
            ): WebResourceResponse? {
                val response = super.shouldInterceptRequest(view, request)
                return response
            }



//            private fun isAjaxRequest(request: WebResourceRequest?): Boolean {
//                return request?.url?.path?.indexOf("AJAXINTERCEPT")?:-1 > -1
//            }
        }


        var webSetting = webView.settings
        webSetting.javaScriptEnabled = true
        webSetting.useWideViewPort =true
        webSetting.loadWithOverviewMode = true
//        webSetting.cacheMode =  WebSettings.LOAD_NO_CACHE
//        webSetting.databaseEnabled = true
//        webSetting.domStorageEnabled = false

        webView.loadUrl(intent.data.toString())
        showLoading()
    }

}