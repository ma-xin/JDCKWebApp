package com.mxin.jdweb.ui.ql

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import com.mxin.jdweb.App
import com.mxin.jdweb.R
import com.mxin.jdweb.base.BaseActivity
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.network.ServiceGenerator
import com.mxin.jdweb.utils.SpannableUtil
import com.mxin.jdweb.utils.kt.dp2px
import com.mxin.jdweb.utils.kt.positiveBtn
import com.mxin.jdweb.utils.kt.toColorInt

open class QlServerSettingActivity: BaseActivity() {

    private val spUtil by lazy { App.getInstance().spUtil }
    private val modelList = mutableListOf<FormEditModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ql_server_setting)
        initView()
    }

    override fun showLoadingView(): View? {
        return findViewById(R.id.contentLayout)
    }

    override fun onLoadRetry() {
    }

    open fun initView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = intent.getStringExtra("title")
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val contentLayout = findViewById<ViewGroup>(R.id.contentLayout)

        initSettingView(contentLayout, modelList)

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            try {
                var saveState = true
                modelList.forEach {
                    try{
                        it.saveCall.invoke(it.value)
                    }catch (e:Exception){
                        saveState = false
                        AlertDialog.Builder(this)
                            .setTitle("保存${it.name.split(",")[0]}失败")
                            .setMessage(e.message)
                            .positiveBtn("确定"){dialog, which ->
                                dialog.dismiss()
                            }.create().show()
                    }
                }
                if(saveState){
                    Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }catch (e: Exception){
                Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show()
            }

        }
    }

    open fun initSettingView(contentLayout:ViewGroup, modelList: MutableList<FormEditModel>){

        val ql_domain = spUtil.getString(SPConstants.QL_domain, ServiceGenerator.domain_default)
        val domainMode = FormEditModel("服务器IP,示例: ${ServiceGenerator.domain_default}", ql_domain)
            .save { spUtil.put(SPConstants.QL_domain, it) }
        modelList.add(domainMode)
        contentLayout.addView(initFormEditView(domainMode))

        contentLayout.addView(getSpaceView(20.dp2px(), R.color.divide_color.toColorInt()))
        val textView = initFormTextView("client_id、client_secret在系统设置->应用设置中添加\n权限勾选环境变量、系统信息", R.color.red.toColorInt(), 13f)
        contentLayout.addView(textView)
        contentLayout.addView(getSpaceView(10.dp2px(), android.R.color.transparent.toColorInt()))

        val client_id = spUtil.getString(SPConstants.QL_client_id)
        val clientIdMode = FormEditModel("client_id", client_id)
            .save { spUtil.put(SPConstants.QL_client_id, it) }
        modelList.add(clientIdMode)
        contentLayout.addView(initFormEditView(clientIdMode))

        val client_secret = spUtil.getString(SPConstants.QL_client_secret)
        val clientSecretModel = FormEditModel("client_secret", client_secret)
            .save { spUtil.put(SPConstants.QL_client_secret, it) }
        modelList.add(clientSecretModel)
        contentLayout.addView(initFormEditView(clientSecretModel))

        val version = spUtil.getString(SPConstants.QL_version)
        val versionModel = FormEditModel("青龙版本号", version)
            .save { spUtil.put(SPConstants.QL_version, it) }
        modelList.add(versionModel)
        contentLayout.addView(initFormEditView(versionModel))
    }

    fun getSpaceView(height: Int, color: Int):View{
        val view = View(this)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        view.setBackgroundColor(color)
        return view
    }

    fun initFormEditView(
        model: FormEditModel,
        lines: Int = 1,
        require: Boolean = false,
        onlyRead: Boolean = false
    ): View {
        val view = View.inflate(this, R.layout.layout_form_edit, null)
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val etValue = view.findViewById<EditText>(R.id.et_value)
        etValue.setLines(lines)
        if(onlyRead){
            etValue.isFocusable = false
            etValue.isFocusableInTouchMode = false
            etValue.isLongClickable = false
        }

        if(require){
            tvName.text = SpannableUtil.formatForeground("*${model.name}", 0, 1, Color.RED)
        }else{
            tvName.text = model.name
        }

        etValue.setText(model.value)
        etValue.hint = model.hint
        etValue.addTextChangedListener(FormEditWatch(model))
        return view
    }

    fun initFormTextView(text:String, textColor:Int, textSize:Float):TextView{
        val textView = TextView(this)
        textView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        textView.setPadding(8.dp2px())
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        textView.setTextColor(textColor)
        textView.setText(text)
        return textView
    }

}

data class FormEditModel(var name: String, var value: String?, private val _hint: String? = null){

    val hint get() = _hint?:"请输入$name"

    lateinit var saveCall : (String?)->Unit

    fun save(func: (String?) -> Unit) : FormEditModel{
        saveCall = func
        return this
    }
}

class FormEditWatch(val model: FormEditModel): TextWatcher{
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        model.value = s?.toString()?:""
    }

}