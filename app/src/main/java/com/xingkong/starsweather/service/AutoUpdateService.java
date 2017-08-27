package com.xingkong.starsweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.xingkong.starsweather.WeatherActivity;
import com.xingkong.starsweather.gson.Weather;
import com.xingkong.starsweather.util.HttpUtil;
import com.xingkong.starsweather.util.SharePrefsManager;
import com.xingkong.starsweather.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {



    public AutoUpdateService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
                updateBingPic();
            }
        }).start();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        String time= SharePrefsManager.getString("interval");
        int anHour=1*60*60*1000;
        if(time!=null){
            anHour=anHour*Integer.parseInt(time.substring(0,1));
        }
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent intent1=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            String weatherId=weather.basic.weatherId;
            String weatherUrl="https://free-api.heweather.com/v5/weather?city="+
                    weatherId+"&key=3641ea7c9cde405daa16d2cc80a60ec0";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                        }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final  String responseText=response.body().string();
                    final Weather weather=Utility.handleWeatherResponse(responseText);
                            if(weather!=null&&"ok".equals(weather.status)){
                                SharedPreferences.Editor editor=
                                        PreferenceManager.
                                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                editor.putString("weather",responseText);
                                editor.apply();
                            }
                        }
                    });
        }
    }

    /**
     * 更新并应每日一图
     */
    private void updateBingPic(){
        String requestBingPic="https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
        final String baseUrl="http://cn.bing.com";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                String pic="";
                Log.w("image","相片开始初始化");
                Log.w("image",bingPic);
                try{
                    JSONObject jsonObject=new JSONObject(bingPic);
                    JSONArray jsonArray= jsonObject.getJSONArray("images");
                    String url=jsonArray.getJSONObject(0).getString("url");
                    pic=baseUrl+url;
                    Log.w("image",pic);
                }catch (Exception e){
                    e.printStackTrace();
                }
                final  String image=pic;
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",image);
                editor.apply();
            }
        });
    }
}
