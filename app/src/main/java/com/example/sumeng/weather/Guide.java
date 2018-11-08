package com.example.sumeng.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.sumeng.utils.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumeng on 2018/11/7.
 */

public class Guide extends Activity implements ViewPager.OnPageChangeListener{

    //显示三个引导页
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    //为引导页增加小圆点
    private ImageView[] dots; //存放小圆点的集合
    private int[] ids = {R.id.iv1,R.id.iv2,R.id.iv3};

    //最后一页pageview添加button按钮
    private Button btn;

    @Override
    protected void  onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        setContentView(R.layout.guide);
        //引导页初始化
        initViews();
        //小圆点初始化
        initDots();
        btn = views.get(2).findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(Guide.this,MainActivity.class);
                startActivity(i);
                //finish是关闭该activity
                finish();
            }
        });
    }

    void initDots(){
        dots = new ImageView[views.size()];
        for(int i =0;i<views.size();i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }
    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.page1,null));
        views.add(inflater.inflate(R.layout.page2,null));
        views.add(inflater.inflate(R.layout.page3,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        //为pageviewer配置监听事件
        vp.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //视图发生变化时，调用该方法
    @Override
    public void onPageSelected(int position) {
        for (int a = 0;a<ids.length;a++){
            if(a==position){
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            }else {
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
