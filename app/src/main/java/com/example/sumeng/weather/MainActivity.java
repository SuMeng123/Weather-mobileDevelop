package com.example.sumeng.weather;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by sumeng on 2018/9/25.
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.weather_info);
    }
}
