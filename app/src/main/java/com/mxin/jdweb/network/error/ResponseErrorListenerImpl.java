/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mxin.jdweb.network.error;

import android.net.ParseException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;


/**
 * ================================================
 * 展示 {@link Throwable} 的用法
 * <p>
 * Created by JessYan on 04/09/2017 17:18
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class ResponseErrorListenerImpl {

    public static String handleResponseError(Throwable t) {

        //这里不光只能打印错误, 还可以根据不同的错误做出不同的逻辑处理
        //这里只是对几个常用错误进行简单的处理, 展示这个类的用法, 在实际开发中请您自行对更多错误进行更严谨的处理
        String msg = "接口请求失败！";
        if (t instanceof UnknownHostException) {
            msg = "当前网络不可用，请检查网络";
        } else if (t instanceof SocketTimeoutException) {
            msg = "服务器连接超时，请检查网络后重试！";
        }else if(t instanceof ConnectException){
            //java.net.ConnectException: Failed to connect to /161.184.160.158:6877
        } else if (t instanceof HttpException) {
            HttpException httpException = (HttpException) t;
            msg = convertStatusCode(httpException.code(), httpException.message(), "HttpException");
        } else if (t instanceof JsonParseException || t instanceof ParseException ||
//                || t instanceof com.alibaba.fastjson.JSONException
                t instanceof JSONException  || t instanceof JsonIOException) {
            msg = "数据解析错误";
            t.printStackTrace();
        }
        return msg;
    }

    public static String convertStatusCode(int code, String errorMessage, String defaultMessage) {
        String msg;
        if (code == 500) {
            msg = "服务器发生错误了~500 "+errorMessage;
        } else if (code == 404) {
            msg = "请求无法访问 ~404 "+errorMessage;
        } else if (code == 403) {
            msg = "请求被服务器拒绝~403 "+errorMessage;
        } else if (code == 307) {
            msg = "请求被重定向到其他页面~307 "+errorMessage;
        } else {
            msg = defaultMessage+"~"+code+" "+errorMessage;
        }
        return msg;
    }
}