package com.example.sumeng.weather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.sumeng.bean.MyLocationListerner;
import com.example.sumeng.bean.TodayWeather;
import com.example.sumeng.utils.ViewPagerAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.InvalidMarkException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumeng on 2018/9/25.
 */

public class MainActivity extends Activity implements View.OnClickListener ,ViewPager.OnPageChangeListener{

    private static final int UPDATE_TODAY_WEATHER = 1;

    private ImageView mUpdateBtn;

    private ImageView mtitleLocation;

    private ImageView mCitySelect;

    public LocationClient mLocationClient = null;
    private MyLocationListerner myListener = new MyLocationListerner();
    //初始化界面控件
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    //定义progressbar控件
    private ProgressBar mUpdateProgressBar;//刷新按钮动画

    //定位按钮
    private ImageView mTitleLocation;

    //六天天气信息展示
    //显示两个展示页
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;
    //为引导页增加小圆点
    private ImageView[] dots; //存放小圆点的集合
    private int[] ids = {R.id.iv1,R.id.iv2};
    private TextView week_today,temperature,climate,wind,week_today1,temperature1,climate1,wind1,week_today2,temperature2,climate2,wind2;
    private TextView week_today3,temperature3,climate3,wind3,week_today4,temperature4,climate4,wind4,week_today5,temperature5,climate5,wind5;


