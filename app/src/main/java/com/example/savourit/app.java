package com.example.savourit;

import android.app.Application;

import com.example.savourit.utils.UserUtils;

public class app extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UserUtils.init(this);
    }
}
