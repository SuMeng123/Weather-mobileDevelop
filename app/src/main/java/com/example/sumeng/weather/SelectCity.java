package com.example.sumeng.weather;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sumeng.app.MyApplication;
import com.example.sumeng.bean.City;

import java.util.ArrayList;
import java.util.List;

import static com.example.sumeng.app.MyApplication.getInstance;


/**
 * Created by sumeng on 2018/10/16.
 */

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;

    private String[] data={"第1组","第2组","第3组","第4组","第5组","第6组", "第7组","第8组","第9组","第10组","第11组","第12组","第13组", "第14组","第15组","第16组","第17组","第18组","第19组","第20组",
            "第21组","第22组"};

    private ListView mlistView;

    private MyApplication application;

    private List<City> mCityList;

    private City[] dataCity;

    private List<String> dataCityName;

    private String[] dataStr;

    //城市编号
    private String cityCode= "";
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        //指定布局文件
        setContentView(R.layout.select_city);

        //给后退按钮设置监听事件
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        //获得城市信息list
        application = getInstance();
        mCityList = application.getCityList();
        dataCityName = new ArrayList<>();
        for (int i=0;i<mCityList.size();i++){
            dataCityName.add(mCityList.get(i).getCity());
        }
        dataStr =  dataCityName.toArray(new String[dataCityName.size()]);

        //显示城市信息list
        mlistView = (ListView)findViewById(R.id.list_view);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,dataStr);
        mlistView.setAdapter(adapter);

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Toast.makeText(SelectCity.this, "你点击城市的编码是:"+mCityList.get(i).getNumber(),Toast.LENGTH_SHORT).show();
                cityCode = mCityList.get(i).getNumber();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.title_back:
                //向intent发起方返回数据（key-value）
                Intent i = new Intent();
                i.putExtra("cityCode",cityCode);
                setResult(RESULT_OK,i);
                //finish之后会执行intent发起方的回调函数
                finish();
                break;
            default:
                break;
        }
    }
}
