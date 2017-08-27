package com.xingkong.starsweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 17273 on 2017/8/13.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public String hum;

    public Wind wind;

    public class More{
        @SerializedName("txt")
        public String info;


    }

    public class Wind{
        public String dir;
        public String sc;
    }
}
