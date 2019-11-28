package com.dailyyoga.audiofocusactivity;

import android.app.Application;

/**
 * @author: ZhaoJiaXing@gmail.com
 * @created on: 2019/11/28 14:33
 * @description:
 */
public class AudioApplication extends Application {

    public static AudioApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}
