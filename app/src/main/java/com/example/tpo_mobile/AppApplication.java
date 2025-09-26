package com.example.tpo_mobile;


import android.app.Application;

import com.example.tpo_mobile.session.SessionHolder;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AppApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        SessionHolder.init(this); // <-- importante
    }
}
