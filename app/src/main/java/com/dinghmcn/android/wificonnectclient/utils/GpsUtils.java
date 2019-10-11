package com.dinghmcn.android.wificonnectclient.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.dinghmcn.android.wificonnectclient.MainActivity;
import com.dinghmcn.android.wificonnectclient.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by zl121325 on 2019/4/14.
 */

public class GpsUtils {
    Context mContext;
    private LocationManager m_mgr;
    private Location m_location;
    int MAX_SATELITE_COUNT = 50;
    private GpsStatus m_gpsStatus;
    GpsInfo[] mGpsInfo = new GpsInfo[MAX_SATELITE_COUNT];
    String strGpsFilePaht = "";
    boolean isLocationOpened = false;
    private boolean mStartLogGpsData = false;
    private int mSecond = 0;
    st_TimerTask timerTask;
    Timer st_timer;
    private int mStateliteCount;
    private int mLocatetime = 0;
    AlertDialog gpsDialog;
    private Handler hGpsHand = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public GpsUtils(Context mContext) {
        this.mContext = mContext;
        initGpsService();
    }

    private void initGpsService() {
        strGpsFilePaht = "/data/GpsData.txt";
        boolean bdeleteFile = deleteGpsDataFile(strGpsFilePaht);
        Log.d("Gps", "The bdeleteFile = " + bdeleteFile);
        //}
        Log.w("initGpsService: ", "111");
        m_mgr = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (m_mgr == null) {
            Log.i("lvhongshan_gps", "LocationManager is null");

        } else {
            Log.i("lvhongshan_gps", "LocationManager is not null");
        }
        openGPS();

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        m_mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
                locationListener);
        timerTask = new st_TimerTask();
        st_timer = new Timer();
        st_timer.schedule(timerTask, 100, 1000);
        boolean bsucess = m_mgr.addGpsStatusListener(statusListener);

        Log.e("Gps", "Add the statusListner is " + bsucess);

        if (!bsucess) {
            Toast.makeText(mContext, R.string.gps_open_error, Toast.LENGTH_SHORT).show();
        }
        bsucess = m_mgr.addNmeaListener(mNmeaListener);
        Log.e("Gps", "Add the statusListner is " + bsucess);
        if (!bsucess) {
            Toast.makeText(mContext, R.string.gps_open_error, Toast.LENGTH_SHORT).show();
        }

