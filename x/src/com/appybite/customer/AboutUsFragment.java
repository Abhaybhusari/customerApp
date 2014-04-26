package com.appybite.customer;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class AboutUsFragment extends Fragment {

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	private ImageView ivHotelBg;
	private TextView tvHotelName, tvDesc, tvOpening, tvClosing, tvAddress, tvPhone, tvEmail, tvCurrecy, tvWebsite;
	
	private ProgressBar pbHotelInfo;
	
	public AboutUsFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_aboutus_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_aboutus, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
	
		loadHotelInfo();
		
		return v;
	}
	
	@Override
	public void onDestroy() {
		
		CustomerHttpClient.stop();
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();
		super.onDestroy();
	}
	
	private void updateLCD(View v) {
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_category)
			.showImageForEmptyUri(R.drawable.bg_default_category)
			.showImageOnFail(R.drawable.bg_default_category)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		ivHotelBg = (ImageView)v.findViewById(R.id.ivHotelBg);
		tvHotelName = (TextView)v.findViewById(R.id.tvHotelName);
		tvHotelName.setText("");
		tvDesc = (TextView)v.findViewById(R.id.tvDesc);
		tvOpening = (TextView)v.findViewById(R.id.tvOpening);
		tvClosing = (TextView)v.findViewById(R.id.tvClosing);
		tvAddress = (TextView)v.findViewById(R.id.tvAddressValue);
		tvAddress.setText("");
		tvPhone = (TextView)v.findViewById(R.id.tvPhoneValue);
		tvPhone.setText("");
		tvPhone.setAutoLinkMask(Linkify.PHONE_NUMBERS);
		tvEmail = (TextView)v.findViewById(R.id.tvEmailValue);
		tvEmail.setText("");
		tvCurrecy = (TextView)v.findViewById(R.id.tvCurrencyValue);
		tvCurrecy.setText("");
		tvWebsite = (TextView)v.findViewById(R.id.tvWebsiteValue);
		tvWebsite.setText("");
		
		pbHotelInfo = (ProgressBar)v.findViewById(R.id.pbHotelInfo);
		pbHotelInfo.setVisibility(View.INVISIBLE);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		RelativeLayout rlLogo = (RelativeLayout)v.findViewById(R.id.rlItemInfo);
		PRJFUNC.mGrp.relayoutView(rlLogo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvHotelName);
		PRJFUNC.mGrp.relayoutView(tvHotelName, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvDesc);
		PRJFUNC.mGrp.relayoutView(tvDesc, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvDesc);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvOpening);
		PRJFUNC.mGrp.relayoutView(tvOpening, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvOpening);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvClosing);
		PRJFUNC.mGrp.relayoutView(tvClosing, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvClosing);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvAddress);
		PRJFUNC.mGrp.relayoutView(tvAddress, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvAddress);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvPhone);
		PRJFUNC.mGrp.relayoutView(tvPhone, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvPhone);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvEmail);
		PRJFUNC.mGrp.relayoutView(tvEmail, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvEmail);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvCurrecy);
		PRJFUNC.mGrp.relayoutView(tvCurrecy, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvCurrecy);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvWebsite);
		PRJFUNC.mGrp.relayoutView(tvWebsite, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvWebsite);

		TextView tv = (TextView)v.findViewById(R.id.tvAddressLabel);
		PRJFUNC.mGrp.setTextViewFontScale(tv);
		PRJFUNC.mGrp.relayoutView(tv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tv);
		
		tv = (TextView)v.findViewById(R.id.tvPhoneLabel);
		PRJFUNC.mGrp.setTextViewFontScale(tv);
		PRJFUNC.mGrp.relayoutView(tv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tv);

		tv = (TextView)v.findViewById(R.id.tvEmailLabel);
		PRJFUNC.mGrp.setTextViewFontScale(tv);
		PRJFUNC.mGrp.relayoutView(tv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tv);
		
		tv = (TextView)v.findViewById(R.id.tvCurrencyLabel);
		PRJFUNC.mGrp.setTextViewFontScale(tv);
		PRJFUNC.mGrp.relayoutView(tv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tv);
		
		tv = (TextView)v.findViewById(R.id.tvWebsiteLabel);
		PRJFUNC.mGrp.setTextViewFontScale(tv);
		PRJFUNC.mGrp.relayoutView(tv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tv);
		
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
	}
	
	public void loadHotelInfo() {
		
		ImageLoader.getInstance().displayImage(PrefValue.getString(getActivity(), R.string.pref_hotel_bg), ivHotelBg, options, animateFirstListener);
		tvHotelName.setText(PrefValue.getString(getActivity(), R.string.pref_hotel_name));

		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/aboutus.php?hotel_id=6759
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			pbHotelInfo.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/aboutus.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbHotelInfo.setVisibility(View.INVISIBLE);
					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(getActivity(), "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {

		            	/*
						{
						    "open_time": "09:00",
						    "closed_time": "23:30",
						    "hotel_details": {
						        "hotel_name": "Hilton Resorts",
						        "hotel_desc": "Restaurant Description",
						        "address": "12 - merfitte street",
						        "phone": "01299874589",
						        "email": "josemontes22@yahoo.com",
						        "currency": "USD",
						        "website": "hilton.th"
						    },
						    "hotel_image": {
						        "hotel_logo": "http://www.roomallocator.com/appybiteRestaurant/depimages/screenshot_1039_1.jpg",
						        "app_header": "http://www.roomallocator.com/appybiteRestaurant/depimages/screenshot_1091_1.jpg",
						        "app_icon": "http://www.roomallocator.com/appybiteRestaurant/depimages/xxxx.jpg"
						    }
						}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String opening = jsonObject.getString("open_time");
	            		String closing = jsonObject.getString("closed_time");
	            		
	            		JSONObject hotel_details = jsonObject.getJSONObject("hotel_details");
	            		// String hotel_name = hotel_details.getString("hotel_name");
	            		String hotel_desc = hotel_details.getString("hotel_desc");
	            		String address = hotel_details.getString("address");
	            		String phone = hotel_details.getString("phone");
	            		String email = hotel_details.getString("email");
	            		String currency = hotel_details.getString("currency");
	            		String website = hotel_details.getString("website");

	            		tvOpening.setText("Opening: " + opening);
	            		tvClosing.setText("Closing: " + closing);
	            		tvDesc.setText(hotel_desc);
	            		tvAddress.setText(address);
	            		tvPhone.setText(phone);
	            		tvEmail.setText(email);
	            		tvCurrecy.setText(currency);
	            		tvWebsite.setText(website);
	            		
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getActivity(), "Invalid Data",Toast.LENGTH_LONG).show();
					} 
	            }
	        });

		} else {
			Toast.makeText(getActivity(), "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}
}
