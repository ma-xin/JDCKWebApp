package com.mxin.jdweb.base

import android.content.Context
import com.mxin.jdweb.widget.loading.LoadingDialog

object GlobalViewManager {

//    init {
//        initRefreshLayout()
//    }

    fun createLoadingView(context: Context): LoadingDialog {
        return LoadingDialog.Builder(context).create()
    }

    fun createLoadingIndicatorView(context: Context): LoadingDialog{
        return LoadingDialog.Builder(context).createIndicator()
    }

}