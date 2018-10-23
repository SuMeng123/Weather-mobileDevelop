package com.example.sumeng.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.example.sumeng.bean.City;
import com.example.sumeng.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumeng on 2018/10/16.
 */

public class MyApplication extends Application{
    private static final String TAG = "MyAPP";

    private static MyApplication mApplication;

    //application对象是全局的单例的，
    //在不同的activity/service都会获得同一个对象，通过applicarion可以进行数据传递共享缓存

    private CityDB mCityDB;

    private List<City> mCityList;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");

        mApplication = this;
        mCityDB = openCityDB();
        initCityList();
    }

    public  static  MyApplication getInstance(){
        return mApplication;
    }

    /**
     * 根据db文件创建CityDB对象，CityDB对象来操作数据库增删改查
     * @return 返回CityDB对象
     */
    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

    /**
     * 初始化mCityList
     */
    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                prepareCityList();
            }
        }).start();
    }

    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        int i=0;
        for (City city : mCityList) {
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    public List<City> getCityList() {
        return mCityList;
    }
}

