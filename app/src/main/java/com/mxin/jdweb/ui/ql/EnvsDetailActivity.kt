package com.mxin.jdweb.ui.ql

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.network.ServiceGenerator
import com.mxin.jdweb.network.api.EnvsApi
import com.mxin.jdweb.network.api.SystemApi
import com.mxin.jdweb.network.data.EnvsData
import com.mxin.jdweb.utils.SpannableUtil
import com.mxin.jdweb.utils.kt.positiveBtn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class EnvsDetailActivity: QlServerSettingActivity() {

    companion object{

        //查看
        fun viewEnv(context: Context, env: EnvsData): Intent {
            return Intent(context, EnvsDetailActivity::class.java)
                .putExtra("ViewOperator", "view")
                .putExtra("ViewOperatorName", "查看")
                .putExtra("env", env)
        }

        //新增
        fun addEnv(context: Context): Intent {
            return Intent(context, EnvsDetailActivity::class.java)
                .putExtra("ViewOperator", "add")
                .putExtra("ViewOperatorName", "新增")
        }

        //修改
        fun updateEnv(context: Context, env: EnvsData, position:Int): Intent {
            return Intent(context, EnvsDetailActivity::class.java)
                .putExtra("ViewOperator", "update")
                .putExtra("ViewOperatorName", "修改")
                .putExtra("position", position)
                .putExtra("env", env)
        }

        //外面提交过来（可能新增，可能修改）
        fun outSubmitEnv(context: Context, env: EnvsData, searchValue: String): Intent {
            return Intent(context, EnvsDetailActivity::class.java)
                .putExtra("ViewOperator", "outSubmit")
                .putExtra("ViewOperatorName", "提交")
                .putExtra("searchValue", searchValue)
                .putExtra("env", env)

        }
    }

    private lateinit var contentLayout:ViewGroup
    private lateinit var env: EnvsData;
    private lateinit var toolbar: Toolbar
    private lateinit var btnSave: Button
    private val modelList = mutableListOf<FormEditModel>()
    private val spUtil by lazy { App.getInstance().spUtil }

    //当前页面操作状态
    private var viewOperator:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun initView() {
        viewOperator = intent.getStringExtra("ViewOperator")
        val viewOperatorName = intent.getStringExtra("ViewOperatorName")
        env = intent.getParcelableExtra("env")?: EnvsData(null, "", "", 0, 0F, "","","","")

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "${viewOperatorName?:""}环境变量"
        versionCode = spUtil.getString(SPConstants.QL_version)
        if(!TextUtils.isEmpty(versionCode)){
            toolbar.subtitle = "青龙版本：$versionCode"
        }

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        contentLayout = findViewById(R.id.contentLayout)

        btnSave = findViewById(R.id.btn_save)
        btnSave.visibility = View.GONE
        btnSave.setOnClickListener {
            try {
                modelList.forEach {
                    it.saveCall.invoke(it.value)
                }
                editEnv(env)
            }catch (e: Exception){
                Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show()
            }
        }
        getQLVersion()
    }

    private fun initData(){
        if("outSubmit" == viewOperator){
            val searchValue = intent.getStringExtra("searchValue")
            if(TextUtils.isEmpty(searchValue)){
                AlertDialog.Builder(this).setMessage("没有匹配到搜索关键字，无法查询是否存在重复变量，是否继续新增?")
                    .positiveBtn("继续新增"){dialog, which ->
                        initEnvView(this.env)
                        dialog.dismiss()
                    }
                    .setNegativeButton("关闭页面"){dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    .create().show()
            }else{
                searchEnv(searchValue!!)
            }
        }
        else{
            initEnvView(this.env)
        }

    }

    private fun initEnvView(env: EnvsData){
        val onlyRead = "view"==viewOperator
        btnSave.visibility = if(onlyRead) View.GONE else View.VISIBLE

        val nameModel = FormEditModel("名称", env.name)
            .save {  env.name = it}
        modelList.add(nameModel)
        contentLayout.addView(initFormEditView(nameModel, require = true, onlyRead = onlyRead))

        val valueModel = FormEditModel("值", env.value)
            .save {  env.value = it}
        modelList.add(valueModel)
        contentLayout.addView(initFormEditView(valueModel, 3, require = true, onlyRead = onlyRead))

        val remarkModel = FormEditModel("备注", env.remarks)
            .save {  env.remarks = it}
        modelList.add(remarkModel)
        contentLayout.addView(initFormEditView(remarkModel, require = false, onlyRead = onlyRead))
    }

//  接口有问题，查询接口返回的是第一个
//    private fun getEnv(id:Long){
//        lifecycleScope.launch {
//            showLoadingDialog("")
//            try{
//                val resp = ServiceGenerator.createService(EnvsApi::class.java).get(id)
//                if(resp.code == 200 && resp.data!=null){
//                    env = resp.data
//                    initEnvView(env)
//                }else if(!resp.message.isNullOrEmpty()){
//                    Toast.makeText(this@EnvsDetailActivity, resp.message, Toast.LENGTH_SHORT).show()
//                }else{
//                    Toast.makeText(this@EnvsDetailActivity, "查询失败！" , Toast.LENGTH_SHORT).show()
//                }
//            }catch (e:Exception){
//                e.printStackTrace()
//                Toast.makeText(this@EnvsDetailActivity, "查询异常！${e.message}" , Toast.LENGTH_SHORT).show()
//            }
//            dismissLoadingDialog()
//        }
//    }

    private fun editEnv(env: EnvsData){
        if(TextUtils.isEmpty(env.name)){
            Toast.makeText(this, "请输入名称！" , Toast.LENGTH_SHORT).show()
            return
        }
        if(TextUtils.isEmpty(env.value)){
            Toast.makeText(this, "请输入值！" , Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            showLoadingDialog("正在保存")
            try{
                val json = JSONObject()
                json["name"] = env.name
                json["value"] = env.value
                json["remarks"] = env.remarks

                val resp = if(env.getEId()!=null){
                    if(versionCode.compareQLVersion("2.11.0")){
                        json["id"] = env.getEId()
                    }else{
                        json["_id"] = env.getEId()
                    }
                    ServiceGenerator.createService(EnvsApi::class.java).update(json.toJSONString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                }else{
                    if(versionCode.compareQLVersion("2.10.6")){
                        val array = JSONArray()
                        array.add(json)
                        ServiceGenerator.createService(EnvsApi::class.java).add(array.toString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    }else{
                        ServiceGenerator.createService(EnvsApi::class.java).add(json.toJSONString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                    }
                }
                if(resp.code == 200){
                    setResult(Activity.RESULT_OK, intent.putExtra("env", env))
                    Toast.makeText(this@EnvsDetailActivity, "保存成功！" , Toast.LENGTH_SHORT).show()
                    finish()
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(this@EnvsDetailActivity, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@EnvsDetailActivity, "保存失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@EnvsDetailActivity, "保存异常！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

    private fun searchEnv(searchValue:String){
        lifecycleScope.launch {
            showLoadingDialog("正在查询环境变量")
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).list(searchValue)
                if(resp.code == 200){
                    val list = resp.data
                    if(list?.size ==1 ){
                        val oldEnv = list[0]
                        env.setEid(oldEnv)
                        env.remarks = oldEnv.remarks
                        initEnvView(env)
                    }else{
                        if(list?.size?:0>0){
                            AlertDialog.Builder(this@EnvsDetailActivity)
                                .setMessage("关键字【${searchValue}】查询到${list?.size?:0}条，当前是新增变量，若变量添加重复，请自行删除！")
                                .setPositiveButton("确定"){dialog,_->
                                    dialog.dismiss()
                                }.create().show()
                        }
                        initEnvView(env)
                    }
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(this@EnvsDetailActivity, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@EnvsDetailActivity, "查询环境变量失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@EnvsDetailActivity, "查询环境变量失败！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

    private var versionCode :String = ""
    private fun getQLVersion(){
        lifecycleScope.launch {
            try{
                val resp = ServiceGenerator.createService(SystemApi::class.java).version()
                if(resp.code == 200){
                    resp.data?.version?.let {
                        versionCode = it
                        toolbar.subtitle = "青龙版本：$it"
                        overrideVersion(context = this@EnvsDetailActivity, it)
                        return@launch
                    }
//                    Toast.makeText(this@EnvsDetailActivity, "获取系统版本号失败！" , Toast.LENGTH_SHORT).show()
                }
//                else if(!resp.message.isNullOrEmpty()){
//                    Toast.makeText(this@EnvsDetailActivity, resp.message, Toast.LENGTH_SHORT).show()
//                }else{
//                    Toast.makeText(this@EnvsDetailActivity, "获取系统版本号失败！" , Toast.LENGTH_SHORT).show()
//                }
            }catch (e:Exception){
                e.printStackTrace()
//                Toast.makeText(this@EnvsDetailActivity, "获取系统版本号失败！${e.message}" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun overrideVersion(context: Context, version:String){
        val cacheQlVersion = spUtil.getString(SPConstants.QL_version)
        if(!TextUtils.isEmpty(version) && cacheQlVersion!= version ){
            AlertDialog.Builder(context)
                .setTitle("检测到青龙版本号")
                .setMessage("当前缓存的版本号：$cacheQlVersion, 检测系统的版本号：$version\n是否覆盖缓存的版本号？")
                .setPositiveButton(SpannableUtil.formatForeground("覆盖", Color.RED)){ dialog, _ ->
                    spUtil.put(SPConstants.QL_version, version)
                    dialog.dismiss()
                }
                .setNegativeButton(SpannableUtil.formatForeground("取消", Color.GRAY)){ dialog, _ ->
                    dialog.dismiss()
                }
        }

    }

}