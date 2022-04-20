package com.mxin.jdweb.widget.loading

import android.view.View

interface IShowLoadingView {

    fun showLoading()

    fun showLoadSuccess()

    fun showLoadFailed()

    fun showEmpty()

    fun isShowLoading():Boolean

}


interface IConfigLoadingView : IShowLoadingView {

    fun showLoadingView(): View?

    fun onLoadRetry()

}


class LoadingViewHelper private constructor(val view: IConfigLoadingView) : IShowLoadingView {

    companion object{

        @JvmStatic
        fun bindView(view: IConfigLoadingView): LoadingViewHelper {
            return LoadingViewHelper(view)
        }

    }

    private var mHolder : Gloading.Holder? = null

    private fun getHolder():Gloading.Holder?{
        if(mHolder==null && view.showLoadingView()!=null){
            mHolder = Gloading.getDefault().wrap(view.showLoadingView())?.withRetry { view.onLoadRetry() }
        }
        return mHolder
    }


    override fun showLoading() {
        getHolder()?.showLoading()
    }

    override fun showLoadSuccess() {
        getHolder()?.showLoadSuccess()
    }

    override fun showLoadFailed() {
        getHolder()?.showLoadFailed()
    }

    override fun showEmpty() {
        getHolder()?.showEmpty()
    }

    override fun isShowLoading():Boolean = getHolder()?.isShow?:false

}