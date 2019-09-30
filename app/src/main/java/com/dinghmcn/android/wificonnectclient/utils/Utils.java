
package com.dinghmcn.android.wificonnectclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Utils{
    public static void SetPreferences(Context context, SharedPreferences sp, int name, String flag) {
        String nameStr = context.getResources().getString(name);
        Editor editor = sp.edit();
        editor.putString(nameStr, flag);
        editor.commit();
    }
}
