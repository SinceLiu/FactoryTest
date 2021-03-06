package com.dinghmcn.android.wificonnectclient.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * 录音测试
 *
 * @author zl121325
 * @date 2019 /4/11
 */
public class HeadsetLoopbackUtils {
	private static final String TAG = HeadsetLoopbackUtils.class.getSimpleName();

	private AudioManager mAudioManager;
	private int mInputBufferSize;
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;

	/**
	 * 让线程停止的标志
	 */
	public boolean isRecording = false;

	public HeadsetLoopbackUtils(Context context) {
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 开始录音并播出
	 */
	public void start() {
		isRecording = true;
		init();
		new RecordPlayThread().start();
	}

	/**
	 * 停止录音并清理
	 */
	public void stop() {
		if(isRecording) {
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
				if (mIsStartRecordSuccess) {
					mAudioRecord.stop();
					mIsStartRecordSuccess = false;
				}
				mAudioRecord.release();
				mAudioRecord = null;
			}
		}
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

	public boolean mIsStartRecordSuccess = false;
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
				mIsStartRecordSuccess = false;
				t.printStackTrace();
			}
		}
	}
}
