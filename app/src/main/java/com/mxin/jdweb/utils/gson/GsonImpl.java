package com.mxin.jdweb.utils.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GsonImpl extends Json {

    private Gson gson = new Gson();

    public static Json get() {
        if (json == null) {
            json = new GsonImpl();
        }
        return json;
    }

    @Override
    public String toJson(Object src) {
        return gson.toJson(src);
    }
    @Override
    public <T> T toObject(String json, Class<T> claxx) {
        return gson.fromJson(json, claxx);
    }
    @Override
    public <T> T toObject(byte[] bytes, Class<T> claxx) {
        return gson.fromJson(new String(bytes), claxx);
    }
    @Override
    public <T> T fromJson(String json, Type t){
        return gson.fromJson(json, t);
    }

    @Override
    public <T> List<T> toList(String json, Class<T> claxx) {
          Type type = new TypeToken<ArrayList<T>>() {}.getType();
             List<T> list = gson.fromJson(json, type);
        return list;
    }
}