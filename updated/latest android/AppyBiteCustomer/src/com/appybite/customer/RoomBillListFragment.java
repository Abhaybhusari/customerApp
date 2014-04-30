package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class RoomBillListFragment extends Fragment {

	//. DB
	LocalOrderListDatabase foodDB;
	ExtrasDatabase extraDB;
	ModifierDatabase modifierDB;

	private TextView tvTotalPriceValue, tvTotalPriceLabel;
	private ListView lvCartList;
	private CartListAdapter m_adtCartList;
	private Button btCheckout;
	
	public RoomBillListFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_roombill_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_roombill, container, false);

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
		tvTotalPriceLabel = (TextView)v.findViewById(R.id.tvTotalPriceLabel);
		// tvTotalPriceLabel.setText("Room Bill (" + PrefValue.getString(getActivity(), R.string.pref_order_code) + ")");
		
		m_adtCartList = new CartListAdapter(
				getActivity(), null,
				R.layout.item_cart, 
				new ArrayList<ReceiptInfo>()
				);
		lvCartList = (ListView)v.findViewById(R.id.lvCartList);
		lvCartList.setSelector(new ColorDrawable(Color.TRANSPARENT));
		lvCartList.setCacheColorHint(Color.TRANSPARENT);
		lvCartList.setDividerHeight(0);
		lvCartList.setAdapter(m_adtCartList);
		
		btCheckout = (Button)v.findViewById(R.id.btCheckout);
		btCheckout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(m_adtCartList.getCount() == 0) {
					
					MessageBox.OK(getActivity(), "Alert", "You have no orders to checkout.");
					return;
				}
				
				checkoutRoomBill();
			}
		});
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.viRoomBill), LayoutLib.LP_RelativeLayout);
		
		RelativeLayout rlCart = (RelativeLayout)v.findViewById(R.id.rlCart);
		PRJFUNC.mGrp.relayoutView(rlCart, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.relayoutView(tvTotalPriceValue, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvTotalPriceValue);
		
		PRJFUNC.mGrp.relayoutView(tvTotalPriceLabel, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvTotalPriceLabel);
		
		ImageView iv1 = (ImageView)v.findViewById(R.id.iv1);
		PRJFUNC.mGrp.relayoutView(iv1, LayoutLib.LP_RelativeLayout);
		
		ImageView iv2 = (ImageView)v.findViewById(R.id.iv2);
		PRJFUNC.mGrp.relayoutView(iv2, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setButtonFontScale(btCheckout);
	}
	
	public void loadCartList() {
		
		m_adtCartList.clear();
		
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		String currency = PrefValue.getString(getActivity(), R.string.pref_currency);
		float fPrice = 0f;
		
		ArrayList<ReceiptInfo> foodList = foodDB.getFoodItemList(hotel_id, c_id, LocalOrderListDatabase.status_open); 
		
		for (int i = 0; i < foodList.size(); i++) {
			
			ReceiptInfo food = foodList.get(i);
			m_adtCartList.add(food);
			fPrice += Float.parseFloat(food.price) * food.qnt;
			
			ArrayList<ReceiptInfo> extraList = extraDB.getExtraListByProductId(hotel_id, c_id, foodList.get(i).id, LocalOrderListDatabase.status_open);
			for (int j = 0; j < extraList.size(); j++) {
				
				ReceiptInfo extra = extraList.get(j);
				if(extra.qnt > 0) {
					m_adtCartList.add(extra);
					fPrice += Float.parseFloat(extra.price) * extra.qnt;
				}
			}
			
			ArrayList<ReceiptInfo> modifierList = modifierDB.getModifierListByProductId(hotel_id, c_id, foodList.get(i).id, LocalOrderListDatabase.status_open);
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
	
	private JSONArray getOrderJson() throws JSONException
	{
		/*
		 [
		    {
		        "hotel_id": "6759",
		        "room_id": "2",
		        "user_id": "3",
		        "order_id": "456",
		        "item_id": "23",
		        "dep_id": "4",
		        "dep_name": "test",
		        "item_name": "test",
		        "price": "45"
		    }
		]
		 * */

		//. orders
		JSONArray orders = new JSONArray();
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		
		ArrayList<ReceiptInfo> foodList = foodDB.getFoodItemList(hotel_id, cid, LocalOrderListDatabase.status_open); 
		
		for (int i = 0; i < foodList.size(); i++) {
			
			ReceiptInfo food = foodList.get(i);
			float fPrice = Float.parseFloat(food.price) * food.qnt;
			
			JSONObject order = new JSONObject();
			order.put("hotel_id", hotel_id);
			order.put("room_id", "2");
			order.put("user_id", cid);
			order.put("order_id", "456");
			order.put("item_id", food.id);
			order.put("dep_id", food.depart_id);
			order.put("dep_name", food.depart_name);
			order.put("item_name", food.title);
			order.put("price", String.valueOf(fPrice));

			orders.put(order);
		}
		
		return orders;
	}
	
	private void checkoutRoomBill() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/inset_in_final_bill.php?data=
			//. data=[{"hotel_id":"6759","room_id":"2","user_id":"3","order_id":"456","item_id":"23","dep_id":"4","dep_name":"test","item_name":"test","price":"45"}]
			
			RequestParams params = new RequestParams();
			
			try {
				params.add("data", getOrderJson().toString());
			} catch (JSONException e1) {
				
				Toast.makeText(getActivity(), "Invalid Order", Toast.LENGTH_LONG).show();
				e1.printStackTrace();
				return;
			}
			
			DialogUtils.launchProgress(getActivity(), "Please wait...");
			CustomerHttpClient.get("new/inset_in_final_bill.php", params, new AsyncHttpResponseHandler() {
				
				boolean success = false;
				
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					if(success) {
						
						String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
						String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
						String pay_type = LocalOrderListDatabase.pay_type_charge_room;
						String status = LocalOrderListDatabase.status_closed;
						
						foodDB.updatePayTypeByHotel(hotel_id, c_id, pay_type, status);
						extraDB.updatePayTypeByHotel(hotel_id, c_id, pay_type, status);
						modifierDB.updatePayTypeByHotel(hotel_id, c_id, pay_type, status);
						
						loadCartList();
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
	           				
	           				MessageBox.OK(getActivity(), "Alert", "Success!");
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
}
