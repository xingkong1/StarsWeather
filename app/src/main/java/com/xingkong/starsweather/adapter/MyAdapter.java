package com.xingkong.starsweather.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by yanghongtao on 2017/8/21 0021.
 */

public class MyAdapter extends PagerAdapter {

    List<View> viewList;

    public MyAdapter(List<View> viewList){
        this.viewList=viewList;
    }


    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        container.addView(viewList.get(position),0);
        return viewList.get(position);
    }
}
