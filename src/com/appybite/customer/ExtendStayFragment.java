package com.appybite.customer;

import org.apache.http.Header;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.dialog.NumberPickerDlg;
import com.yj.commonlib.dialog.NumberPickerDlg.OnFinishedListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class ExtendStayFragment extends Fragment {

	//. Check Table
	private Button btExtendDays, btExtendStay;
	private ProgressBar pbLoading;
	
	public ExtendStayFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_extendstay_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_extendstay, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		return v;
	}
	
	@Override
	public void onDestroy() {
		
		CustomerHttpClient.stop();
		super.onDestroy();
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		//. Check Table
		btExtendDays = (Button)v.findViewById(R.id.btExtendDays);
		btExtendDays.setText("0 days");
		btExtendDays.setTag("0");
		btExtendDays.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				NumberPickerDlg countDlg = new NumberPickerDlg(getActivity(), Integer.parseInt((String)btExtendDays.getTag()), new OnFinishedListener() {
					
					@Override
					public void onOk(int number) {
						
						btExtendDays.setText(String.valueOf(number) + " days");
						btExtendDays.setTag(String.valueOf(number));
					}
				});
				countDlg.show();
			}
		});
		
		btExtendStay = (Button)v.findViewById(R.id.btExtendStay);
		btExtendStay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				int days = Integer.parseInt((String)btExtendDays.getTag());
				if(days == 0) {
					MessageBox.OK(getActivity(), "Alert", "Please select extend days first.");
					return;
				}
				
				extendStay();
			}
		});
		
		pbLoading = (ProgressBar)v.findViewById(R.id.pbLoading);
		pbLoading.setVisibility(View.INVISIBLE);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}

		TextView tvExtendDays = (TextView)v.findViewById(R.id.tvExtendDays);
		PRJFUNC.mGrp.relayoutView(tvExtendDays, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvExtendDays);

		PRJFUNC.mGrp.relayoutView(btExtendDays, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btExtendDays);

		PRJFUNC.mGrp.relayoutView(btExtendStay, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btExtendStay);
	}
	
	private void extendStay() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. http://www.appyorder.com/pro_version/webservice_smart_app/extendStay.php?id=6759&email=mio@mio.mio&days=10
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String email = PrefValue.getString(getActivity(), R.string.pref_customer_email_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("email", email);
			params.add("days", (String)btExtendDays.getTag());
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("available_times.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbLoading.setVisibility(View.INVISIBLE);
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

						String responseBody = new String(response);
						Log.i("HTTP Response <<<", responseBody);
						MessageBox.OK(getActivity(), "Alert", responseBody);

					} catch (Exception e) {
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
