package com.dinghmcn.android.wificonnectclient;

/**
 * Created by 1 on 2017/2/20.
 */

public class NvInfo {
    int nvID;
    byte[] data = new byte[256];
	byte[] reply = new byte[256];

    public NvInfo(int nvID){
        this.nvID = nvID;
    }
}
