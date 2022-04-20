package com.mxin.jdweb.utils.kt

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import com.mxin.jdweb.utils.SpanUtils
import com.mxin.jdweb.utils.Utils

/**
 *  Created by ：2020/7/23
 *  author : Administrator
 *
 */


/**   dip2px */
fun Int.dp2px(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()

}


/**   colorResId to ColorInt */
fun Int.toColorInt(): Int {
    return Utils.getApp().resources.getColor(this)
}

/**   StringResId to String */
fun Int.string(): String {
    return Utils.getApp().resources.getString(this)
}


/**  dialog   */
/**
 *  AlertDialog 配置
 *  @param title 标题  isNullOrEmpty不显示标题
 *  @param message 内容  isNullOrEmpty不显示内容
 *  @param config 设置dialog 可使用 positiveBtn() 和 #negativeBtn()
 */
fun Context.confirmDialog(
    title: CharSequence? = null,
    message: CharSequence? = null,
    config: (builder: AlertDialog.Builder) -> Unit
) {
    val builder = AlertDialog.Builder(this)
    if (!title.isNullOrEmpty()) builder.setTitle(title)
    if (!message.isNullOrEmpty()) builder.setMessage(message)
    config(builder)
    builder.create().show()
}

/**
 *  AlertDialog 配置 PositiveButton
 *  @param btnText  按钮文字
 *  @param btnTextColor 字体颜色
 *  @param isDismiss 是否自动关闭
 *  @param click 按钮点击事件
 *
 */
fun AlertDialog.Builder.positiveBtn(
    btnText: String,
    btnTextColor: Int = -1,
    isDismiss: Boolean = true,
    click: (dialog: DialogInterface, which: Int) -> Unit
) : AlertDialog.Builder{
    val span = if (btnTextColor == -1) btnText else SpanUtils().setForegroundColor(btnTextColor)
        .append(btnText).create()
    setPositiveButton(span) { dialog, which ->
        click.invoke(dialog, which)
        if (isDismiss) {
            dialog.dismiss()
        }
    }
    return this
}

/**
 *  AlertDialog 配置 NegativeButton
 *  @param btnText  按钮文字
 *  @param btnTextColor 字体颜色
 */
fun AlertDialog.Builder.negativeBtn(btnText: String, btnTextColor: Int = -1): AlertDialog.Builder {
    val span = if (btnTextColor == -1) btnText else SpanUtils().setForegroundColor(btnTextColor)
        .append(btnText).create()
    setNegativeButton(span) { dialog, _ ->
        dialog.dismiss()
    }
    return this
}

/**
 *  AlertDialog 配置 NegativeButton
 *  @param btnText  按钮文字
 *  @param btnTextColor 字体颜色
 *  @param isDismiss 是否自动关闭
 *  @param click 按钮点击事件
 *
 */
fun AlertDialog.Builder.negativeBtn(
    btnText: String,
    btnTextColor: Int = -1,
    isDismiss: Boolean = true,
    click: (dialog: DialogInterface, which: Int) -> Unit
) {
    val span = if (btnTextColor == -1) btnText else SpanUtils().setForegroundColor(btnTextColor)
        .append(btnText).create()
    setNegativeButton(span) { dialog, which ->
        click.invoke(dialog, which)
        if (isDismiss) {
            dialog.dismiss()
        }
    }
}
