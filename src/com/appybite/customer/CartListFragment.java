package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RT_Printer.WIFI.WifiPrintDriver;
import com.appybite.customer.CartListAdapter.CallbackItemEvent;
import com.appybite.customer.db.ExtrasDatabase;
import com.appybite.customer.db.LocalOrderListDatabase;
import com.appybite.customer.db.ModifierDatabase;
import com.appybite.customer.info.ReceiptInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class CartListFragment extends Fragment implements CallbackItemEvent {

	//. DB
	LocalOrderListDatabase foodDB;
	ExtrasDatabase extraDB;
	ModifierDatabase modifierDB;

	private TextView tvTotalPriceValue;
	private ListView lvCartList;
	private CartListAdapter m_adtCartList;
	private Button btAddMore, btSendOrder;
	private String pay_type = LocalOrderListDatabase.pay_type_cash, status = LocalOrderListDatabase.status_closed;
	
	public CartListFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_cart_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_cart, container, false);

		foodDB = new LocalOrderListDatabase(getActivity());
		extraDB = new ExtrasDatabase(getActivity());
		modifierDB = new ModifierDatabase(getActivity());

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		loadCartList();
		
		return v;
	}
	
	@Override
	public void onDestroy() {
		
		CustomerHttpClient.stop();
		
		foodDB.close();
		extraDB.close();
		modifierDB.close();

		super.onDestroy();
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		tvTotalPriceValue = (TextView)v.findViewById(R.id.tvTotalPriceValue);
		
		m_adtCartList = new CartListAdapter(
				getActivity(), this,
				R.layout.item_cart, 
				new ArrayList<ReceiptInfo>()
				);
		lvCartList = (ListView)v.findViewById(R.id.lvCartList);
		lvCartList.setSelector(new ColorDrawable(Color.TRANSPARENT));
		lvCartList.setCacheColorHint(Color.TRANSPARENT);
		lvCartList.setDividerHeight(0);
		lvCartList.setAdapter(m_adtCartList);
		
		btAddMore = (Button)v.findViewById(R.id.btAddMore);
		btAddMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).onBackPressed();
			}
		});
		
		btSendOrder = (Button)v.findViewById(R.id.btSendOrder);
		btSendOrder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(m_adtCartList.getCount() == 0) {
					
					MessageBox.OK(getActivity(), "Alert", "You have no orders to place.");
					return;
				}
				
				String msg = String.format("User: %s\nOrder Place: %s %s\nTotal Price: %s", 
						PrefValue.getString(getActivity(), R.string.pref_customer_name),
						PrefValue.getString(getActivity(), R.string.pref_order_type),
						PrefValue.getString(getActivity(), R.string.pref_order_code),
						tvTotalPriceValue.getText().toString()
						);
				
				final Dialog dialog = new Dialog(getActivity());
				dialog.setTitle("Place Order");
				dialog.setContentView(R.layout.dialog_placeorder);
				
				TextView tvMsg = (TextView)dialog.findViewById(R.id.tvMsg);
				tvMsg.setText(msg);
				
				final RadioButton rbCash = (RadioButton)dialog.findViewById(R.id.rbCash);
				RadioButton rbChargeRoom = (RadioButton)dialog.findViewById(R.id.rbChargeRoom);
				
				if (PrefValue.getBoolean(getActivity(), R.string.pref_app_demo)) {
					rbChargeRoom.setVisibility(View.GONE);
				} else {
					rbChargeRoom.setVisibility(View.VISIBLE);
				}
				
				Button btOk = (Button)dialog.findViewById(R.id.btOk);
				btOk.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						
						dialog.dismiss();
						
						if(locationManager == null)
							locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
						
						if (!locationManager
								.isProviderEnabled(LocationManager.GPS_PROVIDER))
							buildAlertMessageNoGps();
						else {
							Location locationGps = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							
							Location locationNet = locationManager
									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

							
							String longitude = "", latitude = "";
							if(locationGps != null)
							{
								longitude = String.valueOf(locationGps.getLongitude());
								latitude = String.valueOf(locationGps.getLatitude());
							}
							else if(locationNet != null)
							{
								longitude = String.valueOf(locationNet.getLongitude());
								latitude = String.valueOf(locationNet.getLatitude());
							}
							
							if(rbCash.isChecked()) {
								pay_type = LocalOrderListDatabase.pay_type_cash;
								status = LocalOrderListDatabase.status_closed;
							} else {
								pay_type = LocalOrderListDatabase.pay_type_charge_room;
								status = LocalOrderListDatabase.status_open;
							}
							
							//. Test
							longitude = "98.2962";
							latitude = "7.88091";

							if (PrefValue.getBoolean(getActivity(), R.string.pref_app_demo))
								inserNewOrderForDemo();
							else
								inserNewOrder(longitude, latitude);
						}
					}
				});
				Button btCancel = (Button)dialog.findViewById(R.id.btCancel);
				btCancel.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		RelativeLayout rlCart = (RelativeLayout)v.findViewById(R.id.rlCart);
		PRJFUNC.mGrp.relayoutView(rlCart, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.relayoutView(tvTotalPriceValue, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvTotalPriceValue);
		
		TextView tvTotalPriceLabel = (TextView)v.findViewById(R.id.tvTotalPriceLabel);
		PRJFUNC.mGrp.relayoutView(tvTotalPriceLabel, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvTotalPriceLabel);
		
		ImageView iv1 = (ImageView)v.findViewById(R.id.iv1);
		PRJFUNC.mGrp.relayoutView(iv1, LayoutLib.LP_RelativeLayout);
		
		ImageView iv2 = (ImageView)v.findViewById(R.id.iv2);
		PRJFUNC.mGrp.relayoutView(iv2, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setButtonFontScale(btAddMore);
		PRJFUNC.mGrp.setButtonFontScale(btSendOrder);
	}
	
	public void loadCartList() {
		
		m_adtCartList.clear();
		
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		String currency = PrefValue.getString(getActivity(), R.string.pref_currency);
		float fPrice = 0f;
		
		ArrayList<ReceiptInfo> foodList = foodDB.getFoodItemList(hotel_id, c_id, LocalOrderListDatabase.status_pending); 
		
		for (int i = 0; i < foodList.size(); i++) {
			
			ReceiptInfo food = foodList.get(i);
			m_adtCartList.add(food);
			fPrice += Float.parseFloat(food.price) * food.qnt;
			
			ArrayList<ReceiptInfo> extraList = extraDB.getExtraListByProductId(hotel_id, c_id, foodList.get(i).id, LocalOrderListDatabase.status_pending);
			for (int j = 0; j < extraList.size(); j++) {
				
				ReceiptInfo extra = extraList.get(j);
				if(extra.qnt > 0) {
					m_adtCartList.add(extra);
					fPrice += Float.parseFloat(extra.price) * extra.qnt;
				}
			}
			
			ArrayList<ReceiptInfo> modifierList = modifierDB.getModifierListByProductId(hotel_id, c_id, foodList.get(i).id, LocalOrderListDatabase.status_pending);
			for (int j = 0; j < modifierList.size(); j++) {
				
				ReceiptInfo modifier = modifierList.get(j);
				if(modifier.qnt > 0) {
					m_adtCartList.add(modifier);
					fPrice += Float.parseFloat(modifier.price) * modifier.qnt;
				}
			}
		}
		
		tvTotalPriceValue.setText(currency + String.format(" %.2f", fPrice));
		
		if(m_adtCartList.getCount() > 0)
			lvCartList.setSelection(m_adtCartList.getCount() - 1);
	}

	@Override
	public void onDeleteClick(int position) {
		// TODO Auto-generated method stub
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		
		ReceiptInfo info = m_adtCartList.getItem(position);
		if(info.type == 0) {
			
			if(info.qnt > 1)
				foodDB.updateQntByProductId(hotel_id, c_id, info.depart_id, info.id, --info.qnt);
			else {
				foodDB.deleteItem(hotel_id, c_id, info.depart_id, info.id, LocalOrderListDatabase.status_pending);
				extraDB.deleteItem(hotel_id, c_id, info.id, LocalOrderListDatabase.status_pending);
				modifierDB.deleteItem(hotel_id, c_id, info.id, LocalOrderListDatabase.status_pending);
			}
		} else if(info.type == 1) {
			if(info.qnt > 1)
				extraDB.updateQntByProductId(hotel_id, c_id, info.product_id, info.id, --info.qnt);
			else {
				extraDB.deleteItem(hotel_id, c_id, info.product_id, info.id);
			}
		} else if(info.type == 2) {
			if(info.qnt > 1)
				modifierDB.updateQntByProductId(hotel_id, c_id, info.product_id, info.id, --info.qnt);
			else {
				modifierDB.deleteItem(hotel_id, c_id, info.product_id, info.id);
			}
		}
		
		loadCartList();		
	}
	
	private void inserNewOrder(String longitude, String latitude)
	{
		//. https://www.appyorder.com/pro_version/webservice_smart_app//new/insert_new_order.php?hotel_id=6759
		if (NetworkUtils.haveInternet(getActivity())) {
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("long", longitude);
			params.add("lat", latitude);
			
			try {
				params.add("data", getOrderJson().toString());
			} catch (JSONException e1) {
				
				Toast.makeText(getActivity(), "Invalid Order", Toast.LENGTH_LONG).show();
				e1.printStackTrace();
				return;
			}
			
			DialogUtils.launchProgress(getActivity(), "Please wait while send orders.");
			CustomerHttpClient.post("new/insert_new_order.php", params, new AsyncHttpResponseHandler() {
				
				boolean success = false;
				
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					if(success) {
						String msg = String.format("User: %s\nOrder Place: %s %s\nTotal Price: %s", 
								PrefValue.getString(getActivity(), R.string.pref_customer_name),
								PrefValue.getString(getActivity(), R.string.pref_order_type),
								PrefValue.getString(getActivity(), R.string.pref_order_code),
								tvTotalPriceValue.getText().toString()
								);
						replyToCustomer("New Order", msg);
					}
						
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
		            	 ({"status":"true","message":"Order Set Successfully"})
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");

            			if(status.equalsIgnoreCase("true")) {
            				
            				Toast.makeText(getActivity(), "Successfully Ordered",Toast.LENGTH_LONG).show();
            				// MessageBox.OK(getActivity(), "Alert", "Successfully Ordered");
	            			success = true;
            			} else {
            				String msg = jsonObject.getString("message");
            				MessageBox.OK(getActivity(), "Alert", msg);
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

	private void inserNewOrderForDemo()
	{
		//. http://www.roomallocator.com/appcreator/services/insertorder.php?
		//. hotel_id=18&item_id=22&item_name=mohamed&cust_id=1&qt=5
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			RequestParams params = new RequestParams();
			try {
				params.add("data", getDemoOrderJson().toString());
			} catch (JSONException e1) {
				
				Toast.makeText(getActivity(), "Invalid Order", Toast.LENGTH_LONG).show();
				e1.printStackTrace();
				return;
			}
			
			//data={"hotel_id":126,"email":"miofuture@yahoo.com","customer_id":20,"roomNo":"A204",
			//"order":[{"itemid":25,"qty":7},{"itemid":13,"qty":3},{"itemid":10,"qty":3}]}
			String url = "http://www.roomallocator.com/appcreator/services/insertorder.php";
			DialogUtils.launchProgress(getActivity(), "Please wait while send orders.");
			/*CustomerHttpClient.post("services/insertorder.php", params, new AsyncHttpResponseHandler() {*/
			/*CustomerHttpClient.get("services/insertorder.php", params, new AsyncHttpResponseHandler() {*/
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				
				boolean success = false;
				
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					if(success) {
						String msg = String.format("User: %s\nOrder Place: %s %s\nTotal Price: %s", 
								PrefValue.getString(getActivity(), R.string.pref_customer_name),
								PrefValue.getString(getActivity(), R.string.pref_order_type),
								PrefValue.getString(getActivity(), R.string.pref_order_code),
								tvTotalPriceValue.getText().toString()
								);
						//replyToCustomer("New Order", msg);
					}
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
		            	 {"status":"true","message":"New Order Added"}	
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");

            			if(status.equalsIgnoreCase("success")) {
            				
            				String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
        					String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
        					
            				LocalOrderListDatabase foodDB = new LocalOrderListDatabase(getActivity());
            				ArrayList<ReceiptInfo> foodList = foodDB.getFoodItemList(hotel_id, c_id, LocalOrderListDatabase.status_pending); 
            				if (PrefValue.getBoolean(getActivity(), R.string.pref_app_demo)) {
            					for (int i=0; i<foodList.size(); i++) {
            						ReceiptInfo food = foodList.get(i);
            						foodDB.updateFoodItem(food.id, LocalOrderListDatabase.status_closed);
            					}
            				}
            				
            				Toast.makeText(getActivity(), "Successfully Ordered",Toast.LENGTH_LONG).show();
            				// MessageBox.OK(getActivity(), "Alert", "Successfully Ordered");
	            			success = true;
	            			((MainActivity)getActivity()).onBackPressed();
            			} else {
            				String msg = jsonObject.getString("message");
            				MessageBox.OK(getActivity(), "Alert", msg);
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
	
	private JSONObject getOrderJson() throws JSONException
	{
		JSONObject object = new JSONObject();

		//. customer
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		String order_type = PrefValue.getString(getActivity(), R.string.pref_order_type);
		String order_code = PrefValue.getString(getActivity(), R.string.pref_order_code);
		
		JSONObject id = new JSONObject();
		id.put("id", c_id);
		object.put("customer", id);
		
		//. details
		JSONObject details = new JSONObject();
		details.put("order_type", order_type);
		details.put("global", order_type);
		details.put("table_no", order_code);
		details.put("room_no", order_code);
		details.put("chair_no", order_code);
		
		object.put("details", details);
		
		//. orders
		JSONArray orders = new JSONArray();
		
		ArrayList<ReceiptInfo> foodList = foodDB.getFoodItemList(hotel_id, c_id, LocalOrderListDatabase.status_pending); 
		if (PrefValue.getBoolean(getActivity(), R.string.pref_app_demo)) {
			ReceiptInfo food = foodList.get(0);
			float fPrice = Float.parseFloat(food.price) * food.qnt;
			
			JSONObject order = new JSONObject();
			order.put("department", food.depart_name);
			order.put("item_id", food.id);
			order.put("title", food.title);
			order.put("price", food.price);
			order.put("qnt", String.valueOf(food.qnt));
			order.put("total_amount", String.valueOf(fPrice));
			
			ArrayList<ReceiptInfo> extraList = extraDB.getExtraListByProductId(hotel_id, c_id, foodList.get(0).id, LocalOrderListDatabase.status_pending);
			JSONArray extraArray = null;
			for (int j = 0; j < extraList.size(); j++) {
				
				ReceiptInfo extra = extraList.get(j);
				fPrice += Float.parseFloat(extra.price) * extra.qnt;
				
				JSONObject extraJson = new JSONObject();
				extraJson.put("id", extra.id);
				extraJson.put("title", extra.title);
				extraJson.put("price", extra.price);
				extraJson.put("qnt", String.valueOf(extra.qnt));
				
				if(extraArray == null)
					extraArray = new JSONArray();
				extraArray.put(extraJson);
			}
			order.put("extra", extraArray);
			
			ArrayList<ReceiptInfo> modifierList = modifierDB.getModifierListByProductId(hotel_id, c_id, foodList.get(0).id, LocalOrderListDatabase.status_pending);
			JSONArray modifierArray = null;
			
			for (int j = 0; j < modifierList.size(); j++) {
				
				ReceiptInfo modifier = modifierList.get(j);
				
				JSONObject modifierJson = new JSONObject();
				modifierJson.put("id", modifier.id);
				modifierJson.put("title", modifier.title);
				modifierJson.put("price", modifier.price);
				modifierJson.put("qnt", String.valueOf(modifier.qnt));
				
				if(modifierArray == null)
					modifierArray = new JSONArray();

				modifierArray.put(modifierJson);
			}
			order.put("modifier", modifierArray);
			
			orders.put(order);
		} else {
			for (int i = 0; i < foodList.size(); i++) {
				
				ReceiptInfo food = foodList.get(i);
				float fPrice = Float.parseFloat(food.price) * food.qnt;
				
				JSONObject order = new JSONObject();
				order.put("department", food.depart_name);
				order.put("item_id", food.id);
				order.put("title", food.title);
				order.put("price", food.price);
				order.put("qnt", String.valueOf(food.qnt));
				order.put("total_amount", String.valueOf(fPrice));
				
				ArrayList<ReceiptInfo> extraList = extraDB.getExtraListByProductId(hotel_id, c_id, foodList.get(i).id, LocalOrderListDatabase.status_pending);
				JSONArray extraArray = null;
				for (int j = 0; j < extraList.size(); j++) {
					
					ReceiptInfo extra = extraList.get(j);
					fPrice += Float.parseFloat(extra.price) * extra.qnt;
					
					JSONObject extraJson = new JSONObject();
					extraJson.put("id", extra.id);
					extraJson.put("title", extra.title);
					extraJson.put("price", extra.price);
					extraJson.put("qnt", String.valueOf(extra.qnt));
					
					if(extraArray == null)
						extraArray = new JSONArray();
					extraArray.put(extraJson);
				}
				order.put("extra", extraArray);
				
				ArrayList<ReceiptInfo> modifierList = modifierDB.getModifierListByProductId(hotel_id, c_id, foodList.get(i).id, LocalOrderListDatabase.status_pending);
				JSONArray modifierArray = null;
				
				for (int j = 0; j < modifierList.size(); j++) {
					
					ReceiptInfo modifier = modifierList.get(j);
					
					JSONObject modifierJson = new JSONObject();
					modifierJson.put("id", modifier.id);
					modifierJson.put("title", modifier.title);
					modifierJson.put("price", modifier.price);
					modifierJson.put("qnt", String.valueOf(modifier.qnt));
					
					if(modifierArray == null)
						modifierArray = new JSONArray();
	
					modifierArray.put(modifierJson);
				}
				order.put("modifier", modifierArray);
				
				orders.put(order);
			}
		}
		
		object.put("order", orders);
		
		return object;
	}
	
	private JSONObject getDemoOrderJson() throws JSONException
	{
		JSONObject object = new JSONObject();

		//. customer
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		String c_email_id = PrefValue.getString(getActivity(), R.string.pref_customer_email_id);
		String order_type = PrefValue.getString(getActivity(), R.string.pref_order_type);
		String order_code = PrefValue.getString(getActivity(), R.string.pref_order_code);

		object.put("hotel_id", hotel_id);
		object.put("email", c_email_id);
		object.put("customer_id", c_id);
		object.put("roomNo", PrefValue.getString(getActivity(), R.string.pref_customer_room_no));
		
		//. orders
		JSONArray orders = new JSONArray();
		LocalOrderListDatabase foodDB = new LocalOrderListDatabase(getActivity());
		ArrayList<ReceiptInfo> foodList = foodDB.getFoodItemList(hotel_id, c_id, LocalOrderListDatabase.status_pending); 
		if (PrefValue.getBoolean(getActivity(), R.string.pref_app_demo)) {
			for (int i=0; i<foodList.size(); i++) {
				ReceiptInfo food = foodList.get(i);
				JSONObject order = new JSONObject();
				order.put("itemid", food.id);
				order.put("qty", String.valueOf(food.qnt));
				orders.put(order);
			}
		}
		object.put("order", orders);
		
		return object;
	}
	
	private String getPrintContent(int tax, int service_charge)
	{
		String printContent = "Receipt";
		
		/*
		 * Receipt
		 * Hotel: Hilton Hotel
		 * roomNo/tableNo/barNo: 100
		 * Payment: cash/charge_room
		 * 
		 * ================================================
		 * Item			qnt		price
		 *    extra		qnt		price
		 *    modifier	qnt		price
		 * Item			qnt		price
		 *    extra		qnt		price
		 *    modifier	qnt		price
		 * ================================================
		 * 
		 * Sub Total: USD100
		 * Tax: 
		 * Service: 
		 * Total: 
		 */
		try {
			String currency = PrefValue.getString(getActivity(), R.string.pref_currency);
			String hotel_name = PrefValue.getString(getActivity(), R.string.pref_hotel_name);
			printContent += "\nHotel: " + hotel_name;
			
			String order_type = PrefValue.getString(getActivity(), R.string.pref_order_type) + "No: " + PrefValue.getString(getActivity(), R.string.pref_order_code);
			printContent += "\n" + order_type;
			printContent += "\n" + "Payment: " + pay_type;
			printContent += "\n================================================";
			String order_details = "";
			float totoal_price = 0f;
			for (int i = 0; i < m_adtCartList.getCount(); i++) {
				
				ReceiptInfo info = m_adtCartList.getItem(i);
				float price = Float.parseFloat(info.price);
				float total = price * info.qnt;
				totoal_price += total;
				
				if(info.type == 0) {
					order_details += String.format("\n%s: %d * %.2f = %.2f",
							info.title, 
							info.qnt, 
							price, 
							total
						);
					if(info.msg != null && info.msg.trim().length() > 0)
					{
						order_details += String.format("\n   ToStaff: %s", info.msg);
					}
				} else if(info.type == 1) {

					order_details += String.format("\n   %s: %d * %.2f = %.2f",
							info.title, 
							info.qnt, 
							price, 
							total
						);
				} else if(info.type == 2) {

					order_details += String.format("\n   %s: %d * %.2f = %.2f",
							info.title, 
							info.qnt, 
							price, 
							total
						);
				}
			}
			
			float tax_price = totoal_price * ((float)tax / 100);
			float service_price = totoal_price * ((float)service_charge / 100);
			printContent += "\n" + order_details;
			printContent += "\n================================================";
			printContent += "\nSubTotal: " + currency + String.format("%.2f", totoal_price);
			printContent += "\nTax: " + currency + String.format("%.2f", tax_price);
			printContent += "\nService: " + currency + String.format("%.2f", service_price);
			printContent += "\nTotal: " + currency + String.format("%.2f", totoal_price + tax_price + service_price);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.i("PrintContent", printContent);
		
		return printContent;
	}
	
	private void loadPrinter()
	{
		String url = "http://www.roomallocator.com/restaurant/printerservicerest.php";
		if (NetworkUtils.haveInternet(getActivity())) {
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			 
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			DialogUtils.launchProgress(getActivity(), "Finding Printer...");
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
	            		JSONObject vat = jsonObject.getJSONObject("vat");
	            		String tax = vat.getString("tax");
	            		String charge = vat.getString("charge");
	            		
	            		JSONArray printerArray = jsonObject.getJSONArray("printers");
 
	            		if(printerArray.length() > 0) {
	            			JSONObject printer = printerArray.getJSONObject(0);
	            			String ip = printer.getString("ip");
	            			String port = printer.getString("port");
	            			new SendToPrinterTask().execute(ip, port, tax, charge);
	            		} else {
	            			Toast.makeText(getActivity(), "No printer registered in server.",Toast.LENGTH_LONG).show();
	            			return;
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
	
	private class SendToPrinterTask extends AsyncTask<String, Integer, Boolean> {
		private String Error = null;
		private ProgressDialog Dialog = new ProgressDialog(getActivity());
		
		protected void onPreExecute() {
			Dialog.setCancelable(false);
			Dialog.setMessage("Printing...");
			Dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... args) {
			try {
				String ip = args[0];
				String port = args[1];
				String tax = args[2];
				String service_charge = args[3];
				// getPrintContent(Integer.parseInt(tax), Integer.parseInt(service_charge));
				boolean r = WifiPrintDriver.WIFISocket(ip,
						Integer.parseInt(port));
				if (!r) {
					Error = "Connecting to printer failed.";
				} else {
					if (WifiPrintDriver.IsNoConnection()) {

						Error = "Printer is not connnected";
					} else {
						
						WifiPrintDriver.Begin();
		    			WifiPrintDriver.ImportData(getPrintContent(Integer.parseInt(tax), Integer.parseInt(service_charge)));
		    			WifiPrintDriver.ImportData("\r");
		    			WifiPrintDriver.excute();
		    			WifiPrintDriver.ClearData();
		    			
						Error = "Successfully printed";
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

		protected void onPostExecute(Boolean result) {

			Dialog.dismiss();
			MessageBox.OK(getActivity(), "Alert", Error);
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			foodDB.updatePayTypeByHotel(hotel_id, c_id, pay_type, status);
			extraDB.updatePayTypeByHotel(hotel_id, c_id, pay_type, status);
			modifierDB.updatePayTypeByHotel(hotel_id, c_id, pay_type, status);
			
			loadCartList();
		}
	}
	
	LocationManager locationManager = null;
	private void buildAlertMessageNoGps() {
		
		if (Build.VERSION.SDK_INT >= 11)
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(
					"Your GPS seems to be disabled, do you want to enable it?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog,
										final int id) {
									startActivity(new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}
							})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int id) {
							dialog.cancel();
						}
					});
			final AlertDialog alert = builder.create();
			alert.show();
		}
		else
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(
					"Your GPS seems to be disabled, please enable and try again?")
					.setCancelable(false)
					.setPositiveButton("Close",
							new DialogInterface.OnClickListener() {
								public void onClick(final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			final AlertDialog alert = builder.create();
			alert.show();
		}
	}
	
	private void replyToCustomer(String tag, String msg)
	{
		// http://appyorder.com/pro_version/webservice_smart_app/gcm/sendToCashier.php?cid=304&hotel_id=6759&tag=ggggggg&message=kkkkkkkkkk
		if (NetworkUtils.haveInternet(getActivity())) {
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("cid", cid);
			params.add("tag", tag);
			params.add("message", msg);
			
			DialogUtils.launchProgress(getActivity(), "please wait while sending message to cashier.");
			CustomerHttpClient.get("gcm/sendToCashier.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					loadPrinter();

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

	            		String result = new String(response);
	            		Log.i("HTTP Response <<<", result);
	            		
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
