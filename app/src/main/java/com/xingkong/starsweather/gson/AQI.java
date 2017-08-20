package com.xingkong.starsweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 17273 on 2017/8/13.
 */

public class AQI {

    @SerializedName("city")
    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
        public String qlty;
    }
}
