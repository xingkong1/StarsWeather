package com.xingkong.starsweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.xingkong.starsweather.service.AutoUpdateService;
import com.xingkong.starsweather.util.MyApplication;
import com.xingkong.starsweather.util.SharePrefsManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        if(prefs.getString("weatherIds",null)!=null){
            Intent intent=new Intent(this,ViewPagerFragment.class);
            startActivity(intent);
            finish();
           /** if(SharePrefsManager.getBoolean("status_service")){
                Intent intent1=new Intent(this, AutoUpdateService.class);
                 startService(intent1);
            }
            */
        }
    }
}
