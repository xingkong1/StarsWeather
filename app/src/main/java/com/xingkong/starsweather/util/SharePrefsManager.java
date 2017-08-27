package com.xingkong.starsweather.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by yanghongtao on 2017/8/25 0025.
 */

public class SharePrefsManager {

    private static SharedPreferences preferences= PreferenceManager.
            getDefaultSharedPreferences(MyApplication.getContext());

    private static SharedPreferences.Editor editor=preferences.edit();

    public static String getString(String key){
       return preferences.getString(key,null);
    }

    public static void set(String key,String value){
        editor.putString(key,value);
        editor.commit();
    }

    public static boolean getBoolean(String key){
        return preferences.getBoolean(key,true);
    }

    public static void setBoolean(String key, Boolean value){
        editor.putBoolean(key,value);
        editor.commit();
    }

}
