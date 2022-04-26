package com.mxin.jdweb;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.mxin.jdweb.common.Constants;
import com.mxin.jdweb.common.SPConstants;
import com.mxin.jdweb.network.data.TokenData;
import com.mxin.jdweb.utils.AppUtils;
import com.mxin.jdweb.utils.SPUtils;
import com.mxin.jdweb.widget.loading.Gloading;
import com.mxin.jdweb.widget.loading.GlobalAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import java.net.URI;
import java.util.List;

public class App extends Application {

    private static App mApp;
    private SPUtils mSPUtil;
    private TokenData token;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        initEnviron();
    }

    public static App getInstance(){
        return mApp;
    }

    public SPUtils getSpUtil(){
        if(mSPUtil==null){
            mSPUtil = SPUtils.getInstance("privateCacheData");
        }
        return mSPUtil;
    }

    private void initEnviron(){
        initGloadLoading();
        initSpData();
    }

    public TokenData getToken(){
        if(token == null){
            SPUtils spUtil = getSpUtil();
            String qlToken = spUtil.getString(SPConstants.QL_token);
            String qlTokenApi = spUtil.getString(SPConstants.QL_token_api,"api");
            String qlTokenType = spUtil.getString(SPConstants.QL_token_type);
            long qlTokenExpiration = spUtil.getLong(SPConstants.QL_token_expiration);
            token = new TokenData(qlToken, qlTokenType, qlTokenExpiration, qlTokenApi);
        }
        return token;
    }

    public void refreshToken(String qlToken, String qlTokenType, long qlTokenExpiration, String qlTokenApi){
        token = new TokenData(qlToken, qlTokenType, qlTokenExpiration, qlTokenApi);
        SPUtils spUtil = getSpUtil();
        spUtil.put(SPConstants.QL_token, qlToken);
        spUtil.put(SPConstants.QL_token_api, qlTokenApi);
        spUtil.put(SPConstants.QL_token_type, qlTokenType);
        spUtil.put(SPConstants.QL_token_expiration, qlTokenExpiration);
    }

    public void refreshToken(TokenData tokenData){
        refreshToken(tokenData.getToken(), tokenData.getToken_type(), tokenData.getExpiration(), tokenData.getApi());
    }

    public void clearToken(){
        token = new TokenData("", "", 0, "api");
        mSPUtil.remove(SPConstants.QL_token);
        mSPUtil.remove(SPConstants.QL_token_api);
        mSPUtil.remove(SPConstants.QL_token_type);
        mSPUtil.remove(SPConstants.QL_token_expiration);
    }

    private void initGloadLoading(){
        Gloading.debug(AppUtils.isAppDebug());
        Gloading.initDefault(new GlobalAdapter());
    }

    private void initSpData(){
        SPUtils spUtil = getSpUtil();
        boolean initState = spUtil.getBoolean(SPConstants.initState, true);
        if(initState){
            spUtil.put(SPConstants.initState, false);
            String qlIP = BuildConfig.ql_ip;
            if(!TextUtils.isEmpty(qlIP) && !qlIP.contains("127.0.0.1")){
                spUtil.put(SPConstants.QL_domain, qlIP);
            }
            String qlVersion = BuildConfig.ql_version;
            if(!TextUtils.isEmpty(qlVersion)){
                spUtil.put(SPConstants.QL_version, qlVersion);
            }
            String qlUserName = BuildConfig.ql_username;
            if(!TextUtils.isEmpty(qlUserName)){
                spUtil.put(SPConstants.QL_login_username, qlUserName);
            }
            String qlPassWord = BuildConfig.ql_password;
            if(!TextUtils.isEmpty(qlPassWord)){
                spUtil.put(SPConstants.QL_login_password, qlPassWord);
            }
            String qlClientId = BuildConfig.ql_client_id;
            if(!TextUtils.isEmpty(qlClientId)){
                spUtil.put(SPConstants.QL_client_id, qlClientId);
            }
            String qlClientSecret = BuildConfig.ql_client_secret;
            if(!TextUtils.isEmpty(qlClientSecret)){
                spUtil.put(SPConstants.QL_client_secret, qlClientSecret);
            }

            String giteeToken = BuildConfig.gitee_token;
            if(!TextUtils.isEmpty(giteeToken)){
                spUtil.put(SPConstants.GitEE_Token, giteeToken);
            }
            String giteeIssueUrl = BuildConfig.gitee_issue_url;
            if(!TextUtils.isEmpty(giteeIssueUrl)){
                spUtil.put(SPConstants.GitEE_repo_url, giteeIssueUrl);
                try{
                    Uri uri = Uri.parse(giteeIssueUrl);
                    List<String> pathSegments = uri.getPathSegments();
                    spUtil.put(SPConstants.GitEE_owner,  pathSegments.get(0));
                    spUtil.put(SPConstants.GitEE_repo,   pathSegments.get(1));
                    spUtil.put(SPConstants.GitEE_number, pathSegments.get(3));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            String webHomeUrl = BuildConfig.web_home_url;
            if(!TextUtils.isEmpty(webHomeUrl)){
                spUtil.put(SPConstants.Web_home_url, webHomeUrl);
            }
            String webCookieDomain = BuildConfig.web_cookie_domain;
            if(!TextUtils.isEmpty(webCookieDomain)){
                spUtil.put(SPConstants.Web_cookie_domain, webCookieDomain);
            }
        }
    }


    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.purple_200, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

}
