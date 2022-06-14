package com.mxin.jdweb.network;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mxin.jdweb.App;
import com.mxin.jdweb.common.SPConstants;
import com.mxin.jdweb.network.api.LoginApi;
import com.mxin.jdweb.network.data.BaseResponse;
import com.mxin.jdweb.network.data.LoginData;
import com.mxin.jdweb.network.data.TokenData;
import com.mxin.jdweb.network.error.ResponseErrorListenerImpl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RespErrorInterceptor implements Interceptor {

    public static final String TAG = "RespErrorInterceptor";
//    private static Handler handler = null;
//    //是否弹出重新登录的弹出框
//    private boolean showLoginDialogFlag = false;

    //加锁标记
    private AtomicBoolean isLock = new AtomicBoolean(false);
    //等待线程队列
    private final Queue<Thread> WAIT_THREAD_QUEUE = new LinkedBlockingQueue<>();

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Response response = chain.proceed(request);
//        ResponseBody responseBody = response.body();
//
//        long contentLength = responseBody.contentLength();
//
//        if (!bodyEncoded(response.headers())) {
//            BufferedSource source = responseBody.source();
//            source.request(Long.MAX_VALUE); // Buffer the entire body.
//            Buffer buffer = source.getBuffer();
//
//            Charset charset = UTF8;
//            MediaType contentType = responseBody.contentType();
//            if (contentType != null) {
//                try {
//                    charset = contentType.charset(UTF8);
//                } catch (UnsupportedCharsetException e) {
//                    return response;
//                }
//            }
//
//            if (!isPlaintext(buffer)) {
//                return response;
//            }
//
//            if (contentLength != 0) {
//                String result = buffer.clone().readString(charset);
////                Log.e(TAG, " success.url():"+ success.request().url());
////                Log.e(TAG, " success.body():" + result);
//                //得到所需的string，开始判断是否异常
//                //***********************do something*****************************
//                try{
//                    JSONObject json = JSON.parseObject(result);
//                    if(json.getIntValue("code") == 401){
//                        App.getInstance().clearToken();
//                        String message =  "账户已过期，请重新登录！";
//                        Activity topAty = Utils.getTopActivity();
//                        if(!showLoginDialogFlag && topAty!=null && topAty.getPackageName().equals(App.getInstance().getPackageName()) && !topAty.isFinishing()) {
//                            if(handler==null){
//                                handler = new Handler(Looper.getMainLooper());
//                            }
//                            handler.post(() -> {
//                                new AlertDialog.Builder(topAty)
//                                        .setMessage(message)
//                                        .setPositiveButton(SpannableUtil.formatForegroundToRed("重新登录"), (dialog, which) -> {
//                                            dialog.dismiss();
//                                            topAty.startActivity(new Intent(topAty, QLLoginActivity.class).putExtra("expire", true)
//                                            );
//                                        })
//                                        .setNegativeButton(SpannableUtil.formatForegroundToGray("取消"), (dialog, which) -> {
//                                            dialog.dismiss();
//                                        })
//                                        .setOnDismissListener(dialog -> {
//                                            showLoginDialogFlag = false;
//                                        })
//                                        .create().show();
//                                showLoginDialogFlag = true;
//                            });
//                        }
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }
        if(isTokenExpired(response)){
            while (!lock()) {
                //获取不到锁的线程,添加到队列，并休眠
                lockWait();
            }
            Request oldRequest = chain.request();
            String oldToken = oldRequest.header("Authorization");
            TokenData tokenData = App.getInstance().getToken();
            if(oldToken==null || oldToken.equals(tokenData.getToken())){
                tokenData = getNewToken();
            }
            unlock();
            if(tokenData!=null){
                HttpUrl url = oldRequest.url();
                HttpUrl.Builder newUrlBuild = url.newBuilder();
                List<String> pathSegments = url.pathSegments();
                if(pathSegments.size()>0 && !tokenData.getApi().equals(pathSegments.get(0))){
                    newUrlBuild.setEncodedPathSegment(0, tokenData.getApi());
                }
                Request.Builder newRequest = oldRequest
                        .newBuilder()
                        .header("Authorization", tokenData.getToken_type() + " " +tokenData.getToken())
                        .url(newUrlBuild.build())
                        ;
                response.close();
                return chain.proceed(newRequest.build());
            }
        }

        if(!response.isSuccessful()){
            String errorMsg = ResponseErrorListenerImpl.convertStatusCode(response.code(), response.message(), "访问出现异常");
            BaseResponse<Object> data = new BaseResponse<>(500, null, errorMsg);
            return response.newBuilder().body(ResponseBody.create(JSON.toJSONString(data), MediaType.get("application/json; charset=UTF-8"))).build();
        }
        return response;
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

//    private boolean bodyEncoded(Headers headers) {
//        String contentEncoding = headers.get("Content-Encoding");
//        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
//    }
//
//    static boolean isPlaintext(Buffer buffer) throws EOFException {
//        try {
//            Buffer prefix = new Buffer();
//            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
//            buffer.copyTo(prefix, 0, byteCount);
//            for (int i = 0; i < 16; i++) {
//                if (prefix.exhausted()) {
//                    break;
//                }
//                int codePoint = prefix.readUtf8CodePoint();
//                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
//                    return false;
//                }
//            }
//            return true;
//        } catch (EOFException e) {
//            return false; // Truncated UTF-8 sequence.
//        }
//    }

    private boolean isTokenExpired(Response response){
        return response.code() == 401;
    }

    private TokenData getNewToken(){
        String clientID = App.getInstance().getSpUtil().getString(SPConstants.QL_client_id);
        String clientSecret = App.getInstance().getSpUtil().getString(SPConstants.QL_client_secret);
        try {
            if (!TextUtils.isEmpty(clientID) && !TextUtils.isEmpty(clientSecret)) {
                BaseResponse<TokenData> resp = ServiceGenerator.createService(LoginApi.class).authTokenSync(clientID, clientSecret).execute().body();
                if (resp.getCode() == 200 && resp.getData() != null) {
                    resp.getData().setApi("open");
                    App.getInstance().refreshToken(resp.getData());
                    return resp.getData();
                }
            } else {
                String username = App.getInstance().getSpUtil().getString(SPConstants.QL_login_username);
                String password = App.getInstance().getSpUtil().getString(SPConstants.QL_login_password);
                JSONObject params = new JSONObject();
                params.put("username", username);
                params.put("password", password);
                BaseResponse<LoginData> resp = ServiceGenerator.createService(LoginApi.class).loginSync(RequestBody.create(params.toJSONString(), MediaType.parse("application/json; charset=UTF-8"))).execute().body();
                if (resp.getCode() == 200 && resp.getData() != null) {
                    TokenData tokenData = new TokenData(resp.getData().getToken(), "Bearer", -1L, "api");
                    App.getInstance().refreshToken(tokenData);
                    return tokenData;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    //加锁的过程必须是原子操作，否则会导致多个线程同时加锁成功。
    public boolean lock(){
        return isLock.compareAndSet(false, true);
    }

    //释放锁

    public void unlock() {
        isLock.set(false);
        //唤醒队列中的第一个线程
        LockSupport.unpark(WAIT_THREAD_QUEUE.poll());
    }

    public void lockWait(){
        //将获取不到锁的线程添加到队列
        WAIT_THREAD_QUEUE.add(Thread.currentThread());
        //并休眠
        LockSupport.park();
    }


}
