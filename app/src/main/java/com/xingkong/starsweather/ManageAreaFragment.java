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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xingkong.starsweather.db.City;
import com.xingkong.starsweather.db.County;
import com.xingkong.starsweather.db.Province;
import com.xingkong.starsweather.service.AutoUpdateService;
import com.xingkong.starsweather.util.HttpUtil;
import com.xingkong.starsweather.util.MyApplication;
import com.xingkong.starsweather.util.SharePrefsManager;
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
 * 城市管理的碎片
 * Created by 17273 on 2017/8/28.
 */

public class ManageAreaFragment extends Fragment {

    public static  final  int LEVEL_MANAGER=0;

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

    private Button addButton;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        addButton=(Button)view.findViewById(R.id.add_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new MyAdapter();
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cityMannager();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_MANAGER){
                    ViewPagerFragment.getViewPager().setCurrentItem(position);
                    getActivity().finish();
                } else if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    String countyName=countyList.get(position).getCountyName();
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
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String countyName=dataList.get(position);
                if (currentLevel == LEVEL_MANAGER) {
                    AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
                    dialog.setTitle("城市管理");
                    dialog.setMessage("确定删除？");
                    dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                            String weatherIds=prefs.getString("weatherIds",null);
                            if((!weatherIds.isEmpty())&&weatherIds.contains(",")){
                                String[] weathers= weatherIds.split(",");
                                for(String w:weathers){
                                    if(w.contains(countyName))
                                        weatherIds= weatherIds.replace(","+w,"");
                                }
                            }else{
                                weatherIds=null;
                            }
                            SharedPreferences.Editor edit= prefs.edit();
                            edit.putString("weatherIds",weatherIds);
                            edit.putString(countyName,null);
                            edit.commit();
                            dataList.remove(position);
                            adapter.notifyDataSetChanged();
                            listView.setSelection(0);

                        }
                    });
                    dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    return true;
                }else{
                    return false;
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }else if(currentLevel==LEVEL_PROVINCE){
                    cityMannager();
                }else if(currentLevel==LEVEL_MANAGER){
                    getActivity().finish();
                }
            }
        });
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
     * 城市管理
     */
    private void cityMannager(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String weatherIds=prefs.getString("weatherIds",null);
        if(weatherIds==null){
            backButton.setVisibility(View.GONE);
            queryProvinces();
        }else{
            titleText.setText("城市管理");
            addButton.setVisibility(View.VISIBLE);
            addButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    addButton.setVisibility(View.GONE);
                    queryProvinces();
                }
            });
            dataList.clear();
            if(!weatherIds.isEmpty()&&weatherIds.contains(",")){
                List<String> weathers= Arrays.asList(weatherIds.split(","));
                for(String weather:weathers){
                    dataList.add(weather.split("/")[1]);
                }
            }else{
                dataList.add(weatherIds.split("/")[1]);
            }
            currentLevel=LEVEL_MANAGER;
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
        }
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
            currentLevel=LEVEL_PROVINCE;
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
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
            currentLevel=LEVEL_CITY;
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
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

    public  class MyAdapter extends BaseAdapter {

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
                viewHolder.text.setText(dataList.get(position));
                if(currentLevel==LEVEL_MANAGER){
                   String weather=SharePrefsManager.getString(dataList.get(position));
                    if(weather!=null){
                        String[] weathers=weather.split(",");
                        viewHolder.text.setText(dataList.get(position)
                                +"                           "+weathers[1]);
                        switch (weathers[0]){
                            case "晴":
                                viewHolder.image.setImageResource(R.drawable.sunny);
                                break;
                            case "多云":
                                viewHolder.image.setImageResource(R.drawable.cloudy);
                                break;
                            case "阴":
                                viewHolder.image.setImageResource(R.drawable.yintian);
                                break;
                            case "大雨":
                                viewHolder.image.setImageResource(R.drawable.heavy_rain);
                                break;
                            case "阵雨":
                                viewHolder.image.setImageResource(R.drawable.shower);
                                break;
                            case "雷阵雨":
                                viewHolder.image.setImageResource(R.drawable.thundershower);
                                break;
                            case "雪":
                                viewHolder.image.setImageResource(R.drawable.snow);
                                break;

                        }
                    }
                }

            }else{
                viewHolder=(ViewHolder)convertView.getTag();
                viewHolder.text.setText(dataList.get(position));
            }

            return convertView;
        }
    }

    public final class  ViewHolder{
        public ImageView image;
        public TextView text;
    }
}

