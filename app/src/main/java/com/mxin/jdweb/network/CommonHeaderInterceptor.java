package com.mxin.jdweb.network;

import android.text.TextUtils;

import com.mxin.jdweb.App;
import com.mxin.jdweb.common.SPConstants;
import com.mxin.jdweb.network.data.TokenData;
import com.mxin.jdweb.utils.SPUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CommonHeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        /*本次请求的接口地址*/
        String apiPath = url.scheme()+"://"+url.host()+":"+url.port()+url.encodedPath().trim();
        if(filterToken(apiPath)){
            return chain.proceed(request);
        }
        Request.Builder builder = request.newBuilder();

        HttpUrl.Builder newUrlBuild = url.newBuilder();
        newUrlBuild.addQueryParameter("t", String.valueOf(System.currentTimeMillis()));

        TokenData token = App.getInstance().getToken();

        List<String> pathSegments = url.pathSegments();
        if(pathSegments.size()>0 && !token.getApi().equals(pathSegments.get(0))){
            newUrlBuild.setEncodedPathSegment(0, token.getApi());
        }
        builder.addHeader("Authorization", token.getToken_type() + " " +token.getToken());


        builder.url(newUrlBuild.build());

        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }

    //过滤公共API
    private boolean filterToken(String url){
        String[] filterUrl = new String[]{"/api/user/login", "/open/auth/token"};
        for (String s : filterUrl) {
            if(url.contains(s)){
                return true;
            }
        }
        return false;
    }

}