    /**
     * 主线程
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    mUpdateProgressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.weather_info);

        //为刷新按钮添加监听事件
        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        //为城市管理按钮添加监听事件
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        //为定位按钮添加监听事件
        mtitleLocation = (ImageView) findViewById(R.id.title_location);
        mtitleLocation.setOnClickListener(this);

        mUpdateProgressBar = (ProgressBar) findViewById(R.id.title_update_progress);

        //初始化两个滑动页面
        initViews();
        //小圆点初始化
        initDots();
        //初始化界面控件
        initView();

    }

    @Override
    public void onClick(View view) {

        //intent 解决Android应用的各组件间的通信
        //四大组件 1\activity  2\service  3\content Providers  4\broadcast reseivers
        //intent包含对其他组件的意图描述信息
        //android 根据intent描述，找到相应组件
        //包含组件名称（组件名称都是独一的）
        if(view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this,SelectCity.class);
            //startActivity(i);
            //启动activity之后，等待新activity传回来的数据
            startActivityForResult(i,1);
        }


        if(view.getId()==R.id.title_update_btn){
            //获取SharedPreferences实例（因为是接口，所以不能直接new,系统为我们提供了方法）
            //String name 保存的文件名
            //int mode  操作文件的模式,下面是四种操作模式的详解
            //Context.MODE_PRIVATE：为默认操作模式，代表该文件是私有数据，只能被应用本身访问，在该模式下，写入的内容会覆盖原文件的内容，如果想把新写入的内容追加到原文件中。可以使用Context.MODE_APPEND
            //Context.MODE_APPEND：模式会检查文件是否存在，存在就往文件追加内容，否则就创建新文件。
            //Context.MODE_WORLD_READABLE和Context.MODE_WORLD_WRITEABLE用来控制其他应用是否有权限读写该文件。
            //MODE_WORLD_READABLE：表示当前文件可以被其他应用读取；
            //MODE_WORLD_WRITEABLE：表示当前文件可以被其他应用写入。
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            //如果没有main_city_code对应的值，则规定默认返回值是101010100
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather",cityCode);
            queryWeatherCode(cityCode);
        }

        if (view.getId()==R.id.title_location){
//            setUpdateProgress();
            startLocate();
            if(mLocationClient.isStarted()){
                mLocationClient.stop();
            }
            mLocationClient.start();
            final Handler BDHandler = new Handler(){
                public void handleMessage(Message msg){
                    switch (msg.what){
                        case UPDATE_TODAY_WEATHER:
                            if(msg.obj!=null){
                                queryWeatherCode(myListener.cityCode);
                            }
                            myListener.cityCode=null;
                            break;
                        default:
                            break;
                    }
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = myListener.cityCode;
                        BDHandler.sendMessage(msg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();


        }
    }

    /**
     * 根据citycode查询城市天气信息
     * @param cityCode
     */
    private void queryWeatherCode(String cityCode) {
        mUpdateBtn.setVisibility(View.GONE);
        mUpdateProgressBar.setVisibility(View.VISIBLE);

        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                TodayWeather todayWeather = null;
                HttpURLConnection con=null;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection(
                    );
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    //解析xml文件
                    todayWeather = parseXML(responseStr);
                    try {
                        Thread.currentThread().sleep(1000);
                    }catch (Exception e){}
                    if (todayWeather != null) {
                        Log.d("myWeather", todayWeather.toString());

                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {

                    if(con != null){
                        con.disconnect();
                    }
//                    try {
//                        Thread.currentThread().sleep(10000);
////                        findViewById(R.id.title_update_progress).setVisibility(View.GONE);
////                        findViewById(R.id.title_update_btn).setVisibility(View.VISIBLE);
//                    }catch (Exception e){}
                }
            }
        }).start();
    }

    /**
     * 解析xml文件
     * @param xmldata
     */
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather= new TodayWeather();
                        }
                        if (xmlPullParser.getName().equals("city")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setCity(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("updatetime")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setUpdatetime(xmlPullParser.getText());
                        }else if (xmlPullParser.getName().equals("shidu")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setShidu(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("wendu")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWendu(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("pm25")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setPm25(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("quality")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setQuality(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setFengxiang(xmlPullParser.getText());
                            fengxiangCount++;
                        }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setFengli(xmlPullParser.getText());
                            todayWeather.setWind1(xmlPullParser.getText());
                            fengliCount++;
                        }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWind2(xmlPullParser.getText());
                            fengliCount++;
                        }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWind3(xmlPullParser.getText());
                            fengliCount++;
                        }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWind4(xmlPullParser.getText());
                            fengliCount++;
                        }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 4) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWind5(xmlPullParser.getText());
                            fengliCount++;
                        }else if (xmlPullParser.getName().equals("fl_1")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWind(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setDate(xmlPullParser.getText());
                            todayWeather.setWeek_today1(xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWeek_today2(xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWeek_today3(xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWeek_today4(xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("date") && dateCount == 4) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWeek_today5(xmlPullParser.getText());
                            dateCount++;
                        } else if (xmlPullParser.getName().equals("date_1")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setWeek_today(xmlPullParser.getText());
                        } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setHigh(xmlPullParser.getText());
                            todayWeather.setTemperatureH1(xmlPullParser.getText().split(" ")[1]);
                            highCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureH2(xmlPullParser.getText().split(" ")[1]);
                            highCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureH3(xmlPullParser.getText().split(" ")[1]);
                            highCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureH4(xmlPullParser.getText().split(" ")[1]);
                            highCount++;
                        } else if (xmlPullParser.getName().equals("high") && highCount == 4) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureH5(xmlPullParser.getText().split(" ")[1]);
                            highCount++;
                        } else if (xmlPullParser.getName().equals("high_1")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureH(xmlPullParser.getText().split(" ")[1]);
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setLow(xmlPullParser.getText().split(" ")[1]);
                            todayWeather.setTemperatureL1(xmlPullParser.getText().split(" ")[1]);
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureL2(xmlPullParser.getText().split(" ")[1]);
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureL3(xmlPullParser.getText().split(" ")[1]);
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureL4(xmlPullParser.getText().split(" ")[1]);
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("low") && lowCount == 4) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureL5(xmlPullParser.getText().split(" ")[1]);
                            lowCount++;
                        } else if (xmlPullParser.getName().equals("low_1")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setTemperatureL(xmlPullParser.getText().split(" ")[1]);
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                            eventType = xmlPullParser.next();
                            todayWeather.setType(xmlPullParser.getText());
                            todayWeather.setClimate1(xmlPullParser.getText());
                            typeCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                            eventType = xmlPullParser.next();
                            todayWeather.setClimate2(xmlPullParser.getText());
                            typeCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                            eventType = xmlPullParser.next();
                            todayWeather.setClimate3(xmlPullParser.getText());
                            typeCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                            eventType = xmlPullParser.next();
                            todayWeather.setClimate4(xmlPullParser.getText());
                            typeCount++;
                        } else if (xmlPullParser.getName().equals("type") && typeCount == 4) {
                            eventType = xmlPullParser.next();
                            todayWeather.setClimate5(xmlPullParser.getText());
                            typeCount++;
                        } else if (xmlPullParser.getName().equals("type_1")) {
                            eventType = xmlPullParser.next();
                            todayWeather.setClimate(xmlPullParser.getText());
                        }
                        break;
                    // 判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    /**
     * 初始化界面控件
     */
    void initView() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality
        );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature
        );
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        //
        week_today=views.get(0).findViewById(R.id.week_today);
        temperature=views.get(0).findViewById(R.id.temperature);
        climate=views.get(0).findViewById(R.id.climate);
        wind=views.get(0).findViewById(R.id.wind);
        //
        week_today1=views.get(0).findViewById(R.id.week_today1);
        temperature1=views.get(0).findViewById(R.id.temperature1);
        climate1=views.get(0).findViewById(R.id.climate1);
        wind1=views.get(0).findViewById(R.id.wind1);
        //
        week_today2=views.get(0).findViewById(R.id.week_today2);
        temperature2=views.get(0).findViewById(R.id.temperature2);
        climate2=views.get(0).findViewById(R.id.climate2);
        wind2=views.get(0).findViewById(R.id.wind2);
        //
        week_today3=views.get(1).findViewById(R.id.week_today);
        temperature3=views.get(1).findViewById(R.id.temperature);
        climate3=views.get(1).findViewById(R.id.climate);
        wind3=views.get(1).findViewById(R.id.wind);
        //
        week_today4=views.get(1).findViewById(R.id.week_today1);
        temperature4=views.get(1).findViewById(R.id.temperature1);
        climate4=views.get(1).findViewById(R.id.climate1);
        wind4=views.get(1).findViewById(R.id.wind1);
        //
        week_today5=views.get(1).findViewById(R.id.week_today2);
        temperature5=views.get(1).findViewById(R.id.temperature2);
        climate5=views.get(1).findViewById(R.id.climate2);
        wind5=views.get(1).findViewById(R.id.wind2);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        week_today.setText("N/A");
        temperature.setText("N/A");
        climate.setText("N/A");
        wind.setText("N/A");
        week_today1.setText("N/A");
        temperature1.setText("N/A");
        climate1.setText("N/A");
        wind1.setText("N/A");
        week_today2.setText("N/A");
        temperature2.setText("N/A");
        climate2.setText("N/A");
        wind2.setText("N/A");
        week_today3.setText("N/A");
        temperature3.setText("N/A");
        climate3.setText("N/A");
        wind3.setText("N/A");
        week_today4.setText("N/A");
        temperature4.setText("N/A");
        climate4.setText("N/A");
        wind4.setText("N/A");
        week_today5.setText("N/A");
        temperature5.setText("N/A");
        climate5.setText("N/A");
        wind5.setText("N/A");
    }

    /**
     * 更新UI中的控件
     * @param todayWeather
     */
    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        //新增六天天气信息
        week_today.setText(todayWeather.getWeek_today());
        temperature.setText(todayWeather.getTemperatureL()+"~"+todayWeather.getTemperatureH());
        climate.setText(todayWeather.getClimate());
        wind.setText(todayWeather.getWind());
        week_today1.setText(todayWeather.getWeek_today1());
        temperature1.setText(todayWeather.getTemperatureL1()+"~"+todayWeather.getTemperatureH1());
        climate1.setText(todayWeather.getClimate1());
        wind1.setText(todayWeather.getWind1());
        week_today2.setText(todayWeather.getWeek_today2());
        temperature2.setText(todayWeather.getTemperatureL2()+"~"+todayWeather.getTemperatureH2());
        climate2.setText(todayWeather.getClimate2());
        wind2.setText(todayWeather.getWind2());
        week_today3.setText(todayWeather.getWeek_today3());
        temperature3.setText(todayWeather.getTemperatureL3()+"~"+todayWeather.getTemperatureH3());
        climate3.setText(todayWeather.getClimate3());
        wind3.setText(todayWeather.getWind3());
        week_today4.setText(todayWeather.getWeek_today4());
        temperature4.setText(todayWeather.getTemperatureL4()+"~"+todayWeather.getTemperatureH4());
        climate4.setText(todayWeather.getClimate4());
        wind4.setText(todayWeather.getWind4());
        week_today5.setText(todayWeather.getWeek_today5());
        temperature5.setText(todayWeather.getTemperatureL5()+"~"+todayWeather.getTemperatureH5());
        climate5.setText(todayWeather.getClimate5());
        wind5.setText(todayWeather.getWind5());
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }

    /**
     * "Intent发起方"实现该方法，与startActivityForResult()搭配使用
     * 该方法是个回调函数，返回方会向发起方返回数据，发起方接受数据，返回方active结束执行finish()便会执行回调函数
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "选择的城市代码为" + newCityCode);
            queryWeatherCode(newCityCode);
        }
    }

    private void startLocate(){
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd0911");
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    //初始化校小圆点
    void initDots(){
        dots = new ImageView[views.size()];
        for(int i =0;i<views.size();i++){
            dots[i]=(ImageView)findViewById(ids[i]);
        }
    }
    //六天天气信息展示
    private void initViews(){
        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<View>();
        views.add(inflater.inflate(R.layout.sixday1,null));
        views.add(inflater.inflate(R.layout.sixday2,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager) findViewById(R.id.viewpager);
        vp.setAdapter(vpAdapter);
        //为pageviewer配置监听事件
        vp.setOnPageChangeListener(this);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

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
