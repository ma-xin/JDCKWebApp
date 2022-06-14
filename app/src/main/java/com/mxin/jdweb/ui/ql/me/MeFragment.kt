package com.mxin.jdweb.ui.ql.me

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.ui.ql.QLLoginActivity
import java.util.*

class MeFragment:Fragment() {

    private val spUtil by lazy { App.getInstance().spUtil }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val mAdapter = SetAdapter(initSetData())
        recyclerView.adapter = mAdapter
        mAdapter.setOnItemClickListener{ _, _, position->
            val item = mAdapter.getItemOrNull(position)?:return@setOnItemClickListener
            when(item.name){
                "APP最新资讯" -> {
                    val intent = Intent(context, OpenWebActivity::class.java)
                    intent.data = Uri.parse("https://gitee.com/maxinDev/jdck-web-app")
                    startActivity(intent)
                }
                "退出登录" -> {
                    App.getInstance().clearToken()
                    startActivity(Intent(context, QLLoginActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    private fun initSetData(): MutableList<SetBean>? {
        val beanList = ArrayList<SetBean>()
        beanList.add(SetBean("APP最新资讯", R.drawable.ic_baseline_language_24))
        beanList.add(SetBean("退出登录", 0).setItemType(SetBean.ITEM_BIG_ROW));
        return beanList
    }

}