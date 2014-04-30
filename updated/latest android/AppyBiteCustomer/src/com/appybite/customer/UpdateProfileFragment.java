package com.appybite.customer;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.KeyboardUtil;

public class UpdateProfileFragment extends Fragment {

	private EditText etEmail, etPwd, etName, etPhone, etCity, etStreet, etPostCode;
	private Button btRegister;

	public UpdateProfileFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_updateprofile, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		return v;
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		etEmail = (EditText)v.findViewById(R.id.etEmail);
		etPwd = (EditText)v.findViewById(R.id.etPwd);
		etName = (EditText)v.findViewById(R.id.etName);
		etPhone = (EditText)v.findViewById(R.id.etPhone);
		etCity = (EditText)v.findViewById(R.id.etCity);
		etStreet = (EditText)v.findViewById(R.id.etStreet);
		etPostCode = (EditText)v.findViewById(R.id.etPostCode);
		
		btRegister = (Button)v.findViewById(R.id.btRegister);
		btRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				hideKeyboard();
				register();
			}
		});

		etEmail.setText(PrefValue.getString(getActivity(), R.string.pref_customer_email_id));
		etName.setText(PrefValue.getString(getActivity(), R.string.pref_customer_name));
		etPhone.setText(PrefValue.getString(getActivity(), R.string.pref_customer_phone));
		etCity.setText(PrefValue.getString(getActivity(), R.string.pref_customer_city));
		etStreet.setText(PrefValue.getString(getActivity(), R.string.pref_customer_state));
		etPostCode.setText(PrefValue.getString(getActivity(), R.string.pref_customer_p_code));
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
	}
	
	public void register() {
		
		Toast.makeText(getActivity(), "Preparing...", Toast.LENGTH_LONG).show();
		
//		String email, pwd, name, phone, city, street, postcode;
//		email = etEmail.getText().toString();
//		pwd = etPwd.getText().toString();
//		name = etName.getText().toString();
//		phone = etPhone.getText().toString();
//		city = etCity.getText().toString();
//		street = etStreet.getText().toString();
//		postcode = etPostCode.getText().toString();
//		
//		if(email.trim().length() == 0) {
//			MessageBox.OK(getActivity(), "Missing Information", "Please input E-MAIL.");
//			return;
//		} else if(pwd.trim().length() == 0) {
//			MessageBox.OK(getActivity(), "Missing Information", "Please input password.");
//			return;
//		} else if(name.trim().length() == 0) {
//			MessageBox.OK(getActivity(), "Missing Information", "Please input your name.");
//			return;
//		}
	}
	
	private void hideKeyboard()
	{
		KeyboardUtil.hideKeyboard(getActivity(), etEmail);
		KeyboardUtil.hideKeyboard(getActivity(), etPwd);
		KeyboardUtil.hideKeyboard(getActivity(), etName);
		KeyboardUtil.hideKeyboard(getActivity(), etPhone);
		KeyboardUtil.hideKeyboard(getActivity(), etCity);
		KeyboardUtil.hideKeyboard(getActivity(), etStreet);
		KeyboardUtil.hideKeyboard(getActivity(), etPostCode);
	}
	
	private void updateProfile() {
		
		String email, pwd, name, phone, city, street, postcode;
		email = etEmail.getText().toString();
		pwd = etPwd.getText().toString();
		name = etName.getText().toString();
		phone = etPhone.getText().toString();
		city = etCity.getText().toString();
		street = etStreet.getText().toString();
		postcode = etPostCode.getText().toString();
		
		if(email.trim().length() == 0) {
			MessageBox.OK(getActivity(), "Missing Information", "Please input E-MAIL.");
			return;
		} else if(pwd.trim().length() == 0) {
			MessageBox.OK(getActivity(), "Missing Information", "Please input password.");
			return;
		} else if(name.trim().length() == 0) {
			MessageBox.OK(getActivity(), "Missing Information", "Please input your name.");
			return;
		}
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/UpdateCustomer.php?hotel_id=6759&id=268&email_id=b@b.com&phone_no=035516789&&city=city&location=location&street_no=555&postcode=20020&house_no=4&password=Yg==
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("id", cid);
			params.add("email_id", email);
			params.add("phone_no", hotel_id);
			params.add("city", hotel_id);
			params.add("street_no", hotel_id);
			params.add("location", hotel_id);
			params.add("house_no", hotel_id);
			params.add("postcode", hotel_id);
			params.add("password", hotel_id);
			
			DialogUtils.launchProgress(getActivity(), "Updating profile...");
			CustomerHttpClient.get("new/UpdateCustomer.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
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
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
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
