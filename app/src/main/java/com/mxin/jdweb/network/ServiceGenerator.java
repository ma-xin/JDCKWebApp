package com.mxin.jdweb.network;

import android.text.TextUtils;

import com.mxin.jdweb.App;
import com.mxin.jdweb.BuildConfig;
import com.mxin.jdweb.common.SPConstants;
import com.mxin.jdweb.utils.SPUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit（OKHttp）配置
 */
public class ServiceGenerator {


    public final static String domain_default = "http://127.0.0.1:5700";

    public static String API_BASE_URL = domain_default; //配置api主地址
    public static int READ_TIMEOUT = 60 * 10;          //配置读取超时时间
    public static int WRIT_TIMEOUT = 60 * 10;          //配置写超时时间
    public static int CONNECT_TIMEOUT = 10;       //配置连接超时时间
    public static SPUtils spUtils = App.getInstance().getSpUtil();

    protected static OkHttpClient.Builder httpClient = builderOkHttpClient();
    protected static Retrofit retrofit;

    protected static OkHttpClient.Builder builderOkHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRIT_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);

        httpClient.addInterceptor(new CommonHeaderInterceptor());
        httpClient.addInterceptor(new RespErrorInterceptor());

        // 日志拦截器
        if (BuildConfig.DEBUG) {
            httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return httpClient;
    }

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL = spUtils.getString(SPConstants.QL_domain, domain_default)).addConverterFactory(GsonConverterFactory.create());

    public static void reset(String domain) {
        SPUtils spUtil = App.getInstance().getSpUtil();
        spUtil.put(SPConstants.QL_domain, domain);
        API_BASE_URL = domain ;
        builder =
                new Retrofit.Builder()
                        .baseUrl(API_BASE_URL).addConverterFactory(GsonConverterFactory.create());
        OkHttpClient client = httpClient.build();
        retrofit = builder.client(client).build();
    }


    public static OkHttpClient.Builder getOkHttpClient(){
        return httpClient;
    }

    public static <S> S createService(Class<S> serviceClass) {
        if(retrofit==null){
            String domain = App.getInstance().getSpUtil().getString(SPConstants.QL_domain);
            reset(domain);
        }
        return retrofit.create(serviceClass);
//        return createService(serviceClass, null);
    }

    public static <S> S createService(Class<S> serviceClass, final String authToken) {
//        if (authToken != null) {
//            httpClient.addInterceptor(new Interceptor() {
//                @Override
//                public Response intercept(Chain chain) throws IOException {
//                    Request original = chain.request();
//
//                    // Request customization: add request headers
//                    Request.Builder requestBuilder = original.newBuilder()
//                            .method(original.method(), original.body());
//
//                    Request request = requestBuilder.build();
//                    return chain.proceed(request);
//                }
//            });
//        }

        return retrofit.create(serviceClass);
    }

// static CommonInterceptor commonInterceptor;
// private static CommonInterceptor getCommonInterceptor(){
//     if(commonInterceptor==null){
//      commonInterceptor = new CommonInterceptor();
//     }
////     SharePreference mSpUtil = Application.getInstance().getSpUtil();
////     String[] keys= new String[]{"sign_userid","sign_compid","sign_depid"};
////     String[] values = new String[]{mSpUtil.getUserId(),mSpUtil.getCompId(),mSpUtil.getStruId()};
////     commonInterceptor.updateValue(keys,values);
//     return commonInterceptor;
// }


}