package com.dinghmcn.android.wificonnectclient.utils;

import android.content.Context;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The type Usb disk utils.
 */
public class USBDiskUtils {
    private static final String MOUNTS_FILE = "/proc/mounts";

    private static String path = "/mnt/usbhost1";
    /**
     * The constant instance.
     */
    public static USBDiskUtils instance;
    private Context mContext;

    /**
     * Instantiates a new Usb disk utils.
     *
     * @param mContext the m context
     */
    public USBDiskUtils(Context mContext) {
        this.mContext = mContext;
        isMounted();
    }

    /**
     * Get instance usb disk utils.
     *
     * @param mContext the m context
     * @return the usb disk utils
     */
    public static USBDiskUtils getInstance(Context mContext){
        if (instance==null)
            instance=new USBDiskUtils(mContext);
        return instance;
    }

    /**
     * Is mounted boolean.
     *
     * @return the boolean
     */
    public  boolean isMounted() {

        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));

            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }

        return blnRet;
    }

    /**
     * Get sd free size long.
     *
     * @return the long
     */
    public long getSDFreeSize(){
        if (!isMounted()) {
            return 0;
        }
        path= DiskManager.getUsbStoragePath(mContext);
        StatFs sf = new StatFs(path);
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        //return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * Gets sd all size.
     *
     * @return the sd all size
     */
    public long getSDAllSize() {
        if (!isMounted()) {
            return 0;
        }
        //取得SD卡文件路径
        path= DiskManager.getUsbStoragePath(mContext);
        StatFs sf = new StatFs(path);
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        //return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize) / 1024 / 1024; //单位MB

    }
}