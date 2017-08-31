package com.xingkong.starsweather;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xingkong.starsweather.service.AutoUpdateService;
import com.xingkong.starsweather.util.SharePrefsManager;


public class SettingActivity extends AppCompatActivity {

    private Button back;

    private LinearLayout setting_voice;
    private ImageView image_voice;
    private boolean status_voice;

    private LinearLayout setting_notification;
    private ImageView image_notification;
    private Boolean status_notification;

    private LinearLayout setting_service;
    private ImageView image_service;
    private Boolean status_service;

    private LinearLayout setting_interval;
    private TextView text_interval;
    private String time;

    final String[] times={"1小时","2小时","6小时","12小时","24小时"};

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);

        setting_interval=(LinearLayout)findViewById(R.id.setting_interval);
        text_interval=(TextView)findViewById(R.id.interval_text) ;
        back=(Button)findViewById(R.id.settingBack_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_voice=(ImageView)findViewById(R.id.image_voice);
        setting_voice=(LinearLayout)findViewById(R.id.setting_voice);
        status_voice= SharePrefsManager.getBoolean("status_voice");

        if(status_voice){
            image_voice.setImageResource(R.drawable.slip_open);
        }else{
            image_voice.setImageResource(R.drawable.slip_close);
        }
        setting_voice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(status_voice){
                    image_voice.setImageResource(R.drawable.slip_close);
                    status_voice=false;
                    SharePrefsManager.setBoolean("status_voice",status_voice);
                }else{
                    image_voice.setImageResource(R.drawable.slip_open);
                    status_voice=true;
                    SharePrefsManager.setBoolean("status_voice",status_voice);
                }
                return false;
            }
        });

        setting_notification=(LinearLayout)findViewById(R.id.setting_notification);
        image_notification=(ImageView)findViewById(R.id.image_notification);
        status_notification=SharePrefsManager.getBoolean("status_notification");

        if(status_notification){
            image_notification.setImageResource(R.drawable.slip_open);
        }else{
            image_notification.setImageResource(R.drawable.slip_close);
        }

        setting_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status_notification){
                    image_notification.setImageResource(R.drawable.slip_close);
                    status_notification=false;
                    SharePrefsManager.setBoolean("status_notification",status_notification);
                    ViewPagerFragment.manager.cancelAll();
                }else{
                    image_notification.setImageResource(R.drawable.slip_open);
                    status_notification=true;
                    SharePrefsManager.setBoolean("status_notification",status_notification);
                }
            }
        });

        image_service=(ImageView)findViewById(R.id.image_service);
        setting_service=(LinearLayout)findViewById(R.id.setting_service);
        status_service= SharePrefsManager.getBoolean("status_service");
        if(status_service){
            image_service.setImageResource(R.drawable.slip_open);
        }else{
            image_service.setImageResource(R.drawable.slip_close);
        }

        setting_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status_service){
                    image_service.setImageResource(R.drawable.slip_close);
                    status_service=false;
                    setting_interval.setBackgroundColor(R.color.colorAccent);
                    setting_interval.setClickable(false);
                    SharePrefsManager.setBoolean("status_service",status_service);
                    Intent intent=new Intent(SettingActivity.this, AutoUpdateService.class);
                    stopService(intent);
                }else{
                    image_service.setImageResource(R.drawable.slip_open);
                    status_service=true;
                    setting_interval.setBackgroundColor(R.color.colorPrimary);
                    setting_interval.setClickable(true);
                    SharePrefsManager.setBoolean("status_service",status_service);
                    Intent intent1=new Intent(SettingActivity.this, AutoUpdateService.class);
                    startService(intent1);
                }
            }
        });

        time= SharePrefsManager.getString("interval");
        if(time!=null){
            text_interval.setText(time);
        }else{
            time=text_interval.getText().toString();
            SharePrefsManager.set("interval",time);
        }

        setting_interval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("更新间隔");
                int i=0;
                for(;i<times.length;i++){
                    if(time==times[i]){
                        break;
                    }
                }
                builder.setSingleChoiceItems(times, i, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(time!=times[which]){
                            text_interval.setText(times[which]);
                            SharePrefsManager.set("interval",times[which]);
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
        if(!status_service){
            setting_interval.setBackgroundColor(R.color.colorAccent);
            setting_interval.setClickable(false);
        }
    }
}
