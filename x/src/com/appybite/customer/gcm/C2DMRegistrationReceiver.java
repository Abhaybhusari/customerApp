package com.appybite.customer.gcm;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

import com.appybite.customer.CustomerHttpClient;
import com.appybite.customer.LoginActivity;
import com.appybite.customer.R;
import com.appybite.customer.SplashActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;

public class C2DMRegistrationReceiver extends BroadcastReceiver {

	final String TAG = "C2DMRegistrationReceiver";
	public static String reg_id, deviceId;
	public final static String senderID = "384631831928";

	/*-------------------------------------------------------------------------------------*/
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
	//	Log.w("C2DM", "Registration Receiver called");
		if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
		//	Log.w("C2DM", "Received registration ID");
			final String registrationId = intent
					.getStringExtra("registration_id");
			reg_id=registrationId;
		//	Log.i(TAG,">>>reg_id"+registrationId);
			String error = intent.getStringExtra("error");

		/*	Log.d("C2DM", "dmControl: registrationId = " + registrationId
					+ ", error = " + error);*/
			deviceId = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);
			//Log.i("C2DM", "DEVICE_ID: " + deviceId);
			createNotification(context, registrationId);
			// new MyAsyncTask().execute("send registration id to server");
			sendRegistrationIdToServer(context, deviceId, registrationId);
			// Also save it in the preference to be able to show it later
			// saveRegistrationId(context, registrationId);
		}

	}

	/*-------------------------------------------------------------------------------------*/
	private void saveRegistrationId(Context context, String registrationId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putString(LoginActivity.AUTH, registrationId);
		edit.commit();
	}

	/*-------------------------------------------------------------------------------------*/
	public void createNotification(Context context, String registrationId) {
		try {
			
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_launcher, "Registration successful", System.currentTimeMillis());
			// Hide the notification after its selected
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			Intent intent = new Intent(context, SplashActivity.class);
			intent.putExtra("registration_id", registrationId);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			if(registrationId == null || registrationId.length() == 0)
				notification.setLatestEventInfo(context, "Registration", "Registration Failed", null);
			else
				notification.setLatestEventInfo(context, "Registration", "Successfully registered", null);
			notificationManager.notify(0, notification);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/*-------------------------------------------------------------------------------------*/
	// Incorrect usage as the receiver may be canceled at any time
	// do this in an service and in an own thread
	public void sendRegistrationIdToServer(final Context context, String deviceId,
			String registrationId) {
		//Log.d("C2DM", "Sending registration ID to my application server");
		if (NetworkUtils.haveInternet(context)) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app//new/UpdatePush.php?CID=44&div_unq_id=123&push_reg_id=321&hotel_id=6759
			String hotel_id = PrefValue.getString(context, R.string.pref_hotel_id);
			String CID = PrefValue.getString(context, R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("CID", CID);
			params.add("device_unique_id", deviceId);
			params.add("push_reg_id", registrationId);
			
			CustomerHttpClient.get("new/UpdatePush.php", params, new AsyncHttpResponseHandler() {

	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	            	
            		String result = new String(response);
            		result = result.replace("({", "{");
            		result = result.replace("})", "}");
            		Log.i("HTTP Response <<<", result);

					try {
						JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");
	            		if(status.equalsIgnoreCase("true"))
	            		{
	            			saveRegistrationId(context, reg_id);
	            		}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        });

		} else {
		}

	}
}
