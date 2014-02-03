package com.baasbox.android.pushybox;

import android.app.Application;
import com.baasbox.android.BaasBox;

/**
 * Created by Andrea Tortorella on 28/01/14.
 */
public class PushyBox extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Class.forName("android.os.AsyncTask", true,this.getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        BaasBox.initDefault(this,Config.BAASBOX_CONFIG);
    }
}
