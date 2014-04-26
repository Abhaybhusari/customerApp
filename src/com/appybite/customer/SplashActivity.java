package com.appybite.customer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.yj.commonlib.util.DeviceUtil;

public class SplashActivity extends Activity {

	private final int SPLASH_DISPLAY_LENGHT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(DeviceUtil.isTabletByRes(this) && 
				getWindowManager().getDefaultDisplay().getOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			return;
		}

		setContentView(R.layout.activity_splash);

		//. Don't let activity be killed when new activity opens
		if (Build.VERSION.SDK_INT >= 17)
		{
			int set_finish = Settings.System.getInt(getContentResolver(),
					Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
			if (set_finish != 0)
				Settings.System.putInt(getContentResolver(),
						Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
		}
		else
		{
			int set_finish = Settings.System.getInt(getContentResolver(),
					Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
			if (set_finish != 0)
				Settings.System.putInt(getContentResolver(),
						Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);			
		}

		//. Splash Timer
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				
				Intent intent = null;
				if(DeviceUtil.isTabletByRes(SplashActivity.this)) {
					intent = new Intent(SplashActivity.this, LoginActivity_Tab.class);
				} else {
					intent = new Intent(SplashActivity.this, LoginActivity.class);
				}
				startActivity(intent);
				finish();
			}
		}, SPLASH_DISPLAY_LENGHT);
	}
}
