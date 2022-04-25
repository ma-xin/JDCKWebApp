package com.mxin.jdweb.widget.loading

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.StyleRes
import com.mxin.jdweb.R
import com.mxin.jdweb.utils.ScreenUtils
import com.wang.avi.AVLoadingIndicatorView
import java.util.*


class LoadingDialog(context: Context, @StyleRes themeResId:Int) :Dialog(context, themeResId) {

    private var tvLoad:TextView ?= null

    fun setContent(content:String){
        if(tvLoad==null){
            tvLoad = findViewById(R.id.tv_load)
        }
        tvLoad?.text = content
    }


    class Builder(private val context: Context){

        fun create(): LoadingDialog {
            val dialog = LoadingDialog(context, R.style.Dialog)
            dialog.setContentView(R.layout.ui_dialog_loading_view)
            dialog.setCanceledOnTouchOutside(false) // 设置点击屏幕Dialog不消失
            val window = dialog.window!!
            val lp: WindowManager.LayoutParams = window.getAttributes()
            val screenW: Int = ScreenUtils.getScreenWidth()
            lp.width = (0.6 * screenW).toInt()
            val titleTxtv = dialog.findViewById<TextView>(R.id.tv_load)
            titleTxtv.text = "努力加载中..."
            return dialog
        }

        fun createIndicator(): LoadingDialog {
            val dialog = LoadingDialog(context, R.style.Dialog)
            dialog.setContentView(R.layout.ui_dialog_loading_indicator_view)
            dialog.setCanceledOnTouchOutside(false) // 设置点击屏幕Dialog不消失
            val indicatorView = dialog.findViewById<AVLoadingIndicatorView>(R.id.avi)
            dialog.setOnShowListener {  indicatorView.show()}
            dialog.setOnDismissListener { indicatorView.hide() }
            return dialog
        }

    }


}

class LoadingEvent private constructor(var isShow:Boolean, var content:String = "努力加载中...", var isCanceled:Boolean = true){

    companion object{

        private const val cacheSize = 2 * 2
        private val cacheEvent by lazy { LinkedList<LoadingEvent>() }

        @JvmStatic
        fun recycle(event: LoadingEvent){
            if(cacheEvent.size < cacheSize){
                event.recycle()
                cacheEvent.add(event)
            }
        }

        @JvmStatic
        fun create(isShow:Boolean, content:String = "努力加载中...", isCanceled:Boolean = true) : LoadingEvent {
            var event: LoadingEvent? = null
            if(cacheEvent.size > 0){
                event = cacheEvent.pollFirst()
            }
            if(event == null){
                return LoadingEvent(isShow, content, isCanceled)
            }else{
                event.isShow = isShow
                event.content = content
                event.isCanceled = isCanceled
                return event
            }
        }

        @JvmStatic
        fun clearCache(){
            cacheEvent.clear()
        }
    }

    private fun recycle() {
        isShow = false
        content = "努力加载中..."
        isCanceled = true
    }


}