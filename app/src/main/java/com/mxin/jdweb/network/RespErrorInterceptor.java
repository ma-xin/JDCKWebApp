package com.mxin.jdweb.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.mxin.jdweb.App;
import com.mxin.jdweb.network.data.BaseResponse;
import com.mxin.jdweb.network.error.ResponseErrorListenerImpl;
import com.mxin.jdweb.ui.ql.QLLoginActivity;
import com.mxin.jdweb.utils.SpannableUtil;
import com.mxin.jdweb.utils.Utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class RespErrorInterceptor implements Interceptor {

    public static final String TAG = "RespErrorInterceptor";
    private static Handler handler = null;

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Response response = chain.proceed(request);

        if(!response.isSuccessful()){
            String errorMsg = ResponseErrorListenerImpl.convertStatusCode(response.code(), "访问出现异常");
            BaseResponse<Object> data = new BaseResponse<>(500, null, errorMsg);
            return response.newBuilder().body(ResponseBody.create(JSON.toJSONString(data), MediaType.get("application/json; charset=UTF-8"))).build();
        }

        ResponseBody responseBody = response.body();

        long contentLength = responseBody.contentLength();

        if (!bodyEncoded(response.headers())) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.getBuffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    return response;
                }
            }

            if (!isPlaintext(buffer)) {
                return response;
            }

            if (contentLength != 0) {
                String result = buffer.clone().readString(charset);
//                Log.e(TAG, " success.url():"+ success.request().url());
//                Log.e(TAG, " success.body():" + result);
                //得到所需的string，开始判断是否异常
                //***********************do something*****************************
                try{

                    BaseResponse data = JSON.parseObject(result, BaseResponse.class);
                    if(data.getCode() == 401){
                        String message =  "账户已过期，请重新登录！";
                        Activity topAty = Utils.getTopActivity();
                        if(topAty!=null && topAty.getPackageName().equals(App.getInstance().getPackageName()) && !topAty.isFinishing()) {
                            if(handler==null){
                                handler = new Handler(Looper.getMainLooper());
                            }
                            handler.post(() -> new AlertDialog.Builder(topAty)
                                .setMessage(message)
                                .setPositiveButton(SpannableUtil.formatForegroundToRed("重新登录"), (dialog, which) -> {
                                    dialog.dismiss();
                                    topAty.startActivity(new Intent(topAty, QLLoginActivity.class).putExtra("expire", true)
                                    );
                                })
                                .setNegativeButton(SpannableUtil.formatForegroundToGray("取消"), (dialog, which) -> dialog.dismiss())
                                .create().show());
                        }
                    }
//                    if(response.isSuccessful() && data!=null  && data.isSuccess()){
//                        if(data.getVersionControl()!=null){
//                            DictHelper.getInstance().cacheDictDatas(data.getVersionControl().getDictVersion());
//                            PlaceHelper.getInstance().cachePlaceDatas(data.getVersionControl().getJgVersion());
//                            DictRoadHelper.getInstance().cacheDictDatas(data.getVersionControl().getRoadVersion());
////                            DictCountryHelper.getInstance().cacheDictCountryDatas(data.getVersionControl().getCountryVersion());
//                        }
//                    }else if(data.getCode() == 102 || data.getCode() == 103){
//                        //102 ： 登录信息已过期  ， 103 ： 登录信息异常   需要重新登录
//                        String errorStr = data instanceof BaseBody ? ((BaseBody)data).getError() : data instanceof BaseResult ? ((BaseResult)data).getMessage() : ("code:"+data.getCode());
//                        String message = data.getCode() == 102 ? "账户信息已过期，请重新登录！" : data.getCode() == 103 ? "账户信息异常，请重新登录！" : errorStr;
//                        Activity topAty = Utils.getTopActivity();
//                        if(topAty!=null && topAty.getPackageName().equals(App.getInstance().getPackageName()) && !topAty.isFinishing()){
//                            new MyHandler(Looper.getMainLooper())
//                                    .post(() -> new AlertDialog.Builder(topAty)
//                                            .setMessage(message)
//                                            .setPositiveButton(SpannableUtil.formatForegroundToRed("去登录"), (dialog, which) -> {
//                                                dialog.dismiss();
//                                                topAty.startActivity(new Intent(topAty, LoginActivity.class)
//                                                        .putExtra("expire", true));
//                                            })
//                                            .setNegativeButton(SpannableUtil.formatForegroundToGray("取消"), (dialog, which) -> dialog.dismiss())
//                                            .create().show());
//                        }
//                    }
//                    String token = response.header(CommonHeaderInterceptor.TOKEN);
//                    if(!TextUtils.isEmpty(token)){
//                        String oldToken = App.getInstance().getToken();
//                        if(!token.equals(oldToken)){
//                            App.getInstance().setToken(token);
//                        }
//                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    static boolean isPlaintext(Buffer buffer) throws EOFException {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}
