package com.example.sumeng.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by sumeng on 2018/10/16.
 */

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        //指定布局文件
        setContentView(R.layout.select_city);

        //给后退按钮设置监听事件
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.title_back:
                //向intent发起方返回数据（key-value）
                Intent i = new Intent();
                i.putExtra("cityCode","101090104");
                setResult(RESULT_OK,i);
                //finish之后会执行intent发起方的回调函数
                finish();
                break;
            default:
                break;
        }
    }
}