        m_location = m_mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (m_location == null) {
            Log.i("lvhongshan_gps", "Location is null");
        } else {
            Log.i("lvhongshan_gps", "Location is not null");
        }
        updateWithNewLocation(m_location);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
            Log.d("Gps", "lvhongshan the onLocationChanged is exced");
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
            Log.d("Gps", "lvhongshan the onProviderDisabled is exced");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Gps", "lvhongshan the onProviderEnabled is exced");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Gps", "lvhongshan the onStatusChanged is exced");
        }
    };

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            Log.w("updateWithNewLocation: ", location.toString());
        }
    }

    private boolean deleteGpsDataFile(String strGpsFilePaht) {
        boolean bDelete = true;
        File file = new File(strGpsFilePaht);
        if (file.exists()) {
            bDelete = file.delete();
        } else {
            return false;
        }
        return bDelete;
    }

    private final GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {

        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            //if(isSDcardexist()){
            if (getLogGpsData()) {
                updateNmeaStatus(nmea);
                writeNeamDatainfile(nmea);
            }
            //}else{
            //Log.d("Gps","The sdcard is not exist");
            //}
        }
    };

    private synchronized boolean getLogGpsData() {
        return mStartLogGpsData;
    }

    private void updateNmeaStatus(String strNmea) {
        Log.d("GPS", "GPS:data = " + strNmea);
    }

    private boolean writeNeamDatainfile(String strNmea) {
        boolean bresult = true;
        try {
            File gpsFile = new File(strGpsFilePaht);
            FileWriter fileWriter = new FileWriter(gpsFile, true);

            boolean bcanWrite = gpsFile.canWrite();
            if (bcanWrite) {
                Log.i("lvhongshan_gps", "writeNeamDatainfile is success");
                fileWriter.append(strNmea + "\r\n");
                fileWriter.flush();
            }
            fileWriter.close();

        } catch (IOException e) {
            bresult = false;
            Log.e("LOG_TAG", e.getLocalizedMessage());
        } finally {
        }

        return bresult;
    }

    private void openGPS() {
        if (!m_mgr.isProviderEnabled(LocationManager.GPS_PROVIDER) && !m_mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            isLocationOpened = false;
            Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(), LocationManager.GPS_PROVIDER, true);
        } else {
            isLocationOpened = true;
        }
    }

    private class GpsInfo {
        int prn;
        int iID;
        private float fAzimuth;
        private float fElevation;
        private float snr;

        public GpsInfo() {
            prn = 0;
            iID = 0;
            fAzimuth = 0;
            fElevation = 0;
            snr = 0;
        }
    }

    class st_TimerTask extends TimerTask {

        @Override
        public void run() {
            mSecond++;
            hGpsHand.sendEmptyMessage(0);
        }

    }

    private GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.w("onGpsStatusChanged: ", "111");
                return;
            }
            m_gpsStatus = m_mgr.getGpsStatus(null);
            Log.w("onGpsStatusChanged: ", event + "----");
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    int nfixTime = m_gpsStatus.getTimeToFirstFix();
                    st_timer.cancel();
                    setLogGpsData(true);
                    Log.d("Gps", "GpsStatus.GPS_EVENT_FIRST_FIX the fix Time is " + nfixTime);
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.d("Gps", "GpsStatus.GPS_EVENT_SATELLITE_STATUS");
                    Iterable<GpsSatellite> allSatellites;
                    allSatellites = m_gpsStatus.getSatellites();
                    Iterator it = allSatellites.iterator();
                    int iCount = 0;
                    while (it.hasNext()) {
                        GpsSatellite satelite = (GpsSatellite) it.next();

                        mGpsInfo[iCount].prn = satelite.getPrn();
                        mGpsInfo[iCount].fAzimuth = satelite.getAzimuth();
                        mGpsInfo[iCount].fElevation = satelite.getElevation();
                        mGpsInfo[iCount].snr = satelite.getSnr();
                        mGpsInfo[iCount].iID = iCount;

                        iCount++;

                        Log.d("Gps", "mGpsInfo[iCount].prn is " + mGpsInfo[iCount].prn);
                        Log.d("Gps", "mGpsInfo[iCount].fAzimuth is " + mGpsInfo[iCount].fAzimuth);
                        Log.d("Gps", "mGpsInfo[iCount].fElevation" + mGpsInfo[iCount].fElevation);
                        Log.d("Gps", "mGpsInfo[iCount].snr" + mGpsInfo[iCount].snr);
                        Log.d("Gps", "mGpsInfo[iCount].iID" + mGpsInfo[iCount].iID);

                    }
                    mStateliteCount = iCount;
                    Log.d("Gps", "the mStateliteCount is" + mStateliteCount);
                    setStateliteinfo(iCount);
                    break;
                case GpsStatus.GPS_EVENT_STARTED:
                    // Event sent when the GPS system has started.
                    Log.d("Gps", "GpsStatus.GPS_EVENT_STARTED");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    // Event sent when the GPS system has stopped.
                    Log.d("Gps", "GpsStatus.GPS_EVENT_STOPPED");
                    break;
                default:
                    break;
            }
        }
    };

    private void setStateliteinfo(int validsatelite) {
        int ncount = 6;

        int bdIndex = 6;
        int gpsIndex = 0;
        for (int i = 0; i < validsatelite; i++) {
            if (mGpsInfo[i].prn < 32 && gpsIndex < ncount) {
                gpsIndex++;
            } else if (mGpsInfo[i].prn >= 32 && bdIndex < ncount * 2) {
                bdIndex++;
            }
        }
        //Modify for passButton clickable when locate success by songguangyu 20140220 start
        if (validsatelite >= 4) {
            mLocatetime++;
        }
        if (validsatelite >= 4 && mLocatetime <= 1) {
        }
        //Modify for passButton clickable when locate success by songguangyu 20140220 end
    }

    public void exit() {
        if (!isLocationOpened) {
            Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(), LocationManager.GPS_PROVIDER, false);
        }
        m_mgr.removeUpdates(locationListener);
        m_mgr.removeGpsStatusListener(statusListener);
        m_mgr.removeNmeaListener(mNmeaListener);
        st_timer.cancel();
    }

    public String getmStateliteCount() {
        return "{Gps:" + mStateliteCount + "}";
    }

    private synchronized void setLogGpsData(boolean start) {
        mStartLogGpsData = start;
    }

}
