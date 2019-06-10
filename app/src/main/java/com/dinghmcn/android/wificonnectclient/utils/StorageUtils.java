package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.dinghmcn.android.wificonnectclient.DiskManager;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by zl121325 on 2019/4/14.
 */

public class StorageUtils {

    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static StorageUtils instance;

    private StorageUtils(Context context) {
        this.mContext = context;
    }

    public static StorageUtils getInstance(Context mContext){
        if (null == instance){
            instance = new StorageUtils(mContext);
        }
        return instance;
    }

    //rom
    public String getRomTotalStorage() {
        long totalSize = 0;
        final int[] phoneSpaceVlaue = {1, 2, 4, 8, 16, 32, 64, 128};
        String[] phoneSpacePath = {"/system", "/data", "/cache"};

        try {
            for (String s : phoneSpacePath) {
                if (s.length() > 0
                        && new File(s).exists()) {
                    StatFs stateFs = new StatFs(s);
                    long blockSize = stateFs.getBlockSizeLong();
                    long totalBlocks = stateFs.getBlockCountLong();
                    totalSize += (totalBlocks * blockSize);
                }

            }

            double totalSizeByGB = Double.parseDouble(new DecimalFormat(".00")
                    .format(totalSize / 1024.0 / 1024.0 / 1024.0));
            for (int i1 : phoneSpaceVlaue) {
                if (totalSizeByGB <= i1) {
                    return i1 + " GB";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }

        return "-1";
    }

    public String getRomAvailableStorage()
    {
        return Formatter.formatFileSize(mContext,
                Environment.getDataDirectory().getUsableSpace());
    }

    // sd
    public String getSdTotalStorage() {
        String sdPatch = DiskManager.getSdStoragePath(mContext);
        return DiskManager.isExistDisk(sdPatch)
                ? Formatter.formatFileSize(mContext, new StatFs(sdPatch).getTotalBytes())
                : "-1";
    }

    public String getSdAvailableStorage()
    {
        String sdPatch = DiskManager.getSdStoragePath(mContext);
        return DiskManager.isExistDisk(sdPatch)
                ? Formatter.formatFileSize(mContext, new StatFs(sdPatch).getAvailableBytes())
                : "-1";
    }

}
