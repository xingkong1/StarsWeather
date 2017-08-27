package com.xingkong.starsweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 17273 on 2017/8/13.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;
    @SerializedName("drsg")
    public Dresssg dresssg;
    public Sport sport;
    @SerializedName("uv")
    public Sunny sunny;

    public class Comfort{
        public String brf;
        @SerializedName("txt")
        public String info;
    }
    public class Dresssg{
        public String brf;
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        public String brf;
        @SerializedName("txt")
        public String info;
    }

    public class Sunny{
        public String brf;
        @SerializedName("txt")
        public String info;
    }
}
