package com.mxin.jdweb.ui.ql

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.alibaba.fastjson.JSONObject
import com.mxin.jdweb.R
import com.mxin.jdweb.network.ServiceGenerator
import com.mxin.jdweb.network.api.EnvsApi
import com.mxin.jdweb.network.api.SystemApi
import com.mxin.jdweb.network.data.EnvsData
import com.mxin.jdweb.utils.kt.positiveBtn
import com.scwang.smartrefresh.layout.constant.RefreshState
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class EnvsDetailActivity: QlServerSettingActivity() {

    private lateinit var contentLayout:ViewGroup
    private var env: EnvsData = EnvsData(-1L, "", "", 0, 0F, "","","","")
    private lateinit var toolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun initView() {
        val position = intent.getIntExtra("position", -1)
        val id = intent.getLongExtra("id", -1)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = if(position==-1 && id == -1L) "新增环境变量" else if(position==-1) "查看环境变量" else "修改环境变量"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        contentLayout = findViewById(R.id.contentLayout)

        val btnSave = findViewById<Button>(R.id.btn_save)
        if(position==-1 && id != -1L){
            btnSave.visibility = View.GONE
        }else{
            btnSave.setOnClickListener {
                try {
                    editEnv(env)
                    Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show()
                }catch (e: Exception){
                    Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initData(){
        val id = intent.getLongExtra("id", -1)
        val env = intent.getParcelableExtra<EnvsData>("env")
        if(env !=null && env.getEId() == -1L){
            val searchValue = intent.getStringExtra("searchValue")
            if(TextUtils.isEmpty(searchValue)){
                AlertDialog.Builder(this).setMessage("没有匹配到搜索关键字，无法查询是否存在重复变量，是否继续新增?")
                    .positiveBtn("继续新增"){dialog, which ->
                        dialog.dismiss()
                    }
                    .setNegativeButton("关闭页面"){dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    .create().show()
            }else{
                this.env = env
                searchEnv(searchValue!!)
            }
        }
        else if(id != -1L){
            getEnv(id)
        }else{
            initEnvView(this.env)
        }
    }

    private fun initEnvView(env: EnvsData){
        val position = intent.getIntExtra("position", -1)
        val id = intent.getLongExtra("id", -1)

        val nameModel = FormEditModel("名称", env.name)
            .save {  env.name = it}
        contentLayout.addView(initFormEditView(nameModel, require = true, onlyRead = (position==-1 && id != -1L)))

        val valueModel = FormEditModel("值", env.name)
            .save {  env.value = it}
        contentLayout.addView(initFormEditView(valueModel, 3, require = true, onlyRead = (position==-1 && id != -1L)))

        val remarkModel = FormEditModel("备注", env.name)
            .save {  env.remarks = it}
        contentLayout.addView(initFormEditView(remarkModel, require = false, onlyRead = (position==-1 && id != -1L)))
    }


    private fun getEnv(id:Long){
        lifecycleScope.launch {
            showLoadingDialog("")
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).get(id)
                if(resp.code == 200 && resp.data!=null){
                    env = resp.data
                    initEnvView(env)
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(this@EnvsDetailActivity, resp.message, Toast.LENGTH_SHORT).show()
                }else{

                    Toast.makeText(this@EnvsDetailActivity, "查询失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@EnvsDetailActivity, "查询异常！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

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
                json.put("name",env.name)
                json.put("value",env.value)
                json.put("remarks",env.remarks)

                val resp = if(env.getEId()!=-1L){
                    json.put("id",env.getEId())
                    ServiceGenerator.createService(EnvsApi::class.java).update(json.toJSONString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                }else{
                    ServiceGenerator.createService(EnvsApi::class.java).add(json.toJSONString().toRequestBody("application/json; charset=UTF-8".toMediaType()))
                }

                if(resp.code == 200){
                    setResult(Activity.RESULT_OK, intent.putExtra("env", env))
                    Toast.makeText(this@EnvsDetailActivity, "保存成功！" , Toast.LENGTH_SHORT).show()
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
                        env.setEid(oldEnv.getEId())
                        toolbar.title = "修改环境变量"
                        initEnvView(env)
                    }else{
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

    private var versionCode :String = "0"
    private fun getQLVersion(){
        lifecycleScope.launch {
            try{
                val resp = ServiceGenerator.createService(SystemApi::class.java).version()
                if(resp.code == 200){
                    resp.data?.version?.let {
                        versionCode = it
                        return@launch
                    }
                    Toast.makeText(this@EnvsDetailActivity, "获取系统版本号失败！" , Toast.LENGTH_SHORT).show()
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(this@EnvsDetailActivity, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@EnvsDetailActivity, "获取系统版本号失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(this@EnvsDetailActivity, "获取系统版本号失败！${e.message}" , Toast.LENGTH_SHORT).show()
            }
        }
    }

}