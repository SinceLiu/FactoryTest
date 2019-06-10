package com.dinghmcn.android.wificonnectclient;

import android.util.SparseBooleanArray;

/**
 * Created by 1 on 2017/2/21.
 */

public class NvItemIds {
    public static final int NV_ESN_I = 0;
    public static final int  NV_UE_IMEI_I = 550;
    public static final int NV_FACTORY_DATA_1_I = 2497;
    public static final int NV_FACTORY_DATA_2_I = 2498;
    public static final int NV_FACTORY_DATA_3_I = 2499;
    public static final int NV_FACTORY_DATA_4_I = 2500;
    public static final int NV_MEID_I = 1943;
    public static final int NV_WIFI_MAC_I = 4678;

    static final SparseBooleanArray  nvItemIds = new SparseBooleanArray();
    static {
        nvItemIds.append(NV_ESN_I, true);
        nvItemIds.append(NV_UE_IMEI_I, true);
        nvItemIds.append(NV_FACTORY_DATA_1_I, true);
        nvItemIds.append(NV_FACTORY_DATA_2_I, true);
        nvItemIds.append(NV_FACTORY_DATA_3_I, true);
        nvItemIds.append(NV_FACTORY_DATA_1_I, true);
        nvItemIds.append(NV_FACTORY_DATA_4_I, true);
        nvItemIds.append(NV_MEID_I, true);
        nvItemIds.append(NV_WIFI_MAC_I, true);
    }

}
