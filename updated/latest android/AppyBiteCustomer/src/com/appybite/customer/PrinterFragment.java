package com.appybite.customer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.RT_Printer.WIFI.WifiPrintDriver;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class PrinterFragment extends Fragment {

	private EditText etPrinterIP, etPort;
	private Button btUpdate, btCheck; 
	private ImageView ivUpdate, ivCheck, ivPrinter;
	private ProgressBar pbPrinter;
	
	public PrinterFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_printer_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_printer, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
	
		loadPrinter();
		
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

		etPrinterIP = (EditText)v.findViewById(R.id.etPrinterIP);
		etPort = (EditText)v.findViewById(R.id.etPrinterPort);
		pbPrinter = (ProgressBar)v.findViewById(R.id.pbPrinter);
		pbPrinter.setVisibility(View.INVISIBLE);
		
		btUpdate = (Button)v.findViewById(R.id.btUpdatePrinter);
		btUpdate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				// Toast.makeText(getActivity(), "Preparing ..", Toast.LENGTH_LONG).show();
				updatePrinter();
			}
		});
		
		btCheck = (Button)v.findViewById(R.id.btCheckPrinter);
		btCheck.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				checkPrinter();
			}
		});
		
		ivCheck = (ImageView)v.findViewById(R.id.ivCheckPrinter);
		ivCheck.setVisibility(View.INVISIBLE);
		ivUpdate = (ImageView)v.findViewById(R.id.ivUpdatePrinter);
		ivUpdate.setVisibility(View.INVISIBLE);
		ivPrinter = (ImageView)v.findViewById(R.id.ivPrinter);
		
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		PRJFUNC.mGrp.setEditTextFontScale(etPrinterIP);
		PRJFUNC.mGrp.relayoutView(etPrinterIP, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setEditTextFontScale(etPort);
		PRJFUNC.mGrp.relayoutView(etPort, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setButtonFontScale(btUpdate);
		PRJFUNC.mGrp.relayoutView(btUpdate, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setButtonFontScale(btCheck);
		PRJFUNC.mGrp.relayoutView(btCheck, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.relayoutView(ivCheck, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(ivUpdate, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(ivPrinter, LayoutLib.LP_RelativeLayout);
	}
	
	public void loadPrinter()
	{
		String url = "http://www.roomallocator.com/restaurant/printerservicerest.php";
		if (NetworkUtils.haveInternet(getActivity())) {
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			 
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			DialogUtils.launchProgress(getActivity(), "Please wait while loading Printer");
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
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
						{
						    "printers": [
						        {
						            "ip": "192.168.1.87",
						            "port": "9100",
						            "periority": "1"
						        }
						    ],
						    "vat": {
						        "tax": "7",
						        "charge": "11"
						    }
						}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		JSONArray printerArray = jsonObject.getJSONArray("printers");
 
	            		if(printerArray.length() > 0) {
	            			JSONObject printer = printerArray.getJSONObject(0);
	            			etPrinterIP.setText(printer.getString("ip"));
	            			etPort.setText(printer.getString("port"));
	            		} else {
	            			etPrinterIP.setText("");
	            			etPort.setText("");
	            		}
	            		
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
	
	private void updatePrinter()
	{
		String printerIP = etPrinterIP.getText().toString();
		String printerPort = etPort.getText().toString();
		
		if(printerIP.trim().length() == 0 ||
				printerPort.trim().length() == 0)
		{
			MessageBox.OK(getActivity(), "Alert", "Please input IP & Port");
			return;
		}

		ivUpdate.setVisibility(View.INVISIBLE);

		//. http://www.roomallocator.com/restaurant/resetprinter4demo.php?hotel_id=6759&ip=192.168.1.87&port=9100
		//. http://www.roomallocator.com/restaurant/resetiprestu.php?hotel_id=6759&ip=192.168.1.87
		//. http://www.roomallocator.com/restaurant/resetipdep.php?dep_id=1&ip=192.168.1.87
		String url = "http://www.roomallocator.com/restaurant/resetiprestu.php";
		if (NetworkUtils.haveInternet(getActivity())) {
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			 
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("ip", printerIP);
			
			pbPrinter.setVisibility(View.VISIBLE);
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbPrinter.setVisibility(View.INVISIBLE);
					super.onFinish();
				}

				@Override
				public void onProgress(int bytesWritten, int totalSize) {
					
					pbPrinter.setProgress(bytesWritten / totalSize * 100);
					super.onProgress(bytesWritten, totalSize);
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
		            	 * {"operation":"sucsess"}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String msg = jsonObject.getString("operation");
            			MessageBox.OK(getActivity(), "Alert", msg);
            			if(msg != null && msg.equalsIgnoreCase("sucsess"))
            				ivUpdate.setVisibility(View.VISIBLE);
	            		
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
	
	private void checkPrinter()
	{
		String printerIP = etPrinterIP.getText().toString();
		String printerPort = etPort.getText().toString();
		
		if(printerIP.trim().length() == 0 ||
				printerPort.trim().length() == 0)
		{
			MessageBox.OK(getActivity(), "Alert", "Please input IP & Port");
			return;
		}

		ivCheck.setVisibility(View.INVISIBLE);
		new CheckPrinterTask().execute(printerIP, printerPort);
	}
	
	private class CheckPrinterTask extends
			AsyncTask<String, Integer, Boolean> {
		private String Error = null;
		protected void onPreExecute() {
			pbPrinter.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(String... args) {
			try {

				String ip = args[0];
				String port = args[1];
				boolean r = WifiPrintDriver.WIFISocket(
						ip,
						Integer.parseInt(port));
				if (!r) {
					Error = "Connecting to printer failed.";
				} else {
					if (WifiPrintDriver.IsNoConnection()) {

						Error = "Connecting to printer failed...";
					} else {
						ivCheck.setVisibility(View.VISIBLE);
						Error = "Connecting to printer success.";
					}
				}
				
				WifiPrintDriver.Close();

				return true;
				
			} catch (Exception e) {
				e.printStackTrace();
				Error = e.getMessage();
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			pbPrinter.setProgress(progress[0]);
		}

		protected void onPostExecute(Boolean result) {

			pbPrinter.setVisibility(View.INVISIBLE);
			MessageBox.OK(getActivity(), "Alert", Error);
		}
	}
}
