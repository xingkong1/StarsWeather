package com.xingkong.starsweather.gson;

/**
 * Created by 17273 on 2017/8/13.
 */

public class AQI {

    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
