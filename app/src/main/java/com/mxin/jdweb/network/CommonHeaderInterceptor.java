package com.mxin.jdweb.network;

import android.text.TextUtils;

import com.mxin.jdweb.App;
import com.mxin.jdweb.common.SPConstants;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CommonHeaderInterceptor implements Interceptor {

    String token = "";
    String tokenType = "";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        /*本次请求的接口地址*/
        String apiPath = url.scheme()+"://"+url.host()+":"+url.port()+url.encodedPath().trim();

        HttpUrl.Builder newUrlBuild = url.newBuilder();
        newUrlBuild.addQueryParameter("t", String.valueOf(System.currentTimeMillis()));
        Request.Builder builder = request.newBuilder();
        builder.url(newUrlBuild.build());

        if(filterToken(apiPath) && TextUtils.isEmpty(token)){
            token = App.getInstance().getSpUtil().getString(SPConstants.QL_token);
            tokenType = App.getInstance().getSpUtil().getString(SPConstants.QL_token_type);
        }
        if(!TextUtils.isEmpty(token)){
            builder.addHeader("Authorization", tokenType + " " +token);
        }

        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }


    //过滤公共API
    private boolean filterToken(String url){
        String[] filterUrl = new String[]{"/api/user/login", "/open/auth/token"};
        for (String s : filterUrl) {
            if(url.contains(s)){
                return false;
            }
        }
        return true;
    }

}
