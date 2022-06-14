package com.mxin.jdweb.utils.gson;
import java.lang.reflect.Type;
import java.util.List;
/**
 * @author soyoungboy
 * @date 2014-11-8 下午2:32:24
 */
public abstract class Json {

 protected static Json json;

 public static <T extends Json> Json create(Class<T> claz) {
  if (json == null) {
   try {
    json = claz.newInstance();
   } catch (IllegalAccessException e) {
    e.printStackTrace();
   } catch (InstantiationException e) {
    e.printStackTrace();
   }finally {
    if(json==null){
     throw new IllegalArgumentException("无法访问无参构造方法！");
    }
   }
  }
  return json;
 }
 public abstract String toJson(Object src);
 public abstract <T> T toObject(String json, Class<T> claxx);
 public abstract <T> T toObject(byte[] bytes, Class<T> claxx);
 public abstract <T> T fromJson(String json, Type t);
 public abstract <T> List<T> toList(String json, Class<T> claxx);
}