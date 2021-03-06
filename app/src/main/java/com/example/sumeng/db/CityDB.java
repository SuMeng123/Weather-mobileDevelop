package com.example.sumeng.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.sumeng.bean.City;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumeng on 2018/10/16.
 */

public class CityDB {
    public static final String CITY_DB_NAME = "city.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;
    public CityDB(Context context, String path) {
        //通过文件path创建SQLiteDatabase对象。通过此对象就可以操作数据库
        db = context.openOrCreateDatabase(path, Context.MODE_PRIVATE, null);
    }
    //从数据库中读取城市列表
    public List<City> getAllCity() {
        List<City> list = new ArrayList<City>();
        Cursor c = db.rawQuery("SELECT * from " + CITY_TABLE_NAME, null);
        while (c.moveToNext()) {
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY,allFirstPY);
            list.add(item);
        }
        return list;
    }
}
