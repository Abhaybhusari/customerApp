package com.appybite.customer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.gcm.C2DMRegistrationReceiver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.image.Utils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;
import com.yj.commonlib.util.KeyboardUtil;

public class LoginActivity extends Activity {

	public final static String AUTH = "authentication";
	private EditText edt_password;
	private EditText edt_email;
	private Button btn_login, btn_sign_up;
	
	private ImageView ivHotelLogo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);

		if(DeviceUtil.isTabletByRes(this))
			PRJFUNC.getScreenInfo_Tab(this);
		else 
			PRJFUNC.getScreenInfo(this);
		
		updateLCD();

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView();
		}
		
		initImageLoader(getApplicationContext());
		
		if (PrefValue.getString(this, R.string.pref_customer_id, null) != null) {

			if(PrefValue.getString(this, LoginActivity.AUTH, null) == null)
				c2dmregister(); // call for push notification registration id

			Intent i = null;
			if(DeviceUtil.isTabletByRes(LoginActivity.this)) {
				i = new Intent(LoginActivity.this, MainActivity_Tab.class);
			} else {
				i = new Intent(LoginActivity.this, MainActivity.class);
			}

			/*if(DeviceUtil.isTabletByRes(LoginActivity.this)) {
				//i = new Intent(LoginActivity.this, AllowedHotels_Tab.class);
			} else {
				i = new Intent(LoginActivity.this, AllowedHotels.class);
			}*/
			
			ImageLoader.getInstance().stop();
			ImageLoader.getInstance().clearMemoryCache();

			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		} else {
			
		}
	}
	
	// //////////////////////////////////////////////////
	private void updateLCD() {

		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(this);
		}
		
		edt_email = (EditText) findViewById(R.id.etUserName);
		edt_password = (EditText) findViewById(R.id.etPwd);
		
		edt_email.setText("demo@appybite.com");
		edt_password.setText("demo");
		
		btn_login = (Button) findViewById(R.id.btLogin);
		btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				hideKeyboard();
				doLogin();
			}
		});
		
		btn_sign_up = (Button) findViewById(R.id.btRegister);
		btn_sign_up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				hideKeyboard();

				Intent intent = null;
				if(DeviceUtil.isTabletByRes(LoginActivity.this)) {
					intent = new Intent(LoginActivity.this, RegisterActivity_Tab.class);
				} else {
					intent = new Intent(LoginActivity.this, RegisterActivity.class);
				}

				startActivity(intent);
			}
		});
		
		ivHotelLogo = (ImageView)findViewById(R.id.ivHotelLogo);
	}

	private void scaleView() {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
//		ImageView ivLogo = (ImageView)findViewById(R.id.ivLogo);
//		PRJFUNC.mGrp.relayoutView(ivLogo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(edt_email, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setEditTextFontScale(edt_email);
		PRJFUNC.mGrp.relayoutView(edt_password, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setEditTextFontScale(edt_password);
		PRJFUNC.mGrp.relayoutView(btn_login, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setButtonFontScale(btn_login);
		PRJFUNC.mGrp.relayoutView(btn_sign_up, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setButtonFontScale(btn_sign_up);
		PRJFUNC.mGrp.relayoutView(ivHotelLogo, LayoutLib.LP_RelativeLayout);
		
		//. AppInfo
		TextView tvAppVer = (TextView)findViewById(R.id.tvAppVer);
		PRJFUNC.mGrp.repaddingView(tvAppVer);
		PRJFUNC.mGrp.setTextViewFontScale(tvAppVer);

		TextView tvWebsite = (TextView)findViewById(R.id.tvWebsite);
		PRJFUNC.mGrp.repaddingView(tvWebsite);
		PRJFUNC.mGrp.setTextViewFontScale(tvWebsite);
	}
	
	private void doLogin()
	{
		if (haveInternet()) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/loginBrand.php?email_id=b@b.com&password=Yg==
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/loginBrand.php?email_id=test@test.com&password=123
			
			//http://www.appyorder.com/pro_version/webservice_smart_app/new/loginBrand.php?email_id=abc@gmail.com&password=YWNjZXNz
			RequestParams params = new RequestParams();
			params.add("email_id", edt_email.getText().toString());
			params.add("password", Base64.encodeToString(edt_password.getText().toString().getBytes(), Base64.NO_WRAP)/*Base64.encodeToString(edt_password.getText().toString().getBytes(), Base64.NO_WRAP)*/);
			
			DialogUtils.launchProgress(LoginActivity.this, "Please wait while logging in...");
			CustomerHttpClient.get("new/loginBrand.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(LoginActivity.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {

		            	/*
		            	///////////////////////////////
		            	pro
						{
						    "status": "true",
						    "Parent_hotel": "6759",
						    "customer_Details": {
						        "id": "268",
						        "name": "A",
						        "address": "A",
						        "state": "",
						        "city": "A",
						        "email_id": "b@b.com",
						        "phone": "1",
						        "p_code": "1",
						        "country": ""
						    },
						    "currency": "USD",
						    "hname":"Hilton Resorts"
						    "long": "100.61",
    						"lat": "13.8197"
    						"allowed hotel list":{"status":"false","message":"No More Restaurant"}
						}
						///////////////////////////////
						demo
						{
						    "status": "true",
						    "data": {
						        "id": "1",
						        "name": "mohamed",
						        "email": "test@test.com",
						        "hotel": "18",
						        "stat": "demo"
						    }
						}		            	 
		            	*/

	            		String result = new String(response);
	            		result.replace("\n", "");
	            		result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = (JSONObject) new JSONObject(result);
						String status = jsonObject.getString("status");
					
						if(status.equalsIgnoreCase("true"))
						{
							JSONObject jsonData = null;
							if(jsonObject.has("data"))
								jsonData = jsonObject.getJSONObject("data");
							
							if(jsonData != null && jsonData.getString("stat").equalsIgnoreCase("demo")) {
								
								PrefValue.setBoolean(LoginActivity.this, R.string.pref_app_demo, true);
								
								String customer_id = jsonData.getString("id");
								String customer_name = jsonData.getString("name");
								String customer_email_id = jsonData.getString("email");
								String hotel_id = jsonData.getString("hotel");
								
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_id, customer_id);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_name, customer_name);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_email_id, customer_email_id);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_pwd, edt_password.getText().toString());
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_id, hotel_id);
								
							} else {
								
								PrefValue.setBoolean(LoginActivity.this, R.string.pref_app_demo, false);
								
								JSONObject cashierDetails = jsonObject.getJSONObject("customer_Details");
								String customer_id = cashierDetails.getString("id");
								String customer_name = cashierDetails.getString("name");
								String customer_address = cashierDetails.getString("address");
								String customer_state = cashierDetails.getString("state");
								String customer_email_id = cashierDetails.getString("email_id");
								String customer_phone = cashierDetails.getString("phone");
								String customer_p_code = cashierDetails.getString("p_code");
								String customer_country = cashierDetails.getString("country");

								String hotel_id = jsonObject.getString("hotel");
								String currency = jsonObject.getString("currency");
								String hotel_name = jsonObject.getString("hname");
								String lat = jsonObject.getString("lat");
								String lon = jsonObject.getString("long");

								JSONObject hotelLogoDetails = jsonObject.getJSONObject("images");
								String hotelLogo = hotelLogoDetails.getString("hotel_logo");
//								Bitmap bitmap = getBitmapFromURL(hotelLogo);
								
								JSONObject hotelImageDetails = jsonObject.getJSONObject("rest_images");
								String hotelImages = hotelImageDetails.getString("restaurant_image");
								String restaurantThumb = hotelImageDetails.getString("restaurant_thmb");
								
								
								String appWelcomeDetails = jsonObject.getString("app_welcome");
//								bundle.putString("appWelcomeDetails", appWelcomeDetails);
//								bundle.putString("hotelLogo", hotelLogo);
//								bundle.putString("hotelImages", hotelImages);
								

								PrefValue.setString(LoginActivity.this, R.string.pref_customer_id, customer_id);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_name, customer_name);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_address, customer_address);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_pwd, edt_password.getText().toString());
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_state, customer_state);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_email_id, customer_email_id);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_phone, customer_phone);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_p_code, customer_p_code);
								PrefValue.setString(LoginActivity.this, R.string.pref_customer_country, customer_country);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_id, hotel_id);
								PrefValue.setString(LoginActivity.this, R.string.pref_currency, currency);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_name, hotel_name);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_lat, lat);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_lon, lon);
								
								// new one
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_welcome_name, appWelcomeDetails);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_bg, hotelImages);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_background_image, hotelImages);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_logo, hotelLogo);
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_thumb, restaurantThumb);

								JSONObject allowedHotelObject = jsonObject.getJSONObject("allowed hotel list");
								String allowedStatus = allowedHotelObject.getString("status");
								PrefValue.setString(LoginActivity.this, R.string.pref_hotel_allowed, allowedStatus);
								
								if(PrefValue.getString(LoginActivity.this, LoginActivity.AUTH, null) == null)
									c2dmregister(); // call for push notification registration id
							}
								
							Intent i = null;
							
							
							if(DeviceUtil.isTabletByRes(LoginActivity.this)) {
								i = new Intent(LoginActivity.this, MainActivity_Tab.class);
								
								
							} else {
								i = new Intent(LoginActivity.this, MainActivity.class);
								
							}
							/*if(DeviceUtil.isTabletByRes(LoginActivity.this)) {
								i = new Intent(LoginActivity.this, MainActivity_Tab.class);
							} else {
								i = new Intent(LoginActivity.this, AllowedHotels.class);
							}*/
							
							ImageLoader.getInstance().stop();
							ImageLoader.getInstance().clearMemoryCache();

							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(i);
							finish();
						}
						else
						{
							String message = jsonObject.getString("message");
							Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
	            }
	        });

		} else {
			Toast.makeText(this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}
	
	public boolean haveInternet() {
		try {
			final ConnectivityManager conn_manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo info = conn_manager.getActiveNetworkInfo();
			if (info == null || !info.isConnected()) {
				return false;
			}
			if (info.isRoaming()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void hideKeyboard()
	{
		KeyboardUtil.hideKeyboard(this, edt_email);
		KeyboardUtil.hideKeyboard(this, edt_password);
	}
	
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				// .writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	public void c2dmregister() { 
		Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
		intent.putExtra("app",PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		intent.putExtra("sender", C2DMRegistrationReceiver.senderID/* "appzeal.c2dm@gmail.com" *//* "k2anup@gmail.com" */);
		startService(intent);
	}
	
	
	

	public Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			URLConnection connection = (URLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			input.close();
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
