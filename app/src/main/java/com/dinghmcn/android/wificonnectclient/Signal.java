
package com.dinghmcn.android.wificonnectclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;



import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Signal extends BaseTestActivity implements OnClickListener {
    private Button mBtOk;
    private Button mBtFailed;
    private Button mBtEmergencyCall;
    private Button mBtServiceCall;

    SharedPreferences mSp;
    private boolean unknownSim = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar.LayoutParams lp =new  ActionBar.LayoutParams(
        	android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        	android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        	Gravity.CENTER);

//		View mView =  LayoutInflater.from(this).inflate(R.layout.title, new LinearLayout(this), false);
//		TextView mTextView = (TextView) mView.findViewById(R.id.action_bar_title);
//		getActionBar().setCustomView(mView, lp);
	
//		mTextView.setText(getTitle());

//		getActionBar().setDisplayShowHomeEnabled(false);
//		getActionBar().setDisplayShowTitleEnabled(false);
//		getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//		getActionBar().setDisplayShowCustomEnabled(true);
//		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        setContentView(R.layout.signal);

        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        mBtOk = (Button) findViewById(R.id.signal_bt_ok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.signal_bt_failed);
        mBtFailed.setOnClickListener(this);
        mBtEmergencyCall = (Button) findViewById(R.id.emergency_call);
        mBtEmergencyCall.setOnClickListener(this);
        mBtServiceCall = (Button) findViewById(R.id.service_call);
        mBtServiceCall.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String simOper = SystemProperties.get("gsm.sim.operator.numeric", "46099");
        if("46000".equals(simOper) || "46002".equals(simOper) || "46007".equals(simOper) || "46008".equals(simOper)) {
            unknownSim = false;
        } else if("46001".equals(simOper) || "46006".equals(simOper) || "46009".equals(simOper)) {
            unknownSim = false;
        } else if("46003".equals(simOper) || "46005".equals(simOper) || "46011".equals(simOper)) {
            unknownSim = false;
        } else {
            unknownSim = true;
        }
        if(unknownSim) {
            mBtServiceCall.setEnabled(false);
        } else {
            mBtServiceCall.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(SystemProperties.getBoolean("ro.cenon_wearable", false)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.FMRadio_notice);
        builder.setMessage(R.string.HeadSet_hook_message);
        builder.setPositiveButton(R.string.Success,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Utils.SetPreferences(Signal.this, mSp, R.string.headsethook_name,
                                AppDefine.FT_SUCCESS);
                    }
                });
        builder.setNegativeButton(R.string.Failed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Utils.SetPreferences(Signal.this, mSp, R.string.headsethook_name,
                        AppDefine.FT_FAILED);
            }
        });
        builder.create().show();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.emergency_call:
                Intent intent112 = new Intent("android.intent.action.CALL_PRIVILEGED");
                intent112.setData(Uri.fromParts("tel", "112", null));
                startActivityForResult(intent112, AppDefine.FT_HOOKSETID);
                break;
            case R.id.service_call:
                Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED");
                String simOper = SystemProperties.get("gsm.sim.operator.numeric", "46099");
                if("46000".equals(simOper) || "46002".equals(simOper) || "46007".equals(simOper) || "46008".equals(simOper)) {
                    intent.setData(Uri.fromParts("tel", "10086", null));
                } else if("46001".equals(simOper) || "46006".equals(simOper) || "46009".equals(simOper)) {
                    intent.setData(Uri.fromParts("tel", "10010", null));
                } else if("46003".equals(simOper) || "46005".equals(simOper) || "46011".equals(simOper)) {
                    intent.setData(Uri.fromParts("tel", "10000", null));
                } else {
                    intent.setData(Uri.fromParts("tel", "112", null));
                }
                startActivityForResult(intent, AppDefine.FT_HOOKSETID);
                break;
            case R.id.signal_bt_ok:
                Utils.SetPreferences(this, mSp, R.string.telephone_name, AppDefine.FT_SUCCESS);
                finish();
                break;
            case R.id.signal_bt_failed:
                Utils.SetPreferences(this, mSp, R.string.telephone_name, AppDefine.FT_FAILED);
                finish();
                break;
        }
    };
}
