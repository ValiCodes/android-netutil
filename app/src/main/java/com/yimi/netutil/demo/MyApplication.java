package com.yimi.netutil.demo;

import android.app.Application;

import com.yimi.netutil.NetUtils;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetUtils.init(this, true);
    }
}
