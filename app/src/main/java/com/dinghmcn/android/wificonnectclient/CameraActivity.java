package com.dinghmcn.android.wificonnectclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * The type Camera activity.
 *
 * @author dinghmcn
 * @date 2018 /4/20 10:47
 */
public class CameraActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "CameraActivity";

    private String mPictureName = "picture.jpg";

    @Nullable
    private CameraView mCameraView;

    @Nullable
    private Handler mBackgroundHandler;

    @Nullable
    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);
            Objects.requireNonNull(getBackgroundHandler()).post(() -> createFile(data));
        }

    };


    boolean mIsCameraStart = false;
    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        mCameraView = findViewById(R.id.camera);

        if (null != mCameraView) {
            init();
        }
    }

    private void init() {
        assert mCameraView != null;
        assert mCallback != null;
        mCameraView.addCallback(mCallback);
        mCameraView.start();
		mIsCameraStart = true;
        // 获取拍照信息
        String cameraInfo = getIntent().getStringExtra("CameraInfo");
        if (null != cameraInfo && !cameraInfo.isEmpty()) {
            // 保存照片名称
            mPictureName = cameraInfo + ".jpg";
            String[] info = cameraInfo.split("-");
            // 前摄或后摄
            int cameraId = Integer.parseInt(info[1]);
            Log.d(TAG, "cameraId : " + cameraId);
            mCameraView.setFacing(cameraId);
        }
        // 执行拍照
        getBackgroundHandler().postDelayed(() -> {if(mIsCameraStart) mCameraView.takePicture();}, 1000);
    }

    /**
     * On pause.
     */
    @Override
    protected void onPause() {
        // 清理
        assert mCameraView != null;
        mCameraView.stop();
        super.onPause();
        Log.d(TAG, "onPause()");
        finish();
    }

    /**
     * On destroy.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理
        if (mBackgroundHandler != null) {
            mBackgroundHandler.getLooper()
                    .quitSafely();
            mBackgroundHandler = null;
        }

        if (null != mCallback && null != mCameraView) {
            mCameraView.removeCallback(mCallback);
            mCallback = null;
            mCameraView = null;
        }
    }


    @NonNull
    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    /**
     * On back pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * 保存照片文件
     *
     * @param data the data
     */
    private void createFile(@NonNull byte[] data) {
        assert mCameraView != null;
        File file = new File(getExternalCacheDir(), mPictureName);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data, 0, data.length);
            fos.flush();
            Log.w(TAG, "Picture save to " + file);
            // 返回照片地址
            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(file)));
            finish();
        } catch (IOException e) {
            Log.w(TAG, "Cannot write to " + file, e);
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
