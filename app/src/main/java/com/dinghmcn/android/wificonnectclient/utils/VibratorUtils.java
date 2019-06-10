
package com.dinghmcn.android.wificonnectclient.utils;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.content.Context.VIBRATOR_SERVICE;

public class VibratorUtils  {
    private android.os.Vibrator mVibrator;
    private Context mContext;
    private static VibratorUtils instance;
    public VibratorUtils(Context mContext) {
        this.mContext = mContext;
        startVibrator();
    }

    private void startVibrator() {
        mVibrator = (android.os.Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        mVibrator.vibrate(10000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mVibrator.cancel();
            }
        },5000);
    }

    public static VibratorUtils getInstance(Context mContext){
        if (instance==null)
            instance=new VibratorUtils(mContext);
        return instance;
    }

}
