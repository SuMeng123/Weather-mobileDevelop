package com.example.sumeng.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.sumeng.app.MyApplication;
import com.example.sumeng.bean.City;
//import com.example.sumeng.utils.ClearEditText;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sumeng on 2018/10/16.
 */

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;

    private EditText mEditText;
    //存城市名字的数组
    List<String> cityName = new ArrayList<String>();
    //存城市编码的数组
    List<String> cityNum = new ArrayList<String>();
    //获得CityList
    ArrayList<City> myCityList = (ArrayList<City>) MyApplication.getInstance().getCityList();
    //获得myCityList的长度
    int City_length = myCityList.size();

    //初始化适配器
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        mBackBtn = (ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        selectCityMsgByPY(myCityList,"");

        ListView mlistView = (ListView)findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(SelectCity.this, android.R.layout.simple_list_item_1,cityName);
        mlistView.setAdapter(adapter);
        //设置点击效果
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();
                i.putExtra("cityCode", cityNum.get(position));
                setResult(RESULT_OK, i);
                finish();
            }
        });
        //获取EditText
        mEditText = (EditText)findViewById(R.id.search_edit);
        //添加监听器
        mEditText.addTextChangedListener(mTextWatcher);

    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart ;
        private int editEnd ;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            temp = s;
            Log.d("myapp", "beforeTextChanged:" + temp) ;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            selectCityMsgByPY(myCityList, s.toString());
            adapter.notifyDataSetChanged();
            Log.d("myapp","onTextChanged:"+s) ;
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = mEditText.getSelectionStart();
            editEnd = mEditText.getSelectionEnd();
            if (temp.length() > 10) {
                Toast.makeText(SelectCity.this,"你输⼊入的字数已经超过了限制！", Toast.LENGTH_SHORT).show();
                s.delete(editStart-1, editEnd);
                int tempSelection = editStart;
                mEditText.setText(s);
                mEditText.setSelection(tempSelection);
            }
            Log.d("myapp","afterTextChanged:") ;
        }
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.title_back:
                Intent i = new Intent();
                i.putExtra("cityCode", "101160101");
                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;

        }
    }

    //根据拼音给出城市信息
    public void selectCityMsgByPY(List<City> myCityList, String PY){
        cityName.clear();
        cityNum.clear();

        //给城市属性数组赋值
        for(int i = 0; i < City_length; i ++){
            if(comparePY(myCityList.get(i), PY))
            {
                cityName.add(myCityList.get(i).getCity());
                cityNum.add(myCityList.get(i).getNumber());
                //Log.d("selectCITY",myCityList.get(i).getCity()+":"+myCityList.get(i).getNumber());
            }
        }
    }

    //城市拼音匹配判断
    public boolean comparePY(City city ,String PY){
        String AllPY = city.getAllFristPY();
        if(AllPY.toLowerCase().contains(PY.toLowerCase()))
        {
            return true;
        }
        else
            return false;
    }

}
