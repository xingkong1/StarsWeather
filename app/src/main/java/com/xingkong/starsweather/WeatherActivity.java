package com.xingkong.starsweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.xingkong.starsweather.db.City;
import com.xingkong.starsweather.db.County;
import com.xingkong.starsweather.gson.AQI;
import com.xingkong.starsweather.gson.Forecast;
import com.xingkong.starsweather.gson.Weather;
import com.xingkong.starsweather.service.AutoUpdateService;
import com.xingkong.starsweather.util.HttpUtil;
import com.xingkong.starsweather.util.Ifly;
import com.xingkong.starsweather.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private  TextView degreeText;

    private TextView weatherInfoText;

    private TextView weather_air;

    private LinearLayout forecastLayout;

    private TextView comfortText;

    private TextView drsgText;

    private TextView sportText;

    private TextView comfortBrf;

    private TextView drsgBrf;

    private TextView sportBrf;

    private TextView temRange;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefreshLayout;

    public DrawerLayout drawerLayout;

    private Button navButton;

    private ImageView now_image;

    private ImageView forecast_image;

    private Ifly ifly;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        SpeechUtility.createUtility(WeatherActivity.this, SpeechConstant.APPID + "=59993311");
        ifly=new Ifly();
        setContentView(R.layout.activity_weather);
        //初始化各控件
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        weather_air=(TextView)findViewById(R.id.weather_air);
        temRange=(TextView)findViewById(R.id.weather_temRange);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        drsgText=(TextView)findViewById(R.id.drsg_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        comfortBrf=(TextView)findViewById(R.id.comfort_brf);
        drsgBrf=(TextView)findViewById(R.id.drsg_brf);
        sportBrf=(TextView)findViewById(R.id.sport_brf);
        //bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        now_image=(ImageView)findViewById(R.id.now_image);
        forecast_image=(ImageView)findViewById(R.id.forecast_image);

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        Log.w("image","image为空");
        loadBingPic();

        final String weatherId;
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        forecast_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String forecast= prefs.getString("forecast_",null);
                if(forecast!=null){
                    if(ifly.mTts==null){
                        ifly.speak(forecast);
                    }else if(ifly.mTts.isSpeaking()){
                        if(ifly.status){
                            ifly.mTts.pauseSpeaking();
                            ifly.status=false;
                        }else{
                            ifly.mTts.resumeSpeaking();
                            ifly.status=true;
                        }
                    }else if(ifly.mTts.destroy()){
                        ifly.mTts.startSpeaking(forecast,null);
                    }

                }

                broadcast();
            }
        });
    }

    /**
     * jiaz必应每日一图
     */
    private void loadBingPic(){
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
                try{
                    JSONObject jsonObject=new JSONObject(bingPic);
                   JSONArray jsonArray= jsonObject.getJSONArray("images");
                    String url=jsonArray.getJSONObject(0).getString("url");
                    pic=baseUrl+url;
                }catch (Exception e){
                    e.printStackTrace();
                }
               final  String image=pic;
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",image);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(image).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        String weatherUrl="https://free-api.heweather.com/v5/weather?city="+
                weatherId+"&key=3641ea7c9cde405daa16d2cc80a60ec0";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                "获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                          SharedPreferences.Editor editor=
                                  PreferenceManager.
                                          getDefaultSharedPreferences(WeatherActivity.this).edit();
                          editor.putString("weather",responseText);
                            if(weather.aqi!=null){
                               County county= DataSupport.where("weatherid=?",weatherId).find(County.class).get(0);
                                county.setPm25(weather.aqi.city.pm25);
                                county.setQlty(weather.aqi.city.qlty);
                                county.save();
                            }
                            editor.apply();
                            showWeatherInfo(weather);
                            broadcast();
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        ifly.mTts=null;
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime="上次更新时间："+weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        String air="";
        if(weather.aqi!=null) {
                //air = "PM2.5：" + weather.aqi.city.pm25 + " ";
                air = air + "空气质量：" + weather.aqi.city.qlty;
        }else{
            List<County> countyList= DataSupport.where("weatherid=?",weather.basic.weatherId).find(County.class);
            if(countyList!=null&&!countyList.isEmpty()) {
                County county=countyList.get(0);
                if(county.getPm25()!=null&&county.getQlty()!=null) {
                    //air = "PM2.5：" + county.getPm25() + "  ";
                    air = air + "空气质量：" + county.getQlty();
                }
            }

        }
        weather_air.setText(air);
        String max=weather.forecastList.get(0).temperature.max;
        String min=weather.forecastList.get(0).temperature.min;
        String range=max+"°"+"/"+min+"°";
        temRange.setText(range);
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        switch (weatherInfo){
            case "晴":
               now_image.setImageResource(R.drawable.sunny);
                break;
            case "多云":
                now_image.setImageResource(R.drawable.cloudy);
                break;
            case "阴":
                now_image.setImageResource(R.drawable.cloudy);
                break;
            case "大雨":
                now_image.setImageResource(R.drawable.heavy_rain);
                break;
            case "阵雨":
                now_image.setImageResource(R.drawable.thundershower);
                break;
            case "雪":
                now_image.setImageResource(R.drawable.snow);
                break;

       }
        forecastLayout.removeAllViews();
        for(int i=1;i<weather.forecastList.size();i++){
            Forecast forecast=weather.forecastList.get(i);
            View view= LayoutInflater.from(this).inflate(
                    R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView rangeText=(TextView)view.findViewById(R.id.range);
            dateText.setText(getWeekOfDate(forecast.date));
            infoText.setText(forecast.more.info);
            rangeText.setText(forecast.temperature.max+"°"+"/"+forecast.temperature.min+"°");
            forecastLayout.addView(view);
        }
        String comfort_brf="舒适指数--- "+weather.suggestion.comfort.brf;
        String drsg_brf="穿衣指数--- "+weather.suggestion.dresssg.brf;
        String sport_brf="运动指数--- "+weather.suggestion.sport.brf;
        String comfort="舒适度: "+weather.suggestion.comfort.info;
        String drsg="穿衣建议: "+weather.suggestion.dresssg.info;
        String sport="运动建议: "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        drsgText.setText(drsg);
        sportText.setText(sport);
        comfortBrf.setText(comfort_brf);
        drsgBrf.setText(drsg_brf);
        sportBrf.setText(sport_brf);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private  void broadcast(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        String text="";
        if(weatherString!=null){
            Weather weather=Utility.handleWeatherResponse(weatherString);
            text+=weather.basic.cityName+":";
            text+="今天是"+getWeekOfDate(weather.forecastList.get(0).date)+",";
            text+="是一个"+weather.now.more.info+"天";
            text+="今天最高温度为"+weather.forecastList.get(0).temperature.max+"摄氏度，";
            text+="最低温度为"+weather.forecastList.get(0).temperature.min+"摄氏度。";
            text+="当前室外温度为"+weather.now.temperature+"摄氏度,";
            text+="相对湿度为百分之"+weather.now.hum+",";
            text+=weather.now.wind.dir+weather.now.wind.sc+"级。";
            if(weather.aqi!=null) {
                text+="空气质量为"+weather.aqi.city.qlty+",PM2.5含量为"+weather.aqi.city.pm25+"。";
            }
            text+="给您一些生活小建议：";
            text+=weather.suggestion.comfort.info+"。";
            text+=weather.suggestion.dresssg.info+"。";
            text+=weather.suggestion.sport.info+"。";
            text+=weather.suggestion.sunny.info+"。";
            SharedPreferences.Editor editor=
                    PreferenceManager.
                            getDefaultSharedPreferences(this).edit();
            editor.putString("forecast",text);
            editor.apply();
        }
    }

    private String getWeekOfDate(String dataText){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date=format.parse(dataText);
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("EEEE");
            String week=simpleDateFormat.format(date);
            return dataText+" "+week;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dataText;
    }


}
