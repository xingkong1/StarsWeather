package com.xingkong.starsweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

import com.xingkong.starsweather.util.MyApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yanghongtao on 2017/8/22 0022.
 */

public class ViewPagerFragment extends FragmentActivity {

    private ViewPager viewPager;

    private LinearLayout numLayout;

    private MyPagerAdapter adapter;

     private List<String> weatherIds;

    private List<String> countyNames;

    public ViewPager getViewPager(){
        return viewPager;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager);
        weatherIds=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String ids=prefs.getString("weatherIds",null);
        if(ids!=null){
            if(ids.contains(",")){
                List<String> weathers=Arrays.asList(ids.split(","));
                for(String weather:weathers){
                    weatherIds.add(weather.split("/")[0]);
                }
            }else{
                weatherIds.add(ids.split("/")[0]);
            }
        }

        viewPager=(ViewPager)findViewById(R.id.viewPager);

        viewPager.setOffscreenPageLimit(6);

        adapter=new MyPagerAdapter(getSupportFragmentManager(),weatherIds);

        viewPager.setAdapter(adapter);



        /**
        FrameLayout viewGroup= (FrameLayout) (LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.activity_weather,null));
        numLayout=(LinearLayout) viewGroup.findViewById(R.id.llGuideGroup);
        numLayout.removeAllViews();
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.icon_dot_normal);
        for(int i=0;i<pages.size();i++){
            Log.w("button",String.valueOf(i));
            ImageView button=new ImageButton(this);
            button.setLayoutParams(new ViewGroup.LayoutParams(bitmap.getWidth(),bitmap.getHeight()));
            button.setScaleType(ImageView.ScaleType.CENTER);
            // button.setImageResource(i==0?R.drawable.icon_dot_select:R.drawable.icon_dot_normal);
            button.setImageResource(R.drawable.dot);
            numLayout.addView(button);
        }
         */
    }

    public void add(String weatherId){
         weatherIds.add(weatherId);
        adapter.updateDate(weatherIds);
        viewPager.setCurrentItem(weatherId.length()-1);
    }

    public void delete(int position){
           weatherIds.remove(position);
          if(weatherIds.isEmpty()){
              Intent intent=new Intent(this,MainActivity.class);
              startActivity(intent);
              finish();
              return;
        }
        adapter.updateDate(weatherIds);
        viewPager.setCurrentItem(0);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> mFragmentList;

        private FragmentManager fragmentManager;

        public MyPagerAdapter(FragmentManager fm, List<String> weatherIds) {
            super(fm);
            this.fragmentManager=fm;
            mFragmentList=new ArrayList<>();
            for(int i=0;i<weatherIds.size();i++){
                WeatherFragment fragment=new WeatherFragment();
                Bundle bundle = new Bundle();
                bundle.putString("weatherId", weatherIds.get(i));
                fragment.setArguments(bundle);
                mFragmentList.add(fragment);
            }
        }

        public void updateDate(List<String> dataList){
           ArrayList<Fragment> fragments=new ArrayList<>();
            for(int i=0;i<dataList.size();i++){
                WeatherFragment fragment=new WeatherFragment();
                Bundle bundle = new Bundle();
                bundle.putString("weatherId", dataList.get(i));
                fragment.setArguments(bundle);
                fragments.add(fragment);
            }
            setFragments(fragments);
        }

        private void setFragments(ArrayList<Fragment> fragments){
            if(this.mFragmentList!=null){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                for(Fragment f:this.mFragmentList){
                    fragmentTransaction.remove(f);
                }
                fragmentTransaction.commit();
                fragmentManager.executePendingTransactions();
            }
            this.mFragmentList=fragments;
            notifyDataSetChanged();
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


}
