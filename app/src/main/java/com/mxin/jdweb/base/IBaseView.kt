package com.mxin.jdweb.base

import com.mxin.jdweb.widget.loading.LoadingDialog


//interface IContextView{
//    fun getContext(): Context
//}
//
//
//interface IBaseView : IContextView, ILoadingView{
//
//    fun finishActivity()
//
//    fun initView()
//
//    fun initData()
//
//    fun getLifeCycleOwner():LifecycleOwner
//
//}


interface ILoadingView{

    fun showLoadingDialog(init:((LoadingDialog)->Unit)?)

    fun dismissLoadingDialog()

}