package com.xingkong.starsweather;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xingkong.starsweather.db.City;
import com.xingkong.starsweather.db.County;
import com.xingkong.starsweather.db.Province;
import com.xingkong.starsweather.gson.Weather;
import com.xingkong.starsweather.service.AutoUpdateService;
import com.xingkong.starsweather.util.HttpUtil;
import com.xingkong.starsweather.util.Ifly;
import com.xingkong.starsweather.util.MyApplication;
import com.xingkong.starsweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 用于遍历省市县数据的碎片
 * Created by 17273 on 2017/8/13.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_SEARCH=0;

    public static final int LEVEL_PROVINCE=1;

    public  static final int LEVEL_CITY=2;

    public static final int LEVEL_COUNTY=3;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private MyAdapter adapter;

    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;

    private List<City> cityList;

    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    private EditText search_text;

    private ImageView search_button;

    private ImageView delete_button;

    private Weather selectWeather;

    private LinearLayout seach_frame;

   // private LinearLayout.LayoutParams linearParams ;

    private RelativeLayout title_frame;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        search_text=(EditText)view.findViewById(R.id.search_text);
        search_button=(ImageView)view.findViewById(R.id.search_button);
        delete_button=(ImageView)view.findViewById(R.id.delete_button);
        seach_frame=(LinearLayout)view.findViewById(R.id.search_frame);
        //linearParams =(LinearLayout.LayoutParams) seach_frame.getLayoutParams();
        title_frame=(RelativeLayout)view.findViewById(R.id.title_frame);
        adapter=new MyAdapter();
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        title_frame.setVisibility(View.GONE);
        seach_frame.setVisibility(View.VISIBLE);
        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    title_frame.setVisibility(View.VISIBLE);
                    seach_frame.setVisibility(View.GONE);
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    String countyName=countyList.get(position).getCountyName();
                    if(getActivity() instanceof MainActivity){
                        Intent intent=new Intent(getActivity(),ViewPagerFragment.class);
                        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                        SharedPreferences.Editor edit=prefs.edit();
                        edit.putString("weatherIds",weatherId+"/"+countyName);
                        edit.commit();
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof AddAreaActivity){
                        Intent intent=new Intent(getActivity(),ViewPagerFragment.class);
                        intent.putExtra("position",weatherId);
                        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                        String weatherIds=prefs.getString("weatherIds",null);
                        SharedPreferences.Editor edit=prefs.edit();
                        if(weatherIds==null){
                            edit.putString("weatherIds",weatherId+"/"+countyName);
                        }else{
                            edit.putString("weatherIds",weatherIds+","+weatherId+"/"+countyName);
                        }
                        edit.commit();
                        startActivity(intent);
                    }

                }else if(currentLevel==LEVEL_SEARCH){
                    if(selectWeather!=null){
                        String weatherId=selectWeather.basic.weatherId;
                        String countyName=selectWeather.basic.cityName;
                        Intent intent=new Intent(getActivity(),ViewPagerFragment.class);
                        intent.putExtra("position",weatherId);
                        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                        String weatherIds=prefs.getString("weatherIds",null);
                        SharedPreferences.Editor edit=prefs.edit();
                        if(weatherIds==null){
                            edit.putString("weatherIds",weatherId+"/"+countyName);
                        }else{
                            edit.putString("weatherIds",weatherIds+","+weatherId+"/"+countyName);
                        }
                        edit.commit();
                        startActivity(intent);
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    seach_frame.setVisibility(View.VISIBLE);
                    title_frame.setVisibility(View.GONE);
                    queryProvinces();
                }else if(currentLevel==LEVEL_PROVINCE){
                      getActivity().finish();
                }
            }
        });


        search_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if(s.length()>0){
                    search_button.setVisibility(View.GONE);
                    delete_button.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            search(s.toString().trim());
                        }
                    }).start();
                }else{
                    search_button.setVisibility(View.VISIBLE);
                    delete_button.setVisibility(View.GONE);
                    queryProvinces();
                }
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_text.setText("");
                InputMethodManager imm=(InputMethodManager)getContext().getSystemService(
                        getContext().INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
                queryProvinces();
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ifly.Dialoginit(getActivity(),search_text);
            }
        });
    }

    /**
     * 根据关键字查找城市
     * @param key
     */
    private void search(String key){
       countyList=DataSupport.where("countyName=?",
               key).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    listView.setSelection(0);
                }
            });
            currentLevel=LEVEL_COUNTY;
        }else{
            String address="https://api.heweather.com/v5/search?" +
                    "city="+key+"&key=3641ea7c9cde405daa16d2cc80a60ec0";
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"查询失败",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Log.w("search",responseText);
                    Weather weather= Utility.handleWeatherResponse(responseText);
                    StringBuffer city=new StringBuffer();
                    if("ok".equals(weather.status.trim())){
                        city.append(weather.basic.cityName).
                                append(",").append(weather.basic.prov).
                                append(",").append(weather.basic.cnty);
                        selectWeather=weather;
                    }else{
                        city.append("没有匹配的结果");
                        selectWeather=null;
                    }
                    dataList.clear();
                    Log.w("search",city.toString());
                    dataList.add(city.toString());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            listView.setSelection(0);
                        }
                    });
                    currentLevel=LEVEL_SEARCH;
                }
            });
        }
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFormServer(String address,final String type){
         showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("county".equals(type)){
                    result=Utility.handleCountyReponse(responseText,selectedCity.getId());
                }else if("city".equals(type)){
                    result=Utility.hanlerCityResponse(responseText,selectedProvince.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }


    /**
     * 查询全国所以的省，优先从数据库查询，如果没有查询再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
                Log.w("province",province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
             queryFormServer(address,"province");
        }
    }

    /**
     * 查询全国所有的市，优先从数据库查询，如果没有查询再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",
                String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFormServer(address,"city");
        }
    }

    /**
     * 查询全国所有的县，优先从数据库查询，如果没有查询再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",
                String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFormServer(address,"county");
        }
    }

    public  class MyAdapter extends BaseAdapter{

        private LayoutInflater layoutInflater=LayoutInflater.from(MyApplication.getContext());



        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder=null;
            if(convertView==null){
               viewHolder=new ViewHolder();
                convertView=layoutInflater.inflate(R.layout.city_item,null);
                viewHolder.image=(ImageView)convertView.findViewById(R.id.city_image);
                viewHolder.text=(TextView)convertView.findViewById(R.id.city_name);
                convertView.setTag(viewHolder);
            }else{
                viewHolder=(ViewHolder)convertView.getTag();
            }
            viewHolder.text.setText(dataList.get(position));
            return convertView;
        }
    }

    public final class  ViewHolder{
        public ImageView image;
        public TextView text;
    }

}
