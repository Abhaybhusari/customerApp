package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.info.HotelInfo;
import com.appybite.customer.location.GPSTracker;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;
import com.yj.commonlib.util.KeyboardUtil;

public class RegisterActivity extends Activity {

	private EditText etEmail, etPwd, etName, etPhone, etCity, etStreet, etPostCode;
	private Button btRegister;
	private ImageView ivHotelLogo;
	
	private int hotel_id = 6759;
	private String hotel_license = "demo";
	
	private LinearLayout llHotelList;
	private HorizontalScrollView hsHotelList;
	
	private ArrayList<HotelInfo> aryHotelList = new ArrayList<HotelInfo>();
	
	GPSTracker mGPS;
	
	private DisplayImageOptions options, optionsForBg;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_register);

		updateLCD();

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView();
		}
		
		loadHotelList();
	}

	// //////////////////////////////////////////////////
	private void updateLCD() {

		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(this);
		}
		
		etEmail = (EditText)findViewById(R.id.etEmail);
		etPwd = (EditText)findViewById(R.id.etPwd);
		etName = (EditText)findViewById(R.id.etName);
		etPhone = (EditText)findViewById(R.id.etPhone);
		etCity = (EditText)findViewById(R.id.etCity);
		etStreet = (EditText)findViewById(R.id.etStreet);
		etPostCode = (EditText)findViewById(R.id.etPostCode);
		
		btRegister = (Button)findViewById(R.id.btRegister);
		btRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				hideKeyboard();
				register();
			}
		});
		
		ivHotelLogo = (ImageView)findViewById(R.id.ivHotelLogo);
		
		llHotelList = (LinearLayout)findViewById(R.id.llHotelList);
		hsHotelList = (HorizontalScrollView)findViewById(R.id.hsHotelList);
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_depart)
			.showImageForEmptyUri(R.drawable.bg_default_depart)
			.showImageOnFail(R.drawable.bg_default_depart)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		optionsForBg = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_restaurant)
			.showImageForEmptyUri(R.drawable.bg_default_restaurant)
			.showImageOnFail(R.drawable.bg_default_restaurant)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build(); 
	}

	private void scaleView() {

		if (PRJFUNC.mGrp == null) {
			return;
		}

		TextView tvRegister = (TextView)findViewById(R.id.tvRegister);
		PRJFUNC.mGrp.relayoutView(tvRegister, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvRegister);
		PRJFUNC.mGrp.relayoutView(ivHotelLogo, LayoutLib.LP_RelativeLayout);

		//. AppInfo
		TextView tvAppVer = (TextView)findViewById(R.id.tvAppVer);
		PRJFUNC.mGrp.repaddingView(tvAppVer);
		PRJFUNC.mGrp.setTextViewFontScale(tvAppVer);

		TextView tvWebsite = (TextView)findViewById(R.id.tvWebsite);
		PRJFUNC.mGrp.repaddingView(tvWebsite);
		PRJFUNC.mGrp.setTextViewFontScale(tvWebsite);
	}

	public void register() {
		
		String email, pwd, name, phone, city, street, postcode;
		email = etEmail.getText().toString();
		pwd = etPwd.getText().toString();
		name = etName.getText().toString();
		phone = etPhone.getText().toString();
		city = etCity.getText().toString();
		street = etStreet.getText().toString();
		postcode = etPostCode.getText().toString();
		
		if(email.trim().length() == 0) {
			MessageBox.OK(this, "Missing Information", "Please input E-MAIL.");
			return;
		} else if(pwd.trim().length() == 0) {
			MessageBox.OK(this, "Missing Information", "Please input password.");
			return;
		} else if(name.trim().length() == 0) {
			MessageBox.OK(this, "Missing Information", "Please input your name.");
			return;
		}
	
		if (NetworkUtils.haveInternet(this)) {
			
			if (hotel_license == "pro") {
				//. http://www.appyorder.com/pro_version/webservice_smart_app/new/Register_HotelID.php?
				//. hotel_id=6759&name=adam&email_id=adam@gmail.com&phone_no=01229945960&city=Alexabdria&password=encodedbase64&street_no=St&postcode=145
				
				RequestParams params = new RequestParams();
				params.add("hotel_id", String.valueOf(hotel_id));
				params.add("email_id", email);
				params.add("password", pwd);
				params.add("name", name);
				params.add("phone_no", phone);
				params.add("city", city);
				params.add("street_no", street);
				params.add("postcode", postcode);
				
				DialogUtils.launchProgress(this, "Please wait while Registering...");
				CustomerHttpClient.get("new/Register_HotelID.php", params, new AsyncHttpResponseHandler() {
					@Override
					public void onFinish() {
						
						DialogUtils.exitProgress();
						super.onFinish();
					}
	
					@Override
					public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
						
						Toast.makeText(RegisterActivity.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
						super.onFailure(statusCode, headers, errorResponse, e);
					}
					
		            @Override
		            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
		                // Pull out the first event on the public timeline
		            	try {
	
			            	/*
							{
							    "status": "true",
							    "message": "Successfully Registration Please Check Your Email",
							    "id": "297"
							}
							*/
	
		            		String result = new String(response);
		            		result = result.replace("({", "{");
		            		result = result.replace("})", "}");
		            		Log.i("HTTP Response <<<", result);
		            		JSONObject jsonObject = new JSONObject(result);
		            		
		            		String status = jsonObject.getString("status");
		            		String msg = jsonObject.getString("message");
		            		
		            		if(status.equalsIgnoreCase("true")) {
		            			
		            			Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
		            			RegisterActivity.this.finish();
		            			
		            		} else {
		            			
		            			MessageBox.OK(RegisterActivity.this, "Error", msg);
		            		}
		            		
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(RegisterActivity.this, "Invalid Data",Toast.LENGTH_LONG).show();
						} 
		            }
		        });
			} else {
				// http://www.roomallocator.com/appcreator/services/insert_new_cust.php?
				//	name=mohaaacccc&password=123&hotel=18&email=abc@test.com
				
				RequestParams params = new RequestParams();
				params.add("hotel", String.valueOf(hotel_id));
				params.add("email", email);
				params.add("password", pwd);
				params.add("name", name);
				
				DialogUtils.launchProgress(this, "Please wait while Registering...");
				CustomerHttpClient.getFromFullService("http://www.roomallocator.com/appcreator/services/insert_new_cust.php", params, new AsyncHttpResponseHandler() {
					@Override
					public void onFinish() {
						
						DialogUtils.exitProgress();
						super.onFinish();
					}
	
					@Override
					public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
						
						Toast.makeText(RegisterActivity.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
						super.onFailure(statusCode, headers, errorResponse, e);
					}
					
		            @Override
		            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
		                // Pull out the first event on the public timeline
		            	try {
	
			            	/*
							{
							    "status": "true",
							    "message": "Successfully Registration Please Check Your Email",
							    "id": "297"
							}
							*/
	
		            		String result = new String(response);
		            		result = result.replace("({", "{");
		            		result = result.replace("})", "}");
		            		Log.i("HTTP Response <<<", result);
		            		JSONObject jsonObject = new JSONObject(result);
		            		
		            		String status = jsonObject.getString("status");
		            		String msg = jsonObject.getString("message");
		            		
		            		if(status.equalsIgnoreCase("true")) {
		            			
		            			Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
		            			RegisterActivity.this.finish();
		            			
		            		} else {
		            			
		            			MessageBox.OK(RegisterActivity.this, "Error", msg);
		            		}
		            		
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							Toast.makeText(RegisterActivity.this, "Invalid Data",Toast.LENGTH_LONG).show();
						} 
		            }
		        });
			}

		} else {
			Toast.makeText(RegisterActivity.this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}
	
	private void hideKeyboard()
	{
		KeyboardUtil.hideKeyboard(this, etEmail);
		KeyboardUtil.hideKeyboard(this, etPwd);
		KeyboardUtil.hideKeyboard(this, etName);
		KeyboardUtil.hideKeyboard(this, etPhone);
		KeyboardUtil.hideKeyboard(this, etCity);
		KeyboardUtil.hideKeyboard(this, etStreet);
		KeyboardUtil.hideKeyboard(this, etPostCode);
	}
	
	private void loadHotelList() {
		aryHotelList.clear();
		
		if (NetworkUtils.haveInternet(this)) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetDepartments.php?hotel_id=6759
			//. http://appyorder.com/pro_version/webservice_smart_app/new/getGeoHotels.php?lat=37&long=-122&rad=6000&t=1
			
			RequestParams params = new RequestParams();
			/*params.add("lat", String.valueOf(mGPS.getLatitude()));
			params.add("long", String.valueOf(mGPS.getLongitude()));*/
			params.add("lat", "37");
			params.add("long", "-122");
			params.add("rad", "6000");
			params.add("t", "1");
			
			DialogUtils.launchProgress(this, "Please wait while loading Data");
			CustomerHttpClient.get("new/getGeoHotels.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(RegisterActivity.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {

	            		/*"pro" : [
	            		  		{
	            		  			"hotel_id" : "6723",
	            		  			"hotel_name" : "Abundant Grounds Cafe",
	            		  			"hotel_logo" : "",
	            		  			"address" : "1600 Kennesaw Due West Rd",
	            		  			"country_id" : "",
	            		  			"phone_no" : "+1 678-354-6155",
	            		  			"email_id" : "appyAbundant@gmail.com",
	            		  			"language" : "",
	            		  			"hotel_desc" : "Our mission is to provide an eclectic and comfortable environment for our customers to enjoy not only coffee and light food, but conversation and culture, while knowing their patronage of our establishment helps give back to the community. ",
	            		  			"resto_website" : "http:\/\/www.abundantgrounds.com\/",
	            		  			"restaurant_image" : "",
	            		  			"application_icon" : "",
	            		  			"aboutus_img" : "",
	            		  			"background_img" : "",
	            		  			"country_id" : "8",
	            		  			"lat" : "33.8621",
	            		  			"long" : "-84.6879",
	            		  			"distance" : "2098.48547281184"
	            		  		}
	            		  "demo" : [
	            		  		{
	            		  			"id":"89",
	            		  			"user_id":"130",
	            		  			"hotel_name":"Pablos",
	            		  			"city":"Leeds",
	            		  			"country":"United Kingdom",
	            		  			"logo":"screenshot_1616.jpg",
	            		  			"mainbackground":"",
	            		  			"rooms":"",
	            		  			"resturant":"",
	            		  			"sightseeing":"",
	            		  			"address":"dfdf",
	            		  			"add_diff_lang":"",
	            		  			"lat":"55.776573",
	            		  			"long":"-2.812500",
	            		  			"numroom":"",
	            		  			"curancy":"",
	            		  			"tax":"7",
	            		  			"charge":"10",
	            		  			"address2":"2323",
	            		  			"phone":"343434",
	            		  			"distance":"5101.15882934968"
	            		  		}*/
	            		 
	            		String result = new String(response);
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonRoot = new JSONObject(result);
	        			
	        			Boolean bStatus = true;
	        			if (jsonRoot.has("demo")) {
	        				try {
			       				JSONArray temp = jsonRoot.getJSONArray("demo");
		       				 	if (temp.length() > 0) {
			       					 for (int i=0; i<temp.length(); i ++) {
			       						 JSONObject objHotels = temp.getJSONObject(i);
			       						 HotelInfo info = new HotelInfo();
			       						 info.hotel_id 	= Integer.parseInt(objHotels.getString("id"));
			       						 info.license 	= "demo";
			       						 info.hotel_name = objHotels.getString("hotel_name");
			       						 info.hotel_logo = objHotels.getString("logo");
			       						 info.address 	= objHotels.getString("address");
			       						 info.country	= objHotels.getString("country");
			       						 info.phone_no	= objHotels.getString("phone");
			       						 info.hotel_desc = "";
			       						 info.lat		= objHotels.getString("lat");
			       						 info.lon		= objHotels.getString("long");
			       						 info.hotel_bg	= objHotels.getString("mainbackground");
			       						 
			       						 aryHotelList.add(info);
			       					 }
			       				 }
	        				} catch (Exception e) {
	        					e.printStackTrace();
	        				}
		       			}
	        			if (jsonRoot.has("pro")) {
	        				JSONArray temp = jsonRoot.getJSONArray("pro");
	       				 	if (temp.length() > 0) {
		       					 for (int i=0; i<temp.length(); i ++) {
		       						 JSONObject objHotels = temp.getJSONObject(i);
		       						 HotelInfo info = new HotelInfo();
		       						 info.hotel_id 	= Integer.parseInt(objHotels.getString("hotel_id"));
		       						 info.license 	= "pro";
		       						 info.hotel_name = objHotels.getString("hotel_name");
		       						 info.hotel_logo = objHotels.getString("hotel_logo");
		       						 info.address 	= objHotels.getString("address");
		       						 info.country	= objHotels.getString("country_id");
		       						 info.phone_no	= objHotels.getString("phone_no");
		       						 info.hotel_desc = objHotels.getString("hotel_desc");
		       						 info.website	= objHotels.getString("resto_website");
		       						 info.lat		= objHotels.getString("lat");
		       						 info.lon		= objHotels.getString("long");
		       						 info.hotel_bg	= objHotels.getString("background_img");
		       						 
		       						 aryHotelList.add(info);
		       					 }
		       				 }
		       			}
	        			
	            		/*if(DeviceUtil.isTabletByRes(RegisterActivity.this)) {
	            			ImageLoader.getInstance().displayImage(PrefValue.getString(RegisterActivity.this, R.string.pref_hotel_bg), ivHotelBg, optionsForBg, animateFirstListener);
	            			ImageLoader.getInstance().displayImage(PrefValue.getString(RegisterActivity.this, R.string.pref_hotel_logo), ivHotelLogo, null, animateFirstListener);
	            			tvHotelName.setText(PrefValue.getString(RegisterActivity.this, R.string.pref_hotel_name));
	            		} else {
		            		if(m_Fragment != null && m_Fragment.getClass().getName() == HomeFragment.class.getName())
		            			((HomeFragment)m_Fragment).loadHotelInfo();
	            		}*/
	            		
	            		updateHotelList();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(RegisterActivity.this, "Invalid Data",Toast.LENGTH_LONG).show();
					} 
	            }
	        });

		} else {
			Toast.makeText(RegisterActivity.this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}
	
	private void updateHotelList() {
		//. Department List
		llHotelList.removeAllViews();
		
		for (int i = 0; i < aryHotelList.size(); i++) {

			final HotelInfo value = aryHotelList.get(i);

			LayoutInflater vi = (LayoutInflater) getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			
			View item = null;
			if(DeviceUtil.isTabletByRes(this))
				item = vi.inflate(R.layout.item_depart_tab, llHotelList, false);
			else
				item = vi.inflate(R.layout.item_depart, llHotelList, false);
			
			ImageView ivThumb = (ImageView)item.findViewById(R.id.ivThumb);
			if (value.license == "pro") {
				ImageLoader.getInstance().displayImage(value.hotel_logo, ivThumb, options, animateFirstListener);
			} else if (value.license == "demo") {
				ImageLoader.getInstance().displayImage(value.hotel_logo, ivThumb, options, animateFirstListener);
			}
			
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setText(value.hotel_name);
			tvTitle.setSelected(true);
			if(i == 0)
				tvTitle.setTextColor(getResources().getColor(R.color.Goldenrod));

			item.setTag(tvTitle);

			item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

					unselectHotels();
					((TextView)arg0.getTag()).setTextColor(getResources().getColor(R.color.Goldenrod));
					hotel_id = value.hotel_id;
					hotel_license = value.license;
				}
			});
			
			if (!PRJFUNC.DEFAULT_SCREEN) {
				
				PRJFUNC.mGrp.relayoutView(item, LayoutLib.LP_LinearLayout);
				PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
				PRJFUNC.mGrp.repaddingView(tvTitle);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
			}
			llHotelList.addView(item);
		}
	}
	
	private void unselectHotels()
	{
		for (int j = 0; j < llHotelList.getChildCount(); j++) {
			
			View item = llHotelList.getChildAt(j);
			TextView tvTitle = (TextView)item.getTag();
			tvTitle.setTextColor(getResources().getColor(R.color.White));
		}
	}
}
