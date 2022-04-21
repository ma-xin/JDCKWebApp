package com.mxin.jdweb.ui.ql

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.base.BaseActivity
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.network.ServiceGenerator
import com.mxin.jdweb.network.api.EnvsApi
import com.mxin.jdweb.network.api.LoginApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class QLLoginActivity: BaseActivity() {

    private val spUtil by lazy { App.getInstance().spUtil }
    private var clientId:String? = null
    private var clientSecret:String? = null

    private lateinit var etUserName:EditText
    private lateinit var etPassWord:EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_ql)
        initView()
        initData()
    }

    override fun showLoadingView(): View? {
        return null
    }

    override fun onLoadRetry() {

    }

    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "青龙面板"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        etUserName = findViewById(R.id.et_username)
        etPassWord = findViewById(R.id.et_password)
        etUserName.setText(spUtil.getString(SPConstants.QL_login_username))
        etPassWord.setText(spUtil.getString(SPConstants.QL_login_password))

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            if(checkQlDomain()){
                login()
            }
        }

        findViewById<Button>(R.id.btn_auth).setOnClickListener {
            if(checkQlDomain() && checkQlClientKey()){
                startAuth()
            }
        }

        findViewById<View>(R.id.tv_setting).setOnClickListener {
            startActivity(Intent(this, QlServerSettingActivity::class.java).putExtra("title","配置青龙服务器"))
        }

    }

    private fun initData() {
        //测试缓存token是否过期
        verifyToken()
    }

    private fun checkQlDomain():Boolean{
        val qlDomain = spUtil.getString(SPConstants.QL_domain)
        return if(TextUtils.isEmpty(qlDomain) || qlDomain.contains("127.0.0.1")){
            AlertDialog.Builder(this)
                .setMessage("请配置青龙服务器IP， 是否去配置？")
                .setPositiveButton("去配置"){ dialog, _ ->
                    startActivity(Intent(this, QlServerSettingActivity::class.java))
                    dialog.dismiss()
                }.create().show()
            false
        }else{
            //初始化服务器ip
            ServiceGenerator.reset(qlDomain)
            true
        }
    }

    private fun checkQlClientKey():Boolean{
        clientId =spUtil.getString(SPConstants.QL_client_id)
        clientSecret =spUtil.getString(SPConstants.QL_client_secret)
        return if(TextUtils.isEmpty(clientId) || TextUtils.isEmpty(clientSecret)){
            AlertDialog.Builder(this)
                .setMessage("请配置青龙应用授权ID，否则无法使用授权登录， 是否去配置？")
                .setPositiveButton("去配置"){ dialog, _ ->
                    startActivity(Intent(this, QlServerSettingActivity::class.java))
                    dialog.dismiss()
                }.setNegativeButton("取消"){ dialog, _ ->
                    dialog.dismiss()
                }.create().show()
            false
        }else{
            true
        }
    }

    private fun login(){
        val username = etUserName.text.toString()
        val password = etPassWord.text.toString()
        if(TextUtils.isEmpty(username)){
            showToast("请输入账户！")
            return
        }
        if(TextUtils.isEmpty(password)){
            showToast("请输入密码！")
            return
        }
        startLogin(username, password)
    }

    private fun startLogin(userName:String, password:String){
        spUtil.put(SPConstants.QL_login_username, userName)
        spUtil.put(SPConstants.QL_login_password, password)
        lifecycleScope.launch {
            showLoadingDialog("正在登录...", false)
            val params = JSONObject()
            params.put("username", userName)
            params.put("password", password)
            try {
                val resp = ServiceGenerator.createService(LoginApi::class.java).login(params.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                if(resp.code == 200){
                    resp.data?.token?.let {
                        cacheToken(it, tokenApi = "api")
                        openNextActivity()
                        return@launch
                    }
                }else if(!TextUtils.isEmpty(resp.message)){
                    Toast.makeText(this@QLLoginActivity, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@QLLoginActivity, "登录失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@QLLoginActivity, "登录失败！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

    //发起应用授权获取token
    private fun startAuth(){
        lifecycleScope.launch {
            showLoadingDialog("正在获取应用授权...", false)
            try{
                val resp = ServiceGenerator.createService(LoginApi::class.java).authToken(clientId!!, clientSecret!!)
                if(resp.code == 200){
                    resp.data?.token?.let {
                        val tokenType = resp.data.token_type
                        cacheToken(it, tokenType, resp.data.expiration, "open")
                        openNextActivity()
                        return@launch
                    }
                    Toast.makeText(this@QLLoginActivity, "授权获取Token失败！" , Toast.LENGTH_SHORT).show()
                }else if(!TextUtils.isEmpty(resp.message)){
                    Toast.makeText(this@QLLoginActivity, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@QLLoginActivity, "授权失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(this@QLLoginActivity, "授权失败！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

    //缓存token
    private fun cacheToken(token:String, tokenType:String = "Bearer", expiration:Long = -1, tokenApi:String = "api"){
        App.getInstance().refreshToken(token, tokenType, expiration, tokenApi)
    }

    //验证缓存的token是否失效
    private fun verifyToken(){
        val token = spUtil.getString(SPConstants.QL_token)
        if(TextUtils.isEmpty(token)){
            return
        }
        showLoadingDialog("正在连接...")
        lifecycleScope.launch {
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).list()
                if(resp.code == 200){
                    openNextActivity()
                    return@launch
                }else{
                    App.getInstance().clearToken()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
            dismissLoadingDialog()
        }
    }


    private fun openNextActivity(){
        val expire = intent.getBooleanExtra("expire", false)
        if(!expire){
            startActivity(Intent(this@QLLoginActivity, QlHomeActivity::class.java))
        }
        dismissLoadingDialog()
        finish()
    }
}