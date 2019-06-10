package com.dinghmcn.android.wificonnectclient;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * The type Show picture full activity.
 *
 * @author dinghmcn
 * @date 2018 /4/20 10:47
 */
public class ShowPictureFullActivity extends AppCompatActivity {
  private static final String TAG = "ShowPictureFullActivity";

  private ImageView mImageView;

  /**
   * On create.
   *
   * @param savedInstanceState the saved instance state
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    View decorView = getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(uiOptions);

    setContentView(R.layout.activity_full_show_picture);

    mImageView = (ImageView) findViewById(R.id.imageView);
  }

  /**
   * On resume.
   */
  @Override
  protected void onResume() {
    super.onResume();

    int resId = getIntent().getIntExtra("res_id", -1);
    if (resId > 0) {
      mImageView.setImageResource(resId);
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          finish();
        }
      },5000);
    } else {
      finish();
    }
  }

  /**
   * On pause.
   */
  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  /**
   * On back pressed.
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }
}
