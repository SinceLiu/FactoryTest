package com.dinghmcn.android.wificonnectclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class BaseTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if(AllTest.begin_auto_test){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	};
}
