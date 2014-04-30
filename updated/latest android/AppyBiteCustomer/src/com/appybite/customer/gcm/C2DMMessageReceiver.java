package com.appybite.customer.gcm;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appybite.customer.MainActivity;
import com.appybite.customer.R;
import com.appybite.customer.SplashActivity;
import com.appybite.customer.db.MessageDatabase;
import com.yj.commonlib.pref.PrefValue;

public class C2DMMessageReceiver extends BroadcastReceiver {
	
	long time = 0;
	final String TAG = "C2DMMessageReceiver";
	Context ctx;
	SharedPreferences pref = null;

	ArrayList<String> url_list = null;
	ArrayList<String> url_name = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		ctx = context;
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		if ("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
			
			Calendar currentDate = Calendar.getInstance();
			time = currentDate.getTime().getTime();
			handleMessage(intent);
			// createNotification(context, msg);
			if(MainActivity.getInstance() == null)
				showSupportNotification(context);
		}
	}

	private void handleMessage(Intent intent) {
		
		String tag = intent.getStringExtra("tag");
		String msg = intent.getStringExtra("message");
		Log.i("push tag", tag);
		Log.i("push msg", msg);
			
		try {
			msg = new String(msg.getBytes(), "utf-8");
			tag = new String(tag.getBytes(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		insertNotificationMessage(ctx, tag, msg, time);
		
        MainActivity.updateGCM();
	}

	public void insertNotificationMessage(Context context, String tag, String msg, long time) {
		
		String hotel_id = PrefValue.getString(context, R.string.pref_hotel_id);
		String c_id = PrefValue.getString(context, R.string.pref_customer_id);
		
		MessageDatabase msg_db = new MessageDatabase(ctx);
		msg_db.insert(hotel_id, c_id, tag, msg, time, 0);
		msg_db.close();
	}
	
	public void showSupportNotification(Context context) {

		NotificationManager manager = 
                 (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder = 
                new NotificationCompat.Builder(context);

		builder.setSmallIcon(R.drawable.ic_launcher)
                 .setContentTitle(context.getString(R.string.app_name))
                 .setContentText("New Message came from " + context.getString(R.string.app_name))
                 .setAutoCancel(true);

		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

		if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
				long[] vib = {0,100,300,600};
				builder.setVibrate(vib);
			}
			else {
				builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), RingtoneManager.TYPE_NOTIFICATION);
			}
		}

		// 알람 클릭시 MainActivity를 화면에 띄운다.
		
		Intent in = new Intent(context, SplashActivity.class);
		in.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, in, 0);

		builder.setContentIntent(pIntent);

		manager.notify(0, builder.build());
	}
}
