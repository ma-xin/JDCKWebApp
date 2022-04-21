package com.mxin.jdweb;

import android.app.Application;
import android.content.Context;

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
        SPUtils spUtil = getSpUtil();
        spUtil.put(SPConstants.QL_token, qlToken);
        spUtil.put(SPConstants.QL_token_api, qlTokenApi);
        spUtil.put(SPConstants.QL_token_type, qlTokenType);
        spUtil.put(SPConstants.QL_token_expiration, qlTokenExpiration);
        token = new TokenData(qlToken, qlTokenType, qlTokenExpiration, qlTokenApi);
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
