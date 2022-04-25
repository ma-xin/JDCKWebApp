package com.mxin.jdweb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mxin.jdweb.common.Constants
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.network.data.EnvsData
import com.mxin.jdweb.ui.ql.QLLoginActivity
import com.mxin.jdweb.ui.setting.SettingActivity
import com.mxin.jdweb.ui.web.WebActivity

class MainActivity : AppCompatActivity() {

    private val spUtil by lazy { App.getInstance().spUtil }
    private val mAdapter by lazy { MainAdapter() }

    private lateinit var launch : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }



    private fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val tvVersion = findViewById<TextView>(R.id.tv_version)
        tvVersion.text = "APP版本：${BuildConfig.VERSION_NAME}"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = mAdapter

        mAdapter.addChildClickViewIds(R.id.iv_icon)
        mAdapter.setOnItemChildClickListener{ _,view,position->
            val item = mAdapter.getItemOrNull(position)?:return@setOnItemChildClickListener
            when(view.id){
                R.id.iv_icon->{
                    if(item.model is WebModel){
                        item.model = if(item.model==WebModel.Normal) WebModel.Traceless else WebModel.Normal
                        spUtil.put(SPConstants.Web_model, (item.model as WebModel).value)
                        when(item.model as WebModel){
                            WebModel.Normal->{
                                item.tag = ""
                                item.icon = R.drawable.ic_bg_ie_browser
                            }
                            WebModel.Traceless->{
                                item.tag="无痕模式"
                                item.icon = R.drawable.ic_bg_ie_browser_traceless
                            }
                        }
                    }
                    mAdapter.notifyItemChanged(position)
                }
            }
        }
        mAdapter.setOnItemClickListener{_,_,position->
            val item = mAdapter.getItemOrNull(position)?:return@setOnItemClickListener
            when(item.title){
                "浏览器CK抓取工具"->{
                    startActivity(Intent(this, WebActivity::class.java).putExtra("model", item.model.toString()))
                }
                "青龙面板"->{
                    startActivity(Intent(this, QLLoginActivity::class.java))
                }
                "设置"->{
                    launch.launch(Intent(this, SettingActivity::class.java))
                }
            }
        }

        launch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == android.app.Activity.RESULT_OK) {
                refreshData()
            }
        }
        refreshData()
    }

    private fun initMainCardData(): MutableList<MainCardData> {
        val dataList = mutableListOf<MainCardData>()
        val array = JSON.parseArray(BuildConfig.user_permission);

        if(array.contains("web")){
            val model = WebModel.toValue( spUtil.getString(SPConstants.Web_model, WebModel.Traceless.value))
            val icon:Int
            val tag:String
            when(model){
                WebModel.Traceless->{
                    tag = "无痕模式"
                    icon = R.drawable.ic_bg_ie_browser_traceless
                }
                else->{
                    tag = ""
                    icon = R.drawable.ic_bg_ie_browser
                }
            }
            dataList.add(MainCardData(icon, "浏览器CK抓取工具", tag,
                "默认地址：${spUtil.getString(SPConstants.Web_home_url, Constants.WebView_Home_Url_Default)}\n点击浏览器图标，切换无痕/正常模式",
                model
            ))
        }
        if(array.contains("ql")){
            dataList.add(MainCardData(R.drawable.icon_ql_logo, "青龙面板", "","提供青龙面板基础数据维护,没有公网IP的就不要点了,访问不到的！"))
        }
        if(array.contains("setting")){
            dataList.add(MainCardData(R.drawable.ic_baseline_settings_24, "设置", "", "设置浏览器CK抓取工具的默认地址\n设置Gitee配置参数"))
        }
        return dataList
    }

    private fun refreshData(){
        mAdapter.setNewInstance(initMainCardData())
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.itemId == R.id.item_acion){
//           startActivity(Intent(this, WebActivity::class.java))
//        }
//        return super.onOptionsItemSelected(item)
//    }
}

data class MainCardData(
    var icon:Int,
    var title:String,
    var tag:String?,
    val descriptor:String,
    var model: MainDataMode? = null
)

interface MainDataMode

enum class WebModel(val value:String):MainDataMode{
    Normal("normal") ,
    Traceless("traceless") ;

    override fun toString(): String {
        return value
    }

    companion object{

        fun toValue(value:String?):WebModel{
            return when(value){
                Normal.value-> Normal
                Traceless.value -> Traceless
                else-> Normal
            }
        }

    }
}

class MainAdapter : BaseQuickAdapter<MainCardData, BaseViewHolder>(R.layout.item_main_card){
    override fun convert(holder: BaseViewHolder, item: MainCardData) {
        holder.setImageResource(R.id.iv_icon, item.icon)

        holder.setGone(R.id.tv_tag, item.tag.isNullOrEmpty())
        holder.setText(R.id.tv_tag, item.tag)

        holder.setText(R.id.tv_title, item.title)
        holder.setText(R.id.tv_desc, item.descriptor)
    }

}