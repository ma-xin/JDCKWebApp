package com.mxin.jdweb.ui.ql

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mxin.jdweb.R
import com.mxin.jdweb.base.BaseFragment
import com.mxin.jdweb.network.ServiceGenerator
import com.mxin.jdweb.network.api.EnvsApi
import com.mxin.jdweb.network.data.EnvsData
import com.mxin.jdweb.utils.RecycleViewDivider
import com.mxin.jdweb.utils.SpannableUtil
import com.mxin.jdweb.utils.kt.dp2px
import com.mxin.jdweb.utils.kt.toColorInt
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class EnvsFragment: BaseFragment() {

    private lateinit var refreshLayout:SmartRefreshLayout
    private val mAdapter by lazy { EvnsAdapter() }
    private lateinit var launch : ActivityResultLauncher<Intent>
    private val mHomeModel by lazy { ViewModelProvider(requireActivity()).get(HomeModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_envs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val etQuery = view.findViewById<EditText>(R.id.query)
        etQuery.hint = "请输入名称/值/备注"
        view.findViewById<View>(R.id.btn_search).setOnClickListener {
            loadEnvsData(etQuery.text.toString())
        }

        launch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == android.app.Activity.RESULT_OK && result.data!=null) {
                val env = result.data?.getParcelableExtra<EnvsData>("env")
                val position = result.data?.getIntExtra("position", -1)?:-1
                val item = mAdapter.getItemOrNull(position)
                if(env!=null && item!=null && item.getEId() == env.getEId()){
                    item.name = env.name
                    mAdapter.setData(position, env)
                }else if(env!=null){
                    mAdapter.addData(env)
                }
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = mAdapter
        recyclerView.addItemDecoration(RecycleViewDivider(context, LinearLayoutManager.VERTICAL, 1.dp2px(), R.color.divide_color.toColorInt()))

        mAdapter.addChildClickViewIds(R.id.content, R.id.right_menu_edit, R.id.right_menu_enable, R.id.right_menu_delete)
        mAdapter.setOnItemChildClickListener{ adapter, view, position->
            val item = mAdapter.getItemOrNull(position)?:return@setOnItemChildClickListener
            when(view.id){
                R.id.content ->{
                    startActivity(EnvsDetailActivity.viewEnv(requireContext(), item))
                }
                R.id.right_menu_edit ->{
                    launch.launch(EnvsDetailActivity.updateEnv(requireContext(), item, position))
                }
                R.id.right_menu_enable->{
                    if(item.status == 0){
                        disableEnv(item, position)
                    }else{
                        enableEnv(item, position)
                    }
                }
                R.id.right_menu_delete->{
                    AlertDialog.Builder(context)
                        .setMessage("是否删除环境变量")
                        .setPositiveButton(SpannableUtil.formatForegroundToRed("删除")){dialog, _ ->
                            deleteEnv(item)
                            dialog.dismiss()
                        }
                        .setNegativeButton(SpannableUtil.formatForegroundToGray("取消")){dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                }
            }
        }

        refreshLayout = view.findViewById(R.id.refreshLayout)
        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setOnRefreshListener {
            loadEnvsData()
        }
        initData()

        mHomeModel.addEnvLiveData.observe(viewLifecycleOwner){
            if(it>0){
                launch.launch(EnvsDetailActivity.addEnv(requireContext()))
            }
        }
    }

    override fun showLoadingView(): View? {
        return refreshLayout
    }

    override fun onLoadRetry() {
        showLoading()
        loadEnvsData()
    }

    private fun initData() {
        onLoadRetry()
    }

    private fun loadEnvsData(searchValue:String=""){
        lifecycleScope.launch {
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).list(searchValue)
                if(resp.code == 200){
                    val list = resp.data
                    mAdapter.setNewInstance(list?.toMutableList())
                    if(list.isNullOrEmpty()){
                        showEmpty()
                    }else{
                        showLoadSuccess()
                    }
                }else if(!resp.message.isNullOrEmpty()){
                    showLoadFailed()
                    Toast.makeText(context, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    showLoadFailed()
                    Toast.makeText(context, "获取环境变量失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                showLoadFailed()
                Toast.makeText(context, "获取环境变量失败！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            refreshLayout.finishRefresh()
        }
    }

    private fun deleteEnv(env: EnvsData){
        lifecycleScope.launch {
            showLoadingDialog("正在删除")
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).delete("[${env.getEId()}]".toRequestBody("application/json; charset=UTF-8".toMediaType()))
                if(resp.code == 200){
                    mAdapter.remove(env)
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(context, resp.message, Toast.LENGTH_SHORT).show()
                }else{

                    Toast.makeText(context, "删除失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(context, "删除异常！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

    private fun enableEnv(env: EnvsData, position:Int){
        lifecycleScope.launch {
            showLoadingDialog("正在启用")
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).enable("[${env.getEId()}]".toRequestBody("application/json; charset=UTF-8".toMediaType()))
                if(resp.code == 200){
                    env.status = 0
                    mAdapter.notifyItemChanged(position)
                    Toast.makeText(context, "启用成功！" , Toast.LENGTH_SHORT).show()
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(context, resp.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "启用失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(context, "启用异常！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }

    private fun disableEnv(env: EnvsData, position:Int){
        lifecycleScope.launch {
            showLoadingDialog("正在禁用")
            try{
                val resp = ServiceGenerator.createService(EnvsApi::class.java).disable("[${env.getEId()}]".toRequestBody("application/json; charset=UTF-8".toMediaType()))
                if(resp.code == 200){
                    env.status = 1
                    mAdapter.notifyItemChanged(position)
                }else if(!resp.message.isNullOrEmpty()){
                    Toast.makeText(context, resp.message, Toast.LENGTH_SHORT).show()
                }else{

                    Toast.makeText(context, "禁用失败！" , Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(context, "禁用异常！${e.message}" , Toast.LENGTH_SHORT).show()
            }
            dismissLoadingDialog()
        }
    }
}

class EvnsAdapter : BaseQuickAdapter<EnvsData, BaseViewHolder>(R.layout.item_envs){

    override fun convert(holder: BaseViewHolder, item: EnvsData) {
        holder.setText(R.id.tv_name, item.name)
        holder.setText(R.id.tv_remarks, "备注：${item.remarks}")
        holder.setText(R.id.tv_value, "值：${item.value}")
        holder.setText(R.id.tv_time, item.getTime())
        holder.setBackgroundResource(R.id.iv_status, if(item.status == 0) R.drawable.shape_label_status_enable else R.drawable.shape_label_status_disable)
        holder.setText(R.id.right_menu_enable, if(item.status == 0) "禁用" else "启用")
        holder.setBackgroundColor(R.id.right_menu_enable, (if(item.status == 0) R.color.gray_pressed else R.color.green ).toColorInt())
    }

}