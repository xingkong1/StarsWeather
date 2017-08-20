package com.xingkong.starsweather.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 和服务器的交互类
 * Created by 17273 on 2017/8/13.
 */

public class HttpUtil {

    public  static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
