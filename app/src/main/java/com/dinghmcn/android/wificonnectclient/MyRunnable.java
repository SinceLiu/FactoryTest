package com.dinghmcn.android.wificonnectclient;

import com.dinghmcn.android.wificonnectclient.MainActivity;

public class MyRunnable implements Runnable {
    public static MainActivity Instance;

    private String msg;
    public MyRunnable(String msg)
    {
        this.msg = msg;
    }
    public void run()
    {
        Instance.outPutMessage(msg);
    }
}
