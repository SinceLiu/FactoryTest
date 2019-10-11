package com.dinghmcn.android.wificonnectclient;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * The type Camera activity.
 *
 * @author dinghmcn
 * @date 2018 /4/20 10:47
 * @deprecated 拍照效率好
 */
public class CameraActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "CameraActivity";

    private String mPictureName = "picture.jpg";

    final int MSG_TAKEPICTURE = 0x1000;

    Handler mHandler;

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
            if (data != null) {
                Log.e("CHEN", "onPictureTaken " + data.length);
                Objects.requireNonNull(getBackgroundHandler()).post(() -> createFile(data));
            } else {
                Log.e("CHEN", "hqb__CameraActivityfinish__onPictureTaken__data is null");
                setResult(RESULT_CANCELED);
                finish();
            }
        }

    };

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
//        mCameraView.setAutoFocus(true);
        mCameraView.setAutoFocus(true);


        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_TAKEPICTURE:
                        if (mCameraView != null) {
                            mCameraView.takePicture();
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        if (null != mCameraView) {
            init();
        }
    }

    private void init() {
        assert mCameraView != null;
        assert mCallback != null;

        mCameraView.addCallback(mCallback);

        mCameraView.start();
        // 获取拍照信息
        String cameraInfo = getIntent().getStringExtra("CameraInfo");
        if (null != cameraInfo && !cameraInfo.isEmpty()) {
            // 保存照片名称
            mPictureName = cameraInfo + ".jpg";
            String[] info = cameraInfo.split("-");
            // 前摄或后摄
            int cameraId = Integer.parseInt(info[1]);
            String[] isfocus = info[0].split("_");
            String isturefocous = isfocus[1];


            Log.e("CHEN", "cameraId : " + cameraId);
            mCameraView.setFacing(cameraId);
            if (cameraId == 0) {
                if ("BW".equals(isturefocous)) {
                    mCameraView.setAutoFocus(false);
                } else if ("BD".equals(isturefocous)) {
                    mCameraView.setAutoFocus(false);
                } else {
                    mCameraView.setFocusable(true);
                    mCameraView.setAutoFocus(true);
                }
//                mCameraView.setAutoFocus(true);
            }
            if (cameraId == 1) {
                mCameraView.setFocusable(true);
                mCameraView.setAutoFocus(true);
            }


        }
        // 执行拍照
//        getBackgroundHandler().postDelayed(() -> mCameraView.takePicture(), 1000);
        Message message = new Message();
        message.what = MSG_TAKEPICTURE;
        mHandler.sendMessageDelayed(message, 1000);
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
        Log.v("hqb", "hqb__CameraActivityfinish__onPause");
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
        Log.v("hqb", "hqb__CameraActivityfinish__onBackPressed");
        finish();
    }

    /**
     * 保存照片文件
     *
     * @param data the data
     */
    private void createFile(@NonNull byte[] data) {
        assert mCameraView != null;
        File originalFile = new File(getExternalCacheDir(), mPictureName);
        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(originalFile)) {
            fos.write(data, 0, data.length);
            fos.flush();
            Log.e("CHEN", "Picture save to " + originalFile);
//            compressBitmap(getExternalCacheDir().getAbsolutePath(),originalFile);
//            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(originalFile)));
//            Log.v("hqb", "hqb__CameraActivityfinish__createFile__success");
//            finish();
            Luban.with(this)
                    .load(originalFile)
                    .ignoreBy(100)

                    .setTargetDir(getExternalCacheDir().getAbsolutePath())
                    .setRenameListener(new OnRenameListener() {
                        @Override
                        public String rename(String filePath) {
                            return mPictureName;
                        }
                    })
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        @Override
                        public void onSuccess(File file) {
                            // TODO 压缩成功后调用，返回压缩后的图片文件
                            // 返回照片地址，上传压缩的图片
                            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(file)));
                            Log.v("hqb", "hqb__CameraActivityfinish__createFile__success");
                            finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO 当压缩过程出现问题时调用
                            // 返回照片地址,压缩失败，上传原图
                            setResult(RESULT_OK, new Intent().setData(Uri.fromFile(originalFile)));
                            Log.v("hqb", "hqb__CameraActivityfinish__createFile__success");
                            finish();
                        }
                    }).launch();

        } catch (IOException e) {
            Log.e("CHEN", "Cannot write to " + originalFile, e);
            Log.e("CHEN", "hqb__CameraActivityfinish__createFile__failed");
            setResult(RESULT_CANCELED);
            finish();
        }
    }


}
