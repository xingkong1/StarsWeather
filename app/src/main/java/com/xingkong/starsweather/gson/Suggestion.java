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

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class Dresssg{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
