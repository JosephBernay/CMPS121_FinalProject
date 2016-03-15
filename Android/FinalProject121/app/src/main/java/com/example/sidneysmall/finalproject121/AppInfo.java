package com.example.sidneysmall.finalproject121;

import android.content.Context;

/**
 * Created by Joseph on 3/10/2016.
 */
public class AppInfo {

    private static AppInfo instance = null;

    protected AppInfo() {
        // Exists only to defeat instantiation.
    }

    // Here are some values we want to keep global.
    public String key;
    public String email;
    public String computerName;

    public static AppInfo getInstance(Context context) {
        if(instance == null) {
            instance = new AppInfo();
            instance.key = "GAmaW6d90bvBKDOw60b68e9t10356Dqy";
            instance.email = "jbernay@ucsc.edu";
            instance.computerName = "SEPHIROTH";
        }
        return instance;
    }

}
