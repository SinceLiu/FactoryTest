package com.example.zl121325.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    static String TAG = "Headset";
    Button recordButton = null;
    Button stopButton = null;
    AudioManager mAudioManager;
    Context mContext;
    static boolean mIsPause = false;
    private int beforeVolume;

    //Modified playing while recording requirement by liuyue 20150427 start
    protected int mInputBufferSize;
    private AudioRecord mAudioRecord;
    private byte[] mInputBufferBytes;
    private LinkedList<byte[]> mInputBytesList;
    //AudioTrack
    private int mOutputBufferSize;
    private AudioTrack mAudioTrack;
    private byte[] mOutputBufferBytes;
    private Thread mRecord;
    private Thread mPlay;
    private boolean flag = true;//让线程停止的标志
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        layoutId = R.layout.transmitter_receiver;
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mIsPause = false;
        getService();
        beforeVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (mAudioManager != null) {
            Log.i(TAG, "onResume:: mAudioManager.getMode() " + mAudioManager.getMode());
//            if (!mAudioManager.isWiredHeadsetOn()) {
//                showWarningDialog(getString(R.string.insert_headset));
//            } else {
                Log.i(TAG, "onResume::start thread ");
                setAudio();
                init();
                startThread();
//            }
        }
    }

    private void startThread() {
        try {
            mRecord = new Thread(new recordSound());
            mPlay = new Thread(new playRecord());
            mRecord.start();
            mPlay.start();
        } catch (Exception e) {
            Log.e(TAG, "start thread catch exception");
            loge(e);
        }
    }

    private void init() {
        recordButton = (Button) findViewById(R.id.transmitter_receiver_start);
        stopButton = (Button) findViewById(R.id.transmitter_receiver_stop);
        stopButton.setVisibility(View.GONE);
        recordButton.setVisibility(View.GONE);
        final TextView mTextView = (TextView) findViewById(R.id.transmitter_receiver_hint);
        mTextView.setText(getString(R.string.transmitter_receiver_recording3));
        //Init AudioRecord
        mInputBufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mInputBufferSize);

        mInputBufferBytes = new byte[mInputBufferSize];
        mInputBytesList = new LinkedList<byte[]>();
        //Init AudioTrack
        mOutputBufferSize = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                mOutputBufferSize,
                AudioTrack.MODE_STREAM);
        mOutputBufferBytes = new byte[mOutputBufferSize];
    }

    class recordSound implements Runnable {
        @Override
        public void run() {
            byte[] bytes_pkg;
            try {
                Log.e(TAG, "recordSound start");
                mAudioRecord.startRecording();
                while (flag) {
//                                 Log.e(TAG,"recordSound read() mIsPause = " + mIsPause);
                    if (mAudioManager != null  && !mIsPause) {
                        mAudioRecord.read(mInputBufferBytes, 0, mInputBufferSize);
                        bytes_pkg = mInputBufferBytes.clone();
                        if (mInputBytesList.size() > 1) {
                            mInputBytesList.removeFirst();
                        }
                        mInputBytesList.add(bytes_pkg);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "recordSound start failed");
                loge(e);
            }
        }
    }

    class playRecord implements Runnable {
        @Override
        public void run() {
            byte[] bytes_pkg = null;
            mAudioTrack.play();
            while (flag) {
                try {
                    if (mAudioManager != null  && !mIsPause) {
//	                             Log.e(TAG,"playrecord write mIsPause = " + mIsPause);
                        mOutputBufferBytes = mInputBytesList.getFirst();
                        bytes_pkg = mOutputBufferBytes.clone();
                        mAudioTrack.write(bytes_pkg, 0, bytes_pkg.length);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "playrecord catch exception");
                    loge(e);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, beforeVolume, 0); //恢复音量
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mIsPause = true;
        Log.i(TAG, "onPause()");
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
        if (alertDialog!=null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public void finish() {
        flag = false;
        super.finish();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        Log.i(TAG, "finsh()");
        //Set normal mic
        mAudioManager.setParameters("second-mic=false");
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    void showWarningDialog(String title) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(mContext).setTitle(title).setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
//                            if (!mAudioManager.isWiredHeadsetOn()) {
//                                alertDialog.dismiss();
//                                alertDialog = null;
//                                showWarningDialog(getString(R.string.insert_headset));
//                            } else {
                                setAudio();
                                init();
                                startThread();
//                            }
                        }
                    }).setCancelable(false).create();
        }
        alertDialog.show();
    }
    public void setAudio() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        float ratio = 0.9f;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
//        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
//                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)), 0);
    }

    void getService() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        //     Utilities.writeCurMessage(this, TAG,"Failed");
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        //     Utilities.writeCurMessage(this, TAG,"Pass");
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
