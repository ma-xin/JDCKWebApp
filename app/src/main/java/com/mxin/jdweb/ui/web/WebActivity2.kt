package com.mxin.jdweb.ui.web

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mxin.jdweb.App
import com.mxin.jdweb.BuildConfig
import com.mxin.jdweb.R
import com.mxin.jdweb.common.Constants
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.network.OKHttpUtils
import com.mxin.jdweb.network.data.EnvsData
import com.mxin.jdweb.ui.ql.EnvsDetailActivity
import com.mxin.jdweb.widget.LoadDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class WebActivity2 : AppCompatActivity() {
    private lateinit var webView: WebView
    private val TAG = "JDWeb2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        initView()
        initData()
    }

    private fun initData() {
        val uri = intent.data
        if (uri != null) {
            // 完整的url信息
            val totalUrl = uri.toString();
            Log.i(TAG, "完整url: " + totalUrl);
            // scheme 协议
            val scheme = uri.getScheme();
            Log.i(TAG, "scheme: " + scheme);
            // host 主机
            val host = uri.getHost();
            Log.i(TAG, "host: " + host);
            //port 端口
            val port = uri.getPort();
            Log.i(TAG, "port: " + port);
            // 访问路径
            val path = uri.getPath();
            Log.i(TAG, "path: " + path);
            // 获取所有参数
            val query = uri.getQuery();
            Log.i(TAG, "query: " + query);
            //获取指定参数值
            val device = uri.getQueryParameter("device");
            Log.i(TAG, "device: " + device);
            val tap = uri.getQueryParameter("tap");
            Log.i(TAG, "tap: " + tap);
            //为了方便的获取整个链接
            val url = uri.toString().replace("jdckweb://","").split("\\?")[0];
            Log.i(TAG, "url: " + url);
        }
    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "JDCOOKIE2"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_power_settings_new_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

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

        AlertDialog.Builder(this)
            .setTitle("温馨提示")
            .setMessage("登录成功后，请点击右上角的上传图标，上传登录账户的cookie凭证！")
            .setPositiveButton("知道了"){ dialog, _ ->
                dialog.dismiss()
            }.create().show()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.web_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.item_acion){
            handleCookie(getWebCookie())
        }
        return super.onOptionsItemSelected(item)
    }

    fun getWebCookie(): String {
        CookieManager.getInstance().run {
            val cookieUrl = App.getInstance().spUtil.getString(SPConstants.Web_cookie_domain, Constants.WebView_Cookie_Domain_Default)
            val cookie = getCookie(cookieUrl)
            Log.d(TAG, "cookie : $cookie")
            return cookie
        }
    }

    //获取cookie中的pt_key 和 pt_pin
    private fun handleCookie(cookie: String) {
        val cookieValues = cookie.split(";")
        var result = ""
        cookieValues.forEach {
            val value = it.trim()
            if (value.startsWith("pt_key=") || value.startsWith("pt_pin=")) {
                result += "$value;"
            }
        }
        if (TextUtils.isEmpty(result)) {
            AlertDialog.Builder(this)
                .setMessage("没有获取到cookie的pt_key和pt_pin，请确认登录后重试！")
                .setPositiveButton("知道了") { dialog, _ ->
                    dialog.dismiss()
                }.setNegativeButton("查看cookie") { dialog, _ ->
                    startActivity(Intent(this, TextActivity::class.java).putExtra("content", cookie))
                }.create().show()
        } else {
            //将cookie提交到服务器
            submit(result)
        }
    }

    private val loadDialog: LoadDialog by lazy { LoadDialog(this) }


    private val spUtil by lazy { App.getInstance().spUtil }
    private fun submit(cookie: String){
        val qlDomain = spUtil.getString(SPConstants.QL_domain)
        if(TextUtils.isEmpty(qlDomain)){
            submitGiteeIssueComment(cookie)
        }
        else{
            submitQLServer(cookie)
        }
    }

    private fun submitGiteeIssueComment(cookie: String) {
        val access_token = spUtil.getString(SPConstants.GitEE_Token, BuildConfig.gitee_token)
        val owner = spUtil.getString(SPConstants.GitEE_owner, BuildConfig.gitee_owner)
        val repo = spUtil.getString(SPConstants.GitEE_repo, BuildConfig.gitee_repo)
        val number = spUtil.getString(SPConstants.GitEE_number, BuildConfig.gitee_issue)
        if(TextUtils.isEmpty(access_token) || TextUtils.isEmpty(owner) || TextUtils.isEmpty(repo) || TextUtils.isEmpty(number)){
            AlertDialog.Builder(this)
                .setMessage("没有获取到GitEE配置参数，请确认后重试！\n如果需要直接提交到公网IP的青龙面板，请在首页配置青龙服务器！")
                .setPositiveButton("知道了") { dialog, _ ->
                    dialog.dismiss()
                }.setNegativeButton("查看cookie") { dialog, _ ->
                    startActivity(Intent(this, TextActivity::class.java).putExtra("content", cookie))
                }.create().show()
            return
        }
        var url = "https://gitee.com/api/v5/repos/{owner}/{repo}/issues/{number}/comments"
        url = url.replace("{owner}", owner).replace("{repo}", repo).replace("{number}", number)
        val params = mapOf("access_token" to access_token, "body" to cookie)
        loadDialog.show()
        OKHttpUtils.doPostRequest(url, params, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                webView.post {
                    loadDialog.dismiss()
                    AlertDialog.Builder(this@WebActivity2)
                        .setTitle("cookie提交GitEE异常！")
                        .setMessage("${e.message}")
                        .setPositiveButton("关闭") { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                webView.post {
                    loadDialog.dismiss()
                    val responseBody = response.body?.string()
                    try {
                        if(!response.isSuccessful){
                            AlertDialog.Builder(this@WebActivity2)
                                .setTitle("cookie提交GitEE失败！")
                                .setMessage("确认gitee配置参数和仓库Issue是否正常！\nresponse Code:${response.code}\n" +
                                        "body:${responseBody}")
                                .setPositiveButton("关闭") { dialog, _ ->
                                    dialog.dismiss()
                                }.create().show()
                            return@post
                        }
                        val json = JSONObject(responseBody)
                        val id = json.opt("id")
                        val body = json.opt("body")
                        if (id != null && body != null) {
                            AlertDialog.Builder(this@WebActivity2)
                                .setTitle("cookie提交GitEE成功！")
                                .setMessage("请勿切换账号或点击设置中的退出登录，否则会导致当前cookie失效\nid:$id \nbody:$body")
                                .setPositiveButton("关闭") { dialog, _ ->
                                    dialog.dismiss()
                                }.create().show()
                        } else {
                            AlertDialog.Builder(this@WebActivity2)
                                .setTitle("cookie提交GitEE失败！")
                                .setMessage("$responseBody")
                                .setPositiveButton("关闭") { dialog, _ ->
                                    dialog.dismiss()
                                }.create().show()
                        }
                    } catch (e: Exception) {
                        AlertDialog.Builder(this@WebActivity2)
                            .setTitle("cookie提交GitEE异常！")
                            .setMessage("${e.message}")
                            .setPositiveButton("关闭") { dialog, _ ->
                                dialog.dismiss()
                            }.create().show()
                    }
                }
            }
        })
    }

    private fun submitQLServer(cookie:String){
        val env = EnvsData(null, cookie, "", 0, 0f, "JD_COOKIE", "", "","")
        var pt_pin = ""
        env.value?.split(";")?.forEach {
            if(it.startsWith("pt_pin=")){
                pt_pin = it
                return@forEach
            }
        }
        startActivity(EnvsDetailActivity.outSubmitEnv(this, env, pt_pin))
    }


}