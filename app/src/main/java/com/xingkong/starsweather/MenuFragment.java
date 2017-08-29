package com.xingkong.starsweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by 17273 on 2017/8/28.
 */

public class MenuFragment extends Fragment implements View.OnClickListener {

    private View view;

    private LinearLayout city_add;

    private LinearLayout city_manage;

    private LinearLayout setting;

    private LinearLayout about;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.menu,container,false);
        city_add=(LinearLayout) view.findViewById(R.id.city_add);
        city_add.setOnClickListener(this);
        city_manage=(LinearLayout)view.findViewById(R.id.city_manage);
        city_manage.setOnClickListener(this);
        setting=(LinearLayout)view.findViewById(R.id.setting);
        setting.setOnClickListener(this);
        about=(LinearLayout)view.findViewById(R.id.about);
        about.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.city_add:
                Intent intent1=new Intent(getActivity(),AddAreaActivity.class);
                startActivity(intent1);
                break;
            case R.id.city_manage:
                Intent intent2=new Intent(getActivity(),ManageAreaActivity.class);
                startActivity(intent2);
                break;
            case R.id.setting:
                Intent intent3=new Intent(getActivity(),SettingActivity.class);
                startActivity(intent3);
                break;
            case R.id.about:
                Intent intent4=new Intent(getActivity(),AboutActivity.class);
                startActivity(intent4);
                break;
        }
    }
}
