package com.melissa.mikochat;


import android.app.Application;
import android.content.Context;

public class ReLaunchActivity extends Application {

    private static Context mContext;
    public static ReLaunchActivity instace;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        instace = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static ReLaunchActivity getIntance() {
        return instace;
    }
}