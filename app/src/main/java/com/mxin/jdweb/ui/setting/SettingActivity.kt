package com.mxin.jdweb.ui.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.base.BaseActivity
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.ui.ql.QLLoginActivity
import com.mxin.jdweb.ui.ql.me.SetAdapter
import com.mxin.jdweb.ui.ql.me.SetBean
import com.mxin.jdweb.ui.ql.me.OpenWebActivity
import java.util.ArrayList

class SettingActivity : BaseActivity() {

    private lateinit var launch : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        initView()
    }

    private fun initView() {

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "设置"

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val mAdapter = SetAdapter(initSetData())
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener{ _, _, position->
            val item = mAdapter.getItemOrNull(position)?:return@setOnItemClickListener
            when(item.name){
                "浏览器CK抓取工具设置" -> {
                    val intent = Intent(this, WebSettingActivity::class.java).putExtra("title",item.name)
                    launch.launch(intent)
                }
                "GitEE中转站参数配置" -> {
                    startActivity(Intent(this, GitEESettingActivity::class.java).putExtra("title",item.name))
                }
            }
        }

        launch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK)
            }
        }
    }

    private fun initSetData(): MutableList<SetBean> {
        val beanList = ArrayList<SetBean>()
        beanList.add(SetBean("浏览器CK抓取工具设置", R.drawable.ic_bg_ie_browser))
        beanList.add(SetBean("GitEE中转站参数配置", R.drawable.ic_gitee_logo))
        return beanList
    }

    override fun showLoadingView(): View? {
        return null
    }

    override fun onLoadRetry() {

    }

}