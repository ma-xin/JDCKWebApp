package com.mxin.jdweb.ui.setting

import android.net.Uri
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.mxin.jdweb.App
import com.mxin.jdweb.BuildConfig
import com.mxin.jdweb.R
import com.mxin.jdweb.common.Constants
import com.mxin.jdweb.common.SPConstants
import com.mxin.jdweb.ui.ql.FormEditModel
import com.mxin.jdweb.ui.ql.QlServerSettingActivity
import com.mxin.jdweb.utils.kt.dp2px
import com.mxin.jdweb.utils.kt.toColorInt
import java.lang.IllegalArgumentException


open class GitEESettingActivity : QlServerSettingActivity() {

    private val spUtil by lazy { App.getInstance().spUtil }

    override fun initSettingView(contentLayout: ViewGroup, modelList: MutableList<FormEditModel>) {

        val owner = spUtil.getString(SPConstants.GitEE_owner, BuildConfig.gitee_owner)
        val repo = spUtil.getString(SPConstants.GitEE_repo, BuildConfig.gitee_repo)
        val number = spUtil.getString(SPConstants.GitEE_number, BuildConfig.gitee_issue)
        val repoIssueUrl = "https://gitee.com/%s/%s/issues/%s".format(owner, repo, number)
        val url = spUtil.getString(SPConstants.GitEE_repo_url, repoIssueUrl)
        val clientIdMode =
            FormEditModel("GitEE仓库Issue地址,示例: ${Constants.GitEE_Repo_Issue_Url}", url)
                .save {
                    spUtil.put(SPConstants.GitEE_repo_url, it)
                    val uri = Uri.parse(it)
                    val pathSegments = uri.pathSegments
                    if(pathSegments.size<4){
                        val ps = Uri.parse(Constants.GitEE_Repo_Issue_Url).pathSegments
                        throw IllegalArgumentException("GitEE仓库Issue地址截取错误，请参考示例地址格式，保存会截取示例中的用户名【${ps[0]}】、仓库名【${ps[1]}】、IssueNumber【${ps[3]}】")
                    }
                    spUtil.put(SPConstants.GitEE_owner, pathSegments[0])
                    spUtil.put(SPConstants.GitEE_repo, pathSegments[1])
                    spUtil.put(SPConstants.GitEE_number, pathSegments[3])
                }
        modelList.add(clientIdMode)
        contentLayout.addView(initFormEditView(clientIdMode, lines = 2))

        val accessToken = spUtil.getString(SPConstants.GitEE_Token, BuildConfig.gitee_token)
        val domainMode = FormEditModel("GitEE私人令牌", accessToken)
            .save {
                spUtil.put(SPConstants.GitEE_Token, it)
            }
        modelList.add(domainMode)
        contentLayout.addView(initFormEditView(domainMode, lines = 2))

    }


}