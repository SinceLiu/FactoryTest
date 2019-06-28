package com.dinghmcn.android.wificonnectclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dinghmcn.android.wificonnectclient.model.DataModel;
import com.dinghmcn.android.wificonnectclient.utils.BatteryChargeUtils;
import com.dinghmcn.android.wificonnectclient.utils.BluetoothUtils;
import com.dinghmcn.android.wificonnectclient.utils.CheckPermissionUtils;
import com.dinghmcn.android.wificonnectclient.utils.ConnectManagerUtils;
import com.dinghmcn.android.wificonnectclient.utils.ConnectManagerUtils.EnumCommand;
import com.dinghmcn.android.wificonnectclient.utils.HeadsetLoopbackUtils;
import com.dinghmcn.android.wificonnectclient.utils.SensorManagerUtils;
import com.dinghmcn.android.wificonnectclient.utils.StorageUtils;
import com.dinghmcn.android.wificonnectclient.utils.USBDiskReceiver;
import com.dinghmcn.android.wificonnectclient.utils.USBDiskUtils;
import com.dinghmcn.android.wificonnectclient.utils.VersionUtils;
import com.dinghmcn.android.wificonnectclient.utils.WifiManagerUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 主界面窗口
 *
 * @author dinghmcn
 * @date 2018 /4/20 10:47
 */
public class MainActivity extends Activity {
    final int MSG_TESTACTIVITY = 0x1000;
    /**
     * 命令标识
     */
    public static final int CMD_CODE = 0xd9;
    /**
     * 相机测试结果返回标识
     */
    public static final int REQUEST_CAMERA_CODE = 9;
    /**
     * 全屏显示纯色判断屏幕是否有坏点返回标识
     */
    public static final int REQUEST_SHOWPICTUREFULL = 10;
    /**
     * 是否输出日志
     */
    private static final boolean mPrintLog = true;
    private static final String GET = "get";
    private static boolean isCatchKey = false;
    private static boolean isCatchTouch = false;
    @Nullable
    private static JSONObject mKeyJsonObject;
    @Nullable
    private static JSONArray mTouchJsonArray;
    @Nullable
    private static JSONObject mTouchJsonObject;
    private static JSONArray mTouchMoveJsonObject;
    /**
     * 日志标志.
     */
    protected final String TAG = getClass().getSimpleName();
    /**
     * 是否在测试相机
     */
    public boolean isCameraOpen = false;
    private ScrollView mScrollView;
    private TextView mTextView, mSeq;
    private StringBuilder mConnectMessage;
    private Handler mMainHandler;
    @Nullable
    private ConnectManagerUtils mConnectManager = null;
    @Nullable
    private WifiManagerUtils mWifiManagerUtils = null;
    private BatteryChargeUtils batteryChargeUtils;
    private BluetoothUtils bluetoothUtils;
    private VersionUtils versionUtils;
    private StorageUtils storageUtils;
    private HeadsetLoopbackUtils headsetLoopbackUtils;
    private Gson gson = new Gson();
    private DataModel dataModel;
    private DataModel mShowPictureFullDataModel;
    private DataModel mKeyDataModel;
    private DataModel mRecordDataModel;
    private USBDiskReceiver usbDiskReceiver;

