package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 *
 * @author zl121325
 * @date 2019/4/11
 */

public class HeadsetLoopbackUtils {
    private static final String TAG = HeadsetLoopbackUtils.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static HeadsetLoopbackUtils instance = null;

    private AudioManager mAudioManager;
    private int mInputBufferSize;
    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;

    /**
     * 让线程停止的标志
     */
    private boolean isRecording = false;

    private HeadsetLoopbackUtils(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        init();
    }

    public static HeadsetLoopbackUtils getInstance(Context mContext) {
        if (instance == null) {
            instance = new HeadsetLoopbackUtils(mContext);
        }
        return instance;
    }

    public void start() {
        isRecording = true;
        new RecordPlayThread().start();
    }

    public void stop() {
        isRecording = false;

        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        //Set normal mic
        mAudioManager.setParameters("second-mic=false");

        if (null != mAudioTrack) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }

        if (null != mAudioRecord) {
        	if(mIsStartRecordSuccess) {
				mAudioRecord.stop();
				mIsStartRecordSuccess = false;
			}
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    private void init() {
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        //Init AudioRecord
        mInputBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                audioEncoding);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                audioEncoding, mInputBufferSize);

        //Init AudioTrack
        int mOutputBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                audioEncoding);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                AudioFormat.CHANNEL_OUT_MONO,
                audioEncoding,
                mOutputBufferSize,
                AudioTrack.MODE_STREAM);


        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        float ratio = 0.7f;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);

    }

    boolean mIsStartRecordSuccess = false;
    class RecordPlayThread extends Thread {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[mInputBufferSize];
                //开始录制
                mAudioRecord.startRecording();
				mIsStartRecordSuccess = true;
                //开始播放
                mAudioTrack.play();

                while (isRecording) {
                    //从MIC保存数据到缓冲区
                    int bufferReadResult = mAudioRecord.read(buffer, 0,
                            mInputBufferSize);

                    byte[] tmpBuf = new byte[bufferReadResult];
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                    //写入数据即播放
                    mAudioTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                mAudioTrack.stop();
                mAudioRecord.stop();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
