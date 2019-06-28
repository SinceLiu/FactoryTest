/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dinghmcn.android.wificonnectclient;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;

/**
 * Dialog that comes up in response to various music-related VIEW intents.
 */
public class UDiskTest extends Activity {
	private final static String TAG = "UDiskTest";

	public static final String USB_PATH = "/storage/12B9-40E7";

	private Uri mUri;
	private long mMediaId = -1;
	private static final int OPEN_IN_MUSIC = 1;
	private String strFromFile = "";
	private File mfile = null;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

//		Intent intent = getIntent();
		// if (intent == null) {
		// finish();
		// return;
		// }
		//
		// mUri = intent.getData();
		// if (mUri == null) {
		// finish();
		// return;
		// }

//		String scheme = mUri.getScheme();
//		Log.v(TAG, " scheme: " + scheme);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		String path = DiskManager.getUsbStoragePath(this);
		if(path == null){
			Toast.makeText(this, "没有外接U盘", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		ArrayList<String> allPathList = new ArrayList<String>();
		allPathList.add(path);
//		ArrayList<String> allPathList = DiskManager.getAllStoragePath(this);
		int size = allPathList.size();
		Log.i(TAG, "-----------------------size = "+size);

//		String path = DiskManager.getUsbStoragePath(this);
//		if (path == null) {
//			path = USB_PATH;
//		}
//		String filename = "/storage/usbotg/test.txt";
//		filename = path + "/test.txt";
//		// setContentView(R.layout.audiopreview);
//		if ("file".equals(scheme)) {
//			filename = mUri.toString();
//			filename = filename.substring("file://".length(), filename.length());
//			Log.v(TAG, " File Name: " + filename);
//		}

		String pathString;
		for (int i = 0; i < size; i++) {
			pathString = allPathList.get(i);
			pathString = pathString + "/test.txt";
			mfile = new File(pathString);
			if (mfile.exists()) {
				try {
					mfile.delete();
				} catch (Exception e) {
					finish();
					e.printStackTrace();
				}
				addFile();
				if (!doTest(i == (size - 1))) {
					break;
				}
			} else {
				addFile();
				if (!doTest(i == (size - 1))) {
					break;
				}
			}
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public boolean doTest(boolean end) {
		strFromFile = openFile(mfile);
		Log.v(TAG, " strFromFile: " + strFromFile);
		if (!"".equals(strFromFile)) {
			if (end) {
				Intent data = new Intent();
				data.putExtra("readfile", strFromFile);
				setResult(RESULT_OK, data);
				Log.v(TAG, " onDestroy : " + strFromFile);
				finish();
			}
		} else {
			Log.v(TAG, " strFromFile: is null");
			strFromFile = "read or write fail,please delete the test.txt in udisk and test again";
			Intent data = new Intent();
			data.putExtra("readfile", strFromFile);
			setResult(RESULT_CANCELED, data);
			Log.v(TAG, " onDestroy : " + strFromFile);
			finish();
			return false;
		}
		return true;
	}

	
	
	public void addFile() {
		try {
			mfile.createNewFile();
			writeFIle();
		} catch (IOException ioe) {
//			System.out.println("addFile IOException");
//			Toast.makeText(this, mfile.getAbsolutePath(), Toast.LENGTH_LONG).show();
//			ioe.printStackTrace();
			finish();
		}

	}

	public void writeFIle() {
		FileWriter fw;
		BufferedWriter bw;
		try {
			fw = new FileWriter(mfile);
			bw = new BufferedWriter(fw);
			bw.write("Udisk test successfully.");
			bw.close();
		} catch (IOException ioe) {
			finish();
		}

	}

	public String openFile(File file) {
		FileReader fr;
		BufferedReader br;
		String str = "";
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			int line = 1;
			if (br.ready()) {
				str = br.readLine();
				// line ++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			finish();
			e.printStackTrace();
		} catch (IOException Ie) {
			finish();
			Ie.printStackTrace();
		}
		return str;
	}
}
