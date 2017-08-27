package com.xingkong.starsweather.util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePalApplication;

/**
 * Created by yanghongtao on 2017/8/22 0022.
 */

public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        context=getApplicationContext();
        LitePalApplication.initialize(context);
    }
    public static Context getContext(){
        return context;
    }
}