    String dir = "cache";
    private String mPictureName = "picture.jpg";
    private Camera mCamera;
    public static MainActivity Instance;
    private String ip;

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyRunnable.Instance = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        Instance = this;
        mScrollView = findViewById(R.id.message_scrollview);
        mTextView = findViewById(R.id.connect_message);
        mSeq = findViewById(R.id.seq);
        usbDiskReceiver = new USBDiskReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        filter.addAction("android.intent.action.MEDIA_REMOVED");
        registerReceiver(usbDiskReceiver, filter);
        testNext();
        dir = System.currentTimeMillis() + "";
    }

    /**
     * 准备工作，初始化测试项、打开Wifi并连接服务
     */
    private void testNext() {
        initPermission();

        mConnectMessage = new StringBuilder();
        mMainHandler = new MainHandel(this);
//        Message message = new Message();
//        message.what = MSG_TESTACTIVITY;
//        mMainHandler.sendMessageDelayed(message, 1000);
        mWifiManagerUtils = WifiManagerUtils.getInstance(this);
        batteryChargeUtils = BatteryChargeUtils.getInstance(this);
        getBatteryInfo();
        bluetoothUtils = BluetoothUtils.getInstance(this);
        bluetoothUtils.bluetoothOpen();
        headsetLoopbackUtils = HeadsetLoopbackUtils.getInstance(this);
//        outPutMessage("headsetLoopbackUtils.start()");
        outPutMessage(getVersionName(this));
        versionUtils = VersionUtils.getInstance(this);
        storageUtils = StorageUtils.getInstance(this);
        assert mWifiManagerUtils != null;
        // 获取服务器信息
        String ip = loadFromSDFile("socketIP.txt");
        if (null == ip || ip.trim().isEmpty()) {
//            prepareConnectServer("{\"IP\":\"172.17.136.145\",\"Port\":12345,\"SSID\":\"celltel\"," +
//                    "\"PWD\":\"celltel-1502" + "\",\"Station\":1}");
//			prepareConnectServer("{\"IP\":\"192.168.56.1\",\"Port\":12345,\"SSID\":\"readboy.20.234-2.4G\"," +
//					"\"PWD\":\"readboy@123" + "\",\"Station\":1}");
//			prepareConnectServer("{\"IP\":\"192.168.0.110\",\"Port\":12345,\"SSID\":\"readboy.20.234-5G\"," +
//					"\"PWD\":\"readboy@123" + "\",\"Station\":1}");
            prepareConnectServer("{\"IP\":\"192.168.99.112\",\"Port\":12345,\"SSID\":\"readboy-24.198-5G\"," +
                    "\"PWD\":\"1234567890" + "\",\"Station\":1}");
//			prepareConnectServer("{\"IP\":\"192.168.1.254\",\"Port\":12345,\"SSID\":\"readboy-factory-fqc-test1\"," +
//					"\"PWD\":\"readboy@fqc" + "\",\"Station\":1}");
        } else {
            prepareConnectServer("{\"IP\":" + ip + ",\"Port\":12345,\"SSID\":\"tianxi\"" +
                    ",\"PWD\":\"28896800\",\"Station\":1}");
        }

    }

    /**
     * 初始化权限事件.
     */
    protected void initPermission() {
        Log.d(TAG, "check permissions");
        //检查权限
        String[] permissions = CheckPermissionUtils.checkPermission(this);
        if (permissions.length == 0) {
            //权限都申请了
            Log.d(TAG, "permission all");
        } else {
            Log.d(TAG, "request permissions : " + Arrays.toString(permissions));
            //申请权限
            ActivityCompat.requestPermissions(this, permissions, 100);
            try {
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * On activity result.
     *
     * @param requestCode the request code
     * @param resultCode  the result code
     * @param data        the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "result:" + requestCode + "/" + resultCode);
        Log.v("hqb", "hqb__MainActivity__onActivityResult__requestCode == REQUEST_CAMERA_CODE = " + (requestCode == REQUEST_CAMERA_CODE)
                + "__resultCode == RESULT_OK = " + (resultCode == RESULT_OK) + "__data = " + data);
        if (requestCode == REQUEST_CAMERA_CODE) {
            isCameraOpen = false;
            Log.w(TAG, "onActivityResult: " + "11 " + (null == data));
            if (resultCode == RESULT_OK && null != data) {
                Uri pictureUri = data.getData();
                assert pictureUri != null;
                Log.w(TAG, "onActivityResult: " + pictureUri.getPath());
                if (ConnectManagerUtils.mConnected) {
                    assert mConnectManager != null;
                    File file = new File(pictureUri.getPath());
                    dataModel.setCamera("ok");
                    mConnectManager.sendFileToServer(file, gson.toJson(dataModel, DataModel.class));
                    mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                    outPutLog(getString(R.string.send_file, pictureUri.toString()));
                }
            } else {
                outPutLog(R.string.execute_command_error);
                mConnectManager.sendMessageToServerNotJson("execute command error");
                Log.e(TAG, "return result failed.");
            }
        } else if (requestCode == REQUEST_SHOWPICTUREFULL) {
            Log.v("hqb", "hqb__onActivityResult__mShowPictureFullDataModel = " + mShowPictureFullDataModel);
            if (mShowPictureFullDataModel != null) {
                if (resultCode == RESULT_OK) {
                    mShowPictureFullDataModel.setScreen("ok");
                } else {
                    mShowPictureFullDataModel.setScreen("cancel");
                }
                mConnectManager.sendMessageToServer(gson.toJson(mShowPictureFullDataModel, DataModel.class));
            }
        }
    }

    /**
     * 打印消息
     *
     * @param message
     */
    public void outPutMessage(String message) {
        mConnectMessage.append(message).append("\r\n");
        mTextView.setText(mConnectMessage);
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void outPutMessage(int idRes) {
        outPutMessage(getString(idRes));
    }

    /**
     * 打印日志
     *
     * @param message
     */
    public void outPutLog(String message) {
        if (mPrintLog) {
            outPutMessage(message);
        }
    }

    private void outPutLog(int idRes) {
        outPutMessage(getString(idRes));
    }

    private void outPutLogSeq(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (mSeq != null) {
                mSeq.setText(message);
            }
        }
    }

    public void ShowMessage(String msg) {
        this.runOnUiThread(new MyRunnable(msg));
    }

    /**
     * 记录按键信息
     *
     * @param keyCode the key code
     * @param event   the event
     * @return the boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isCatchKey) {
            outPutLog(keyCode + "|" + KeyEvent.keyCodeToString(keyCode));
            if (mKeyJsonObject == null) {
                mKeyJsonObject = new JSONObject();
            }
            try {
                mKeyJsonObject.putOpt(KeyEvent.keyCodeToString(keyCode), keyCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 记录触摸信息
     *
     * @param ev the ev
     * @return the boolean
     */
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        Log.d(TAG, "onTouchEvent");
        if (isCatchTouch) {
            if (mTouchJsonArray == null) {
                mTouchJsonArray = new JSONArray();
            }
            Log.w(TAG, "dispatchTouchEvent: " + ev.getAction());
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "down");
                    mTouchJsonObject = new JSONObject();
                    mTouchMoveJsonObject = new JSONArray();
                    try {
                        mTouchJsonObject.put("DOWN", "(" + ev.getRawX() + "," + ev.getRawY() + ")");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "move");
                    if (mTouchMoveJsonObject != null) {
                        mTouchMoveJsonObject.put("(" + ev.getRawX() + "," + ev.getRawY() + ")");
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "up");
                    try {
                        assert mTouchJsonObject != null;
                        mTouchJsonObject.put("MOVE", mTouchMoveJsonObject);
                        mTouchJsonObject.put("UP", "(" + ev.getRawX() + "," + ev.getRawY() + ")");
                        mTouchJsonArray.put(mTouchJsonObject);
                        Log.d(TAG, mTouchJsonObject.toString() + " | " + mTouchJsonArray.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    mTouchJsonArray.put(mTouchJsonObject);
//                    Log.d(TAG, mTouchJsonObject.toString() + " | " + mTouchJsonArray.toString());
                    mTouchMoveJsonObject = null;
                    mTouchJsonObject = null;
                    break;
                default:
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 获取服务器信息，准备连接服务器
     *
     * @param connectInfo
     */
    private void prepareConnectServer(@Nullable String connectInfo) {
        Log.w(TAG, "prepareConnectServer: " + connectInfo);
        if (connectInfo != null && !connectInfo.isEmpty()) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(connectInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            assert jsonObject != null;
            String serverIp = jsonObject.optString("IP", "");
            int serverPort = jsonObject.optInt("Port", -1);
            String wifiSsid = jsonObject.optString("SSID", "");
            String wifiPassword = jsonObject.optString("PWD", "");

            if (ConnectManagerUtils.isIp(serverIp) && serverPort > 0) {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, serverPort);
                mConnectManager = ConnectManagerUtils.newInstance(mMainHandler, inetSocketAddress);
                assert mConnectManager != null;
                assert mWifiManagerUtils != null;
                mConnectManager.connectServer(mWifiManagerUtils, wifiSsid, wifiPassword);
                outPutMessage(getString(R.string.connect_loading, inetSocketAddress.toString()));
            } else {
                outPutMessage(serverIp + ":" + serverPort + getString(R.string.ip_or_port_illegal));
            }
        } else {
            outPutMessage(connectInfo + " " + getString(R.string.connect_info_error));
        }
    }

    /**
     * 获取电池信息
     */
    private void getBatteryInfo() {
        Log.w(TAG, "getBatteryInfo: " + batteryChargeUtils.getBatteryStatus() + "---"
                + batteryChargeUtils.getQuality() + "---" + batteryChargeUtils.getCurrentChargingCurrent() +
                "---" + batteryChargeUtils.getmLevel() + "---" + batteryChargeUtils.getPlugType() + "---" +
                batteryChargeUtils.getStatus() + "---" + batteryChargeUtils.getTemperature() + "----" +
                batteryChargeUtils.getVoltage() + "---" + batteryChargeUtils.isChargingPass());
    }

    @Override
    public void finish() {
        if (null != headsetLoopbackUtils) {
            headsetLoopbackUtils.stop();
        }
        super.finish();
    }

    /**
     * On destroy.
     */
    @Override
    protected void onDestroy() {
        try {
            ConnectManagerUtils.mConnected = false;
            if (null != mConnectManager) {
                mConnectManager.disconnectServer();
                mConnectManager = null;
            }
            if (null != batteryChargeUtils) {
                batteryChargeUtils.unregisterReceiver();
            }
            if (null != bluetoothUtils) {
                bluetoothUtils.exit();
            }
//            if (null != headsetLoopbackUtils) {
//                headsetLoopbackUtils.stop();
//            }
            if (null != usbDiskReceiver) {
                unregisterReceiver(usbDiskReceiver);
            }
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * On back pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private String loadFromSDFile(String fileName) {
        fileName = "/" + fileName;
        String result = null;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + fileName);
            int length = (int) f.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(f);
            fin.read(buff);
            fin.close();
            result = new String(buff, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "没有找到指定文件", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    /**
     * 关机
     */
    private void shutdownSystem() {
        try {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }
    }

    /**
     * 重启
     */
    private void rebootSystem() {
        Intent intent = new Intent("android.intent.action.REBOOT");
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        sendBroadcast(intent);
    }

    @SuppressLint("HandlerLeak")
    private class MainHandel extends Handler {
        /**
         * The M activity weak reference.
         */
        WeakReference<MainActivity> mActivityWeakReference;

        /**
         * Instantiates a new Main handel.
         *
         * @param activity the activity
         */
        MainHandel(MainActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        private String mLastPartInfo = "";

        /**
         * Handle message.
         *
         * @param msg the msg
         */
        @Override
        public void handleMessage(@NonNull Message msg) {
            final MainActivity mainActivity = mActivityWeakReference.get();

            assert mConnectManager != null;
            assert mainActivity.mConnectManager != null;

            if (msg.what == CMD_CODE) {
                Gson gson = new Gson();
                String data = (String) msg.obj;
                String saveData = (String) msg.obj;
                String tmpData = data.trim();
                if (!tmpData.startsWith("{") && !TextUtils.isEmpty(mLastPartInfo)) {
                    data = mLastPartInfo + data;
                    mLastPartInfo = "";
                }
                if (!TextUtils.isEmpty(data)) {
                    int index = data.indexOf("}");
                    if (index >= 0) data = data.substring(0, index + 1);
                    if (index < data.length() - 1) {
                        mLastPartInfo = data.substring(index + 1);
                    }
                }
                try {
//					dataModel = gson.fromJson((String) msg.obj, DataModel.class);
                    saveDatabjectToPath(getExternalCacheDir() + "/" + dir + "/" + System.currentTimeMillis() + ".txt", saveData);
                    dataModel = gson.fromJson(data, DataModel.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    String path = getExternalCacheDir() + "/" + System.currentTimeMillis() + ".txt";
//					saveDatabjectToPath(path, (String) msg.obj);
                    saveDatabjectToPath(path, data);
                    dataModel = null;
                    mConnectManager.sendMessageToServerNotJson("error");
                    return;
                }
//                dataModel = gson.fromJson((String) msg.obj, DataModel.class);

                // 关机
                if (GET.equals(dataModel.getShutdown())) {
                    shutdownSystem();
                }

                // 消息
                String message = dataModel.getShowMessage();
                if (null != message && !message.isEmpty()) {
                    outPutMessage(message);
                }

                if (dataModel != null) {
                    dataModel.setSn(VersionUtils.getSerialNumber());
                }
                if (GET.equals(dataModel.getSn()) || GET.equals(dataModel.getDisk())
                        || GET.equals(dataModel.getSd()) || GET.equals(dataModel.getVersion())
                        || GET.equals(dataModel.getBattery())) {
                    // 串号
                    dataModel.setSn(VersionUtils.getSerialNumber());

                    // 存储
                    dataModel.setDisk(storageUtils.getRomAvailableStorage() + "，" + storageUtils.getRomTotalStorage());

                    // sd卡
                    dataModel.setSd(storageUtils.getSdAvailableStorage() + "，" + storageUtils.getSdTotalStorage());

                    // 版本号
                    dataModel.setVersion(versionUtils.getSoftwareVersion());

                    // 电池
                    dataModel.setBattery(batteryChargeUtils.getmLevel() + "%，" +
                            "" + (batteryChargeUtils.getTemperature() / 10.0f) + "C，" +
                            "" + (batteryChargeUtils.getVoltage() / 1000.0f) + "V");

                    // otg
//                    USBDiskUtils usbDiskUtils = USBDiskUtils.getInstance(MainActivity.this);
//                    dataModel.setOtg(USBDiskUtils.getInstance(MainActivity.this).getSDAllSize()
//                            + "," + usbDiskUtils.getSDFreeSize());

                    mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                }

                if (GET.equals(dataModel.getOtg())) {
                    // otg
                    USBDiskUtils usbDiskUtils = USBDiskUtils.getInstance(MainActivity.this);
//					dataModel.setOtg(USBDiskUtils.getInstance(MainActivity.this).getSDAllSize()
//							+ "," + usbDiskUtils.getSDFreeSize());
                    usbDiskUtils.startTest();
                    int time = dataModel.getTimeout() * 1000;
                    postDelayed(() -> {
                        String result = usbDiskUtils.mIsTestSuccess ? "ok" : "error";
                        dataModel.setOtg(result);
                        mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                    }, time);
                }

                // 传感器
                if (GET.equals(dataModel.getAccelerometer()) || GET.equals(dataModel.getLight())
                        || GET.equals(dataModel.getProximity()) || GET.equals(dataModel.getMagnetometer())
                        || GET.equals(dataModel.getGyroscope())) {
                    SensorManagerUtils sensorManagerUtils = SensorManagerUtils.getInstance(mainActivity);
                    postDelayed(() -> {
                        assert sensorManagerUtils != null;
                        // 加速度传感器
                        String accelerometer = sensorManagerUtils.getJSONObject()
                                .optString(Sensor.TYPE_ACCELEROMETER + "", "error");
                        dataModel.setAccelerometer(null != accelerometer ? accelerometer : "error");
                        // 光感
                        String light = sensorManagerUtils.getJSONObject()
                                .optString(Sensor.TYPE_LIGHT + "", "error");
                        dataModel.setLight(light);
                        // 距离传感器
                        String proximity = sensorManagerUtils.getJSONObject()
                                .optString(Sensor.TYPE_PROXIMITY + "", "error");
                        dataModel.setProximity(proximity);
                        // 磁感应器
                        String magnetometer = sensorManagerUtils.getJSONObject()
                                .optString(Sensor.TYPE_MAGNETIC_FIELD + "", "error");
                        dataModel.setMagnetometer(magnetometer);
                        // 陀螺仪
                        String gyroscope = sensorManagerUtils.getJSONObject()
                                .optString(Sensor.TYPE_GYROSCOPE + "", "error");
                        dataModel.setGyroscope(gyroscope);

                        Log.d("dhm_sensor", gson.toJson(dataModel, DataModel.class));
                        mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                    }, 1000);
                }

                // 相机
                String cameraInfo = dataModel.getCamera();
                if (null != cameraInfo && cameraInfo.contains("-")) {
                    Log.e("lxx","start");
                    // 保存照片名称
                    mPictureName = cameraInfo + ".jpg";
                    String[] info = cameraInfo.split("-");
                    // 前摄或后摄
                    int cameraId = Integer.parseInt(info[1]);
                    Log.d(TAG, "cameraId : " + cameraId);
                    if (isCameraOpen) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    isCameraOpen = true;
                                    if (mCamera != null) {
                                        mCamera.release();
                                        mCamera = null;
                                    }
                                    mCamera = Camera.open(cameraId);
                                    //设置照片尺寸,需硬件支持该尺寸
                                    Camera.Parameters mParameters = mCamera.getParameters();
                                    if (cameraId == 0) {
                                        mParameters.setPictureSize(1600, 1200);
                                    } else {
                                        List<Camera.Size> list = mParameters.getSupportedPictureSizes();
                                        Log.e("lxx", "front camera:" + list.get(0).width + "x" + list.get(0).height);
                                        mParameters.setPictureSize(list.get(0).width, list.get(0).height);
                                    }
                                    mCamera.setParameters(mParameters);
                                    mCamera.setPreviewTexture(new SurfaceTexture(10));
                                    mCamera.startPreview();
                                    takePictures();
                                } catch (Exception e) {
                                    Log.e(TAG, "take pictures error :" + e.toString());
                                    isCameraOpen = false;
                                }
                            }
                        }, 3000);
                    } else {
                        try {
                            isCameraOpen = true;
                            if (mCamera != null) {
                                mCamera.release();
                                mCamera = null;
                            }
                            mCamera = Camera.open(cameraId);
                            //设置照片尺寸
                            Camera.Parameters mParameters = mCamera.getParameters();
                            if (cameraId == 0) {
                                mParameters.setPictureSize(1600, 1200);
                            } else {
                                List<Camera.Size> list = mParameters.getSupportedPictureSizes();
                                Log.e("lxx", "front camera;" + list.get(0).width + "x" + list.get(0).height);
                                mParameters.setPictureSize(list.get(0).width, list.get(0).height);
                            }
                            mCamera.setParameters(mParameters);
                            mCamera.setPreviewTexture(new SurfaceTexture(10));
                            mCamera.startPreview();
                            takePictures();
                        } catch (Exception e) {
                            Log.e(TAG, "take pictures error :" + e.toString());
                            isCameraOpen = false;
                        }
                    }
                }

                // 蓝牙
                if (GET.equals(dataModel.getBluetooth())) {
                    bluetoothUtils = BluetoothUtils.getInstance(MainActivity.this);
                    postDelayed(() -> {
                        JSONArray jsonArray = new JSONArray();
                        Set<BluetoothDevice> bluetoothDevices = bluetoothUtils.getBluetoothDevices();
                        for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                            jsonArray.put(bluetoothDevice.getName() + "," + bluetoothDevice.getAddress());
                        }
                        dataModel.setBluetooth(jsonArray.toString().replace("\\", "").replace("\\", "").replace("[", "").replace("]", ""));
                        mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                    }, 100);
                }

                // 振动
                if (GET.equals(dataModel.getVibrator())) {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (null != vibrator && vibrator.hasVibrator()) {
                        vibrator.vibrate(dataModel.getTimeout());
                    }
                }

                // 拨号
                if (GET.equals(dataModel.getDial())) {
                    Uri uri = Uri.parse("tel:" + 10010);

                    Intent intent1 = new Intent(Intent.ACTION_CALL, uri);

                    intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    } else {
                        try {
                            startActivity(intent1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

                // wifi
                if (GET.equals(dataModel.getWifi())) {
                    assert mWifiManagerUtils != null;
                    List<ScanResult> rssis = mWifiManagerUtils.getWifis();
                    JSONArray jsonArray = new JSONArray();
                    for (ScanResult wifi : rssis) {
                        jsonArray.put(wifi.SSID + "," + wifi.level);
                    }
                    dataModel.setWifi(jsonArray.toString().replace("\"", "")
                            .replace("[", "").replace("]", ""));
                    mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                }

                // 录音
                if (GET.equals(dataModel.getRecord())) {
                    int time = dataModel.getTimeout() * 1000;
                    mRecordDataModel = dataModel;
//                    headsetLoopbackUtils.start();
                    headsetLoopbackUtils.start();
                    postDelayed(() -> {
                        if (mRecordDataModel != null) {
                            mRecordDataModel.setRecord(headsetLoopbackUtils.mIsStartRecordSuccess ? "ok" : "error");
                            mConnectManager.sendMessageToServer(gson.toJson(mRecordDataModel, DataModel.class));
                            headsetLoopbackUtils.stop();
                        }
                    }, time);
                }

                // 按键
                if (GET.equals(dataModel.getKey())) {
                    mKeyDataModel = dataModel;
                    int time = dataModel.getTimeout() * 1000;
                    isCatchKey = true;
                    postDelayed(() -> {
                        Log.v("hqb", "hqb__key__mKeyJsonObject = " + mKeyJsonObject);
                        if (mKeyJsonObject != null) {
                            mainActivity.mConnectManager.sendMessageToServer(mKeyJsonObject.toString());
                        } else {
                            if (mKeyDataModel != null) {
                                mKeyDataModel.setKey("ok");
                                mConnectManager.sendMessageToServer(gson.toJson(mKeyDataModel, DataModel.class));
                            }
                        }
                        mKeyJsonObject = null;
                        isCatchKey = false;
                    }, time);
                }

                // 触摸
                if (GET.equals(dataModel.getTouch())) {
                    isCatchTouch = true;
                    dataModel.setTouch("ok");
                    mainActivity.mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                    int time = dataModel.getTimeout() * 1000;
                    postDelayed(() -> {
                        if (mTouchJsonArray != null) {
                            mainActivity.mConnectManager.sendMessageToServer(mTouchJsonArray.toString());
                        }
                        mTouchJsonArray = null;
                        isCatchTouch = false;
                    }, time);
                }

                // 屏幕
                if (dataModel.getScreen() != null) {
                    String imageName = dataModel.getScreen();
                    mShowPictureFullDataModel = dataModel;
                    int resId = mainActivity.getResources()
                            .getIdentifier(imageName, "drawable",
                                    mainActivity.getPackageName());
                    if (resId > 0) {
                        Log.v("hqb", "hqb__ShowPictureFullActivity__dataModel.getTimeout() = " + dataModel.getTimeout() + "__dataModel.getTimeout() * 1000 = " + (dataModel.getTimeout() * 1000));
                        final Intent intent30 = new Intent(mainActivity,
                                ShowPictureFullActivity.class).putExtra("res_id", resId).putExtra("timeout", dataModel.getTimeout() * 1000);
//                        mainActivity.startActivity(intent30);
                        mainActivity.startActivityForResult(intent30, REQUEST_SHOWPICTUREFULL);
                        mainActivity.outPutLog(mainActivity.getString(R.string.show_file, imageName));
                        Log.d(mainActivity.TAG, mainActivity.getString(R.string.show_file, imageName));
                    } else {
                        mainActivity.outPutLog(mainActivity.getString(R.string.file_not_exist, imageName));
                        Log.d(mainActivity.TAG, mainActivity.getString(R.string.file_not_exist, imageName));
                    }
                }
            } else if (msg.what == MSG_TESTACTIVITY) {
//				mShowPictureFullDataModel = new DataModel();
//				String imageName = "red";
//				int resId = mainActivity.getResources()
//						.getIdentifier(imageName, "drawable",
//								mainActivity.getPackageName());
//				if (resId > 0) {
//					final Intent intent30 = new Intent(mainActivity,
//							ShowPictureFullActivity.class).putExtra("res_id", resId);
////                        mainActivity.startActivity(intent30);
//					mainActivity.startActivityForResult(intent30, REQUEST_SHOWPICTUREFULL);
//					mainActivity.outPutLog(mainActivity.getString(R.string.show_file, imageName));
//					Log.d(mainActivity.TAG, mainActivity.getString(R.string.show_file, imageName));
//				} else {
//					mainActivity.outPutLog(mainActivity.getString(R.string.file_not_exist, imageName));
//					Log.d(mainActivity.TAG, mainActivity.getString(R.string.file_not_exist, imageName));
//				}

//				Message message = new Message();
//				message.what = MSG_TESTACTIVITY;
//				mMainHandler.sendMessageDelayed(message, 5000);
//				headsetLoopbackUtils.start();
//				postDelayed(() -> {
//					String result = headsetLoopbackUtils.mIsStartRecordSuccess ? "ok" : "error";
//					headsetLoopbackUtils.stop();
//					Log.v("hqb", "hqb__record result = " + result);
//				}, 3000);

//				USBDiskUtils usbDiskUtils = USBDiskUtils.getInstance(MainActivity.this);
////				String otg = USBDiskUtils.getInstance(MainActivity.this).getSDAllSize()
////						+ "," + usbDiskUtils.getSDFreeSize() + usbDiskUtils.fileInfo();
//				usbDiskUtils.startTest();
//				postDelayed(() -> {
//					usbDiskUtils.stopTest();
//					String result = usbDiskUtils.mIsTestSuccess ? "ok" : "error";
//					Log.v("hqb", "hqb__usb result = " + result);
//					outPutLog("usb test " + result);
//				}, 3000);
//				Message message = new Message();
//				message.what = MSG_TESTACTIVITY;
//				mMainHandler.sendMessageDelayed(message, 4000);
            } else {
                switch (EnumCommand.values()[msg.what]) {
                    // 连接服务器返回状态
                    case CONNECT:
                        int connect = msg.arg1;
                        switch (connect) {
                            case ConnectManagerUtils.CONNECT_FAILED:
                                mainActivity.outPutMessage(R.string.connect_failed);
                                break;
                            case ConnectManagerUtils.CONNECT_CLOSED:
                                mainActivity.outPutMessage(R.string.connect_closed);
                                break;
                            case ConnectManagerUtils.CONNECT_SUCCESS:
                                mainActivity.outPutMessage(R.string.connect_success);
                                break;
                            default:
                        }
                        break;
                    // 接收命令状态
                    case COMMAND:
                        int command = msg.arg1;
                        switch (command) {
                            case ConnectManagerUtils.COMMAND_ERROR:
                                mainActivity.outPutLog(R.string.command_error);
                                break;
                            case ConnectManagerUtils.COMMAND_RECEIVE:
                                mainActivity.outPutLog(R.string.wait_command);
                                break;
                            case ConnectManagerUtils.COMMAND_SEND:
                                mainActivity.outPutLog(msg.obj.toString());
                                break;
                            default:
                        }
                        break;
                    case SEQ:
                        mainActivity.outPutLogSeq(msg.obj.toString());
                        mConnectManager.sendMessageToServerNotJson("seq=ok");
                        break;
                    case Alive:
                        mainActivity.outPutMessage(msg.obj.toString());
                        mConnectManager.sendMessageToServerNotJson("I am alive!!");
                        break;
                    default:
                        mainActivity.outPutLog(Integer.toString(msg.what));
                }
            }
        }
    }


    public static final String NOMEDIA = ".nomedia";

    /**
     * 创建文件夹（带nomedia）
     *
     * @param dirPath
     */
    public static void mkdirs(String dirPath) {
        try {
            File file = new File(dirPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String filePath = dirPath.endsWith(File.separator) ? (dirPath + NOMEDIA) : (dirPath + File.separator + NOMEDIA);
            File f = new File(filePath);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字符串保存到文件
     *
     * @param path
     */
    public static boolean saveDatabjectToPath(String path, String data) {
        Log.v("hqb", "hqb__path = " + path);
//		outPutMessage(data);
        if (TextUtils.isEmpty(path) || data == null) {
            return false;
        }
        File fp = new File(path);
        if (!fp.getParentFile().exists()) {
            fp.getParentFile().mkdirs();
        }
        mkdirs(fp.getParent());
        try {
            FileOutputStream out = new FileOutputStream(path);
            // 将json数据加密之后存入缓存
            byte[] source = null;
            source = data.getBytes("utf-8");
            if (source != null) {
                out.write(source, 0, source.length);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            String pkName = context.getPackageName();
            versionName = "版本：" + context.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
// 			int versionCode = this.getPackageManager()
// 					.getPackageInfo(pkName, 0).versionCode;
// 			return pkName + "   " + versionName + "  " + versionCode;
        } catch (Exception e) {
        }
        return versionName;
    }


    /**
     * 拍照、保存图片、上传信息
     */
    private void takePictures() {
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (data != null) {
                            File file = createFile(data);
                            if (file != null && ConnectManagerUtils.mConnected) {
                                assert mConnectManager != null;
                                dataModel.setCamera("ok");
                                mConnectManager.sendFileToServer(file, gson.toJson(dataModel, DataModel.class));
                                mConnectManager.sendMessageToServer(gson.toJson(dataModel, DataModel.class));
                                outPutLog(getString(R.string.send_file, Uri.fromFile(file).toString()));
                                Log.e("lxx","finish");
                                isCameraOpen = false;
                            }
                        } else {
                            outPutLog(R.string.execute_command_error);
                            mConnectManager.sendMessageToServerNotJson("take the picture error");
                            isCameraOpen = false;
                        }
                    }
                });
            }
        }, 500);

    }

    /**
     * 保存照片文件
     *
     * @param data the data
     */
    private File createFile(@NonNull byte[] data) {
        assert mCamera != null;
        File file = new File(getExternalCacheDir(), mPictureName);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data, 0, data.length);
            fos.flush();
            Log.w(TAG, "Picture save to " + file);
            // 返回照片地址
            return file;
        } catch (IOException e) {
            Log.w(TAG, "Cannot write to " + file, e);
            return null;
        }
    }
}
