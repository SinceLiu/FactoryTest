package com.dinghmcn.android.wificonnectclient.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 录音测试
 *
 * @author zl121325
 * @date 2019 /4/11
 */
public class HeadsetLoopbackUtils {
    private static final String TAG = HeadsetLoopbackUtils.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static HeadsetLoopbackUtils instance = null;

    private int mInputBufferSize;
    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;
    Context mContext;
    public boolean mRecordSuccess = false;

    private HeadsetLoopbackUtils(Context context) {
		mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        init();
    }

    /**
     * Gets instance.
     *
     * @param mContext the m context
     * @return the instance
     */
    public static HeadsetLoopbackUtils getInstance(Context mContext) {
        if (instance == null) {
            instance = new HeadsetLoopbackUtils(mContext);
        }
        return instance;
    }

    /**
     * 开始录音并播出
     */
    public void start() {
    	Log.v("hqb", "hqb__HeadsetLoopbackUtils__start");
//        isRecording = true;
//        new RecordPlayThread().start();
		try {
			getVolumeBefore();
			setAudio();
			record();
			isRecording = true;
			mRecordSuccess = true;
		}catch (Exception e){
			Log.v("hqb", "hqb__HeadsetLoopbackUtils__start__Exception");
			e.printStackTrace();
			isRecording = false;
			mRecordSuccess = false;
		}
    }

    public void play(){
		Log.v("hqb", "hqb__HeadsetLoopbackUtils__play");
		try{
			mMediaPlayer = new MediaPlayer();
			if (isRecording) {
				Log.e(TAG, "音量为：" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
				//add for click fail button crash when recoding by song 20140506 start
				isRecording = false;
				//add for click fail button crash when recoding by song 20140506 end
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
				try {
					replay();
				} catch (Exception e) {
					Log.v("hqb", "hqb__HeadsetLoopbackUtils__play__Exception");
					e.printStackTrace();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

    /**
     * 停止录音并清理
     */
    public void stop() {
		if (isRecording && mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
		//Modify for stop the playing when exit test before compeletion by xiasiping 20140625 start
		if (!isComplete && mMediaPlayer != null) {
			try {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
				deleteRecordResource();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//Modify for stop the playing when exit test before compeletion by xiasiping 20140625 end
		//add for click fail button crash when recoding by song 20140506 end
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		//Add for adding second-mic test by lvhongshan 20140521 start
		mAudioManager.setParameters("second-mic=false");
		//Add for adding second-mic test by lvhongshan 20140521 end
		recoverAudio();



//        mAudioManager.setMode(AudioManager.MODE_NORMAL);
//        //Set normal mic
//        mAudioManager.setParameters("second-mic=false");
//
//        if (null != mAudioTrack) {
//            mAudioTrack.stop();
//            mAudioTrack.release();
//            mAudioTrack = null;
//        }
//
//        if (null != mAudioRecord) {
//        	if(mIsStartRecordSuccess) {
//				mAudioRecord.stop();
//				mIsStartRecordSuccess = false;
//			}
//            mAudioRecord.release();
//            mAudioRecord = null;
//        }
    }

    private void init() {
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        // 初始化录音器
        mInputBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                audioEncoding);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                audioEncoding, mInputBufferSize);

        //初始化播放器
        int mOutputBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                audioEncoding);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                AudioFormat.CHANNEL_OUT_MONO,
                audioEncoding,
                mOutputBufferSize,
                AudioTrack.MODE_STREAM);

        // 音频模式设为正常模式
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        // 调节音量
        float ratio = 0.7f;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);

    }

	boolean mIsStartRecordSuccess = false;
    /**
     * 录音并播放线程
     */
    class RecordPlayThread extends Thread {
        /**
         * Run.
         */
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
					Log.v("hqb", "hqb__play");
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





	String mAudiofilePath;
	File file;
	MediaRecorder mMediaRecorder;
	/**
	 * 让线程停止的标志
	 */
	boolean isRecording = false;
	AudioManager mAudioManager;
	private boolean isExit = false;
	//Modify for stop the playing when exit test before compeletion by xiasiping 20140625 start
	MediaPlayer mMediaPlayer;
	private boolean isComplete = false;
	//Modify for stop the playing when exit test before compeletion by xiasiping 20140625 end
	private int alarmVolume;    //add for recover volume by lxx 20180808;
	private int musicVolume;
	private int callVolume;
	private int dtmfVolume;
	private int notificationVolume;
	private int ringVolume;
	private int systemVolume;
	public void setAudio() {

//        mAudioManager.setMode(AudioManager.MODE_IN_CALL);

		mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, mAudioManager
//				.getStreamMaxVolume(AudioManager.STREAM_DTMF), 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mAudioManager
//				.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager
//				.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager
//				.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
		//Add for adding second-mic test by lvhongshan 20140521 start
		mAudioManager.setParameters("second-mic=true");
		//Add for adding second-mic test by lvhongshan 20140521 end
	}

	void record() throws IllegalStateException, IOException, InterruptedException {
		mMediaRecorder = new MediaRecorder();
		mAudiofilePath = mContext.getCacheDir().getAbsolutePath() + "/test.amr";
		file = new File(mAudiofilePath);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		//mMediaRecorder.setAudioChannels(1);  //设置音频通道数
		mMediaRecorder.setOutputFile(file.getAbsolutePath());
		Log.e(TAG, "_________________prepare()");
		try {
			mMediaRecorder.prepare();
		} catch (IOException e) {
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			return;
		}
		mMediaRecorder.start();
	}

	void replay() throws IllegalArgumentException, IllegalStateException, IOException {
		Log.v("hqb", "hqb__HeadsetLoopbackUtils__replay");

		//add for pass button enalbed by songguangyu 20140505 end
		FileInputStream mFileInputStream = new FileInputStream(file);

		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(mFileInputStream.getFD());
		mMediaPlayer.prepare();
		mMediaPlayer.start();
		mFileInputStream.close();
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			public void onCompletion(MediaPlayer mPlayer) {
				//Modify for stop the playing when exit test before compeletion by xiasiping 20140625 start
				isComplete = true;
				//Modify for stop the playing when exit test before compeletion by xiasiping 20140625 end
				mPlayer.stop();
				mPlayer.release();
				deleteRecordResource();

				// showConfirmDialog();
				if (!isExit) {
					//       showWarningDialog(getString(R.string.record_finish));
				}
				//modify for pass button enalbed by songguangyu 20140505 start
				//btnPass.setEnabled(true);
				//modify for pass button enalbed by songguangyu 20140505 end
			}
		});
	}

	private void deleteRecordResource() {
		if (file == null) {
			return;
		}
		if (file.exists()) {
			file.delete();
		}
	}

	public void getVolumeBefore() {
		alarmVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
		musicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		callVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
//		dtmfVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_DTMF);
//		notificationVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
//		ringVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
//		systemVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
	}

	public void recoverAudio() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolume, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, dtmfVolume, 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationVolume, 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, ringVolume, 0);
//		mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, systemVolume, 0);
	}
}
