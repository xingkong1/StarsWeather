package com.xingkong.starsweather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xingkong.starsweather.util.MyApplication;
import com.xingkong.starsweather.util.SharePrefsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by yanghongtao on 2017/8/22 0022.
 */

public class ViewPagerFragment extends FragmentActivity {


    private TextView titleCity;

    private Button navButton;

    public DrawerLayout drawerLayout;

    private static ViewPager viewPager;

    private LinearLayout numLayout;

    private   MyPagerAdapter adapter;

     private  List<String> weatherIds;

    private List<String> countyNames;

    private Boolean status_notification;

    public static NotificationManager manager;

    public static ViewPager getViewPager(){
        return viewPager;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String ids=prefs.getString("weatherIds",null);

        List<String> weatherList=new ArrayList<>();

        if(ids!=null){
            if(ids.contains(",")){
                List<String> weathers=Arrays.asList(ids.split(","));
                for(String weather:weathers){
                    weatherList.add(weather.split("/")[0]);
                }
            }else{
                weatherList.add(ids.split("/")[0]);
            }
        }
        if(weatherList.size()==weatherIds.size()){
            return;
        }else{
            adapter.updateDate(weatherList);
        }


        status_notification= SharePrefsManager.getBoolean("status_notification");

        if(status_notification){
            String cityName=ids.split(",")[0].split("/")[1];
            Log.w("city",cityName);
            postNotification(cityName);
        }



    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager);

        titleCity=(TextView)findViewById(R.id.title_city);
        navButton=(Button)findViewById(R.id.nav_button);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);

        manager=(NotificationManager)getSystemService(
                Context.NOTIFICATION_SERVICE);
        weatherIds=new ArrayList<>();
        countyNames=new ArrayList<>();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String ids=prefs.getString("weatherIds",null);
        if(ids!=null){
            if(ids.contains(",")){
                List<String> weathers=Arrays.asList(ids.split(","));
                for(String weather:weathers){
                    weatherIds.add(weather.split("/")[0]);
                    countyNames.add(weather.split("/")[1]);
                }
            }else{
                weatherIds.add(ids.split("/")[0]);
                countyNames.add(ids.split("/")[1]);
            }
        }

        viewPager=(ViewPager)findViewById(R.id.viewPager);

        viewPager.setOffscreenPageLimit(6);

        adapter=new MyPagerAdapter(getSupportFragmentManager(),weatherIds);

        viewPager.setAdapter(adapter);

        String position=getIntent().getStringExtra("position");

         if(position!=null){
            int p= weatherIds.indexOf(position);
             viewPager.setCurrentItem(p);
             titleCity.setText(countyNames.get(p));
         }else{
             viewPager.setCurrentItem(0);
             titleCity.setText(countyNames.get(0));
         }

        status_notification= SharePrefsManager.getBoolean("status_notification");

        if(status_notification){
            String cityName=ids.split(",")[0].split("/")[1];
            Log.w("city",cityName);
                postNotification(cityName);
            }

        navButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                   titleCity.setText(countyNames.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



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

    public void postNotification(String cityName) {
        String weather = SharePrefsManager.getString(cityName);
        String weatherInfo = weather.split(",")[0];
        String degree = weather.split(",")[1];
        String range=weather.split(",")[2];
        if (manager != null) {
            Intent intent = new Intent(this, ViewPagerFragment.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(this);
            switch (weatherInfo) {
                case "晴":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.sunny));
                    break;
                case "多云":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.cloudy));
                    break;
                case "阴":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.cloudy));
                    break;
                case "大雨":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.heavy_rain));
                    break;
                case "雷阵雨":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.thundershower));
                    break;
                case "阵雨":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.shower));
                    break;
                case "雪":
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.snow));
                    break;
            }
            builder.setContentTitle(degree+"  "+range);
            builder.setContentText(weatherInfo);
            builder.setWhen(System.currentTimeMillis());
            builder.setContentInfo(cityName);
            builder.setSmallIcon(R.mipmap.biaozhi);
            builder.setContentIntent(pi);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            manager.notify(1, notification);
        }
    }


    public  void  add(String weatherId){
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

        private  Fragment getFragment(int position){
            return mFragmentList.get(position);
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
