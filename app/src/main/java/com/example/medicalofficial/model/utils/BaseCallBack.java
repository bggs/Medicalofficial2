package com.example.medicalofficial.model.utils;

/**
 * Created by Administrator on 2017/9/28.
 */
public interface BaseCallBack<T> {
    void success(T t);//成功
    void err(int code,String er);//失败
}

