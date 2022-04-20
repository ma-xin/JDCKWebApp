package com.mxin.jdweb.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mxin.jdweb.widget.loading.IConfigLoadingView
import com.mxin.jdweb.widget.loading.LoadingDialog
import com.mxin.jdweb.widget.loading.LoadingViewHelper

abstract class BaseFragment : Fragment(), ILoadingView, IConfigLoadingView {

    private var loadingDialog: LoadingDialog?= null
    private var loadingViewHelper: LoadingViewHelper?= null


    override fun onDestroyView() {
        dismissLoadingDialog()
        loadingDialog=null
//        loadingViewHelper = null
        super.onDestroyView()
    }

    //</editor-fold>


    //<editor-fold desc="加载Dialog">

    override fun showLoadingDialog(init:((LoadingDialog)->Unit)?) {
        val context = context ?: return
        if(loadingDialog==null){
            loadingDialog = GlobalViewManager.createLoadingView(context)
        }
        init?.invoke(loadingDialog!!)
        if(!loadingDialog!!.isShowing){
            loadingDialog!!.show()
        }
    }

    override fun dismissLoadingDialog() {
        if(loadingDialog?.isShowing == true){
            loadingDialog?.dismiss()
        }
    }

    fun showLoadingDialog(text:String, isCancelable:Boolean = true){
        showLoadingDialog {
            if(it is LoadingDialog){
                it.setContent(text)
            }
            it.setCancelable(isCancelable)
        }
    }

    fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    //</editor-fold>


    //<editor-fold desc="初始化加载动画View" >

    private fun getLoadingViewHelper(): LoadingViewHelper {
        if(loadingViewHelper==null){
            loadingViewHelper = LoadingViewHelper.bindView(this)
        }
        return loadingViewHelper!!
    }

    override fun showLoading() {
        getLoadingViewHelper().showLoading()
    }

    override fun showLoadSuccess() {
        getLoadingViewHelper().showLoadSuccess()
    }

    override fun showLoadFailed() {
        getLoadingViewHelper().showLoadFailed()
    }

    override fun showEmpty() {
        getLoadingViewHelper().showEmpty()
    }

    override fun isShowLoading(): Boolean {
        return loadingViewHelper?.isShowLoading()?:false
    }

    //</editor-fold>

}