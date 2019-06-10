package com.dinghmcn.android.wificonnectclient.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Check permission utils.
 *
 * @author dinghmcn
 * @date 2018 /4/20 11:31
 */
public class CheckPermissionUtils {
  @NonNull
  private static String[] permissions = new String[] {
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.CALL_PHONE,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.RECORD_AUDIO
  };

  private CheckPermissionUtils() {
  }

  /**
   * Check permission string [ ].
   *
   * @param context the context
   * @return the string [ ]
   */
  public static String[] checkPermission(@NonNull Context context) {
    List<String> data = new ArrayList<>();
    for (String permission : permissions) {
      int checkSelfPermission = ContextCompat.checkSelfPermission(context, permission);
      if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
        data.add(permission);
      }
    }
    return data.toArray(new String[0]);
  }
}
