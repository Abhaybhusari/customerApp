package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appybite.customer.FavListAdapter.CallbackItemEvent;
import com.appybite.customer.info.DepartInfo;
import com.appybite.customer.info.ItemInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.PRJFUNC;

public class FavouritesFragment extends Fragment implements CallbackItemEvent{

	private ListView lvItemList;
	private FavListAdapter m_adtItemList;
	private ArrayList<DepartInfo> aryDepartInfoList = new ArrayList<DepartInfo>();
	private ProgressBar pbItem;

	public FavouritesFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_subcategory, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		loadFavourites();
		
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
		
		m_adtItemList = new FavListAdapter(
				getActivity(), this,
				R.layout.item_fav, 
				new ArrayList<ItemInfo>()
				);
		lvItemList = (ListView)v.findViewById(R.id.lvCategoryList);
		// lvCategoryList.setSelector(new ColorDrawable(Color.TRANSPARENT));
		lvItemList.setCacheColorHint(Color.TRANSPARENT);
		lvItemList.setDividerHeight(0);
		lvItemList.setAdapter(m_adtItemList);
		lvItemList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				DepartInfo departInfo = (DepartInfo)aryDepartInfoList.get(position);
				ItemInfo itemInfo = (ItemInfo)m_adtItemList.getItem(position);
				
				((MainActivity)getActivity()).goItemDetails(departInfo, itemInfo);
			}
		});

		pbItem = (ProgressBar)v.findViewById(R.id.pbCategory);
		pbItem.setVisibility(View.INVISIBLE);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
	}
	
	public void loadFavourites() {
		
		m_adtItemList.clear();
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/Get_CFavList.php?hotel_id=6759&cid=9
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("cid", cid);
			
			pbItem.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/Get_CFavList.php", params, new AsyncHttpResponseHandler() {
				
				@Override
				public void onFinish() {
					
					pbItem.setVisibility(View.INVISIBLE);
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
						    "status": "true",
						    "data": [
						        {
						            "depart": "restaurant",
						            "depart_id": "0",
						            "id": "56",
						            "title": "steak test",
						            "desc": "555555",
						            "price": "55",
						            "currency": "",
						            "thumb": "http://www.roomallocator.com/restaurant/my_deals_thumbnail/small_487077screenshot975jpg"
						        }
						    ]
						}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		JSONArray data = jsonObject.getJSONArray("data");
	            		
	            		for (int i = 0; i < data.length(); i++) {
							
	            			JSONObject object = data.getJSONObject(i);
	            			if(object.isNull("id"))
	            				continue;
	            			
	            			ItemInfo item = new ItemInfo();
	            			
	            			item.id = object.getString("id");
	            			item.title = object.getString("title");
	            			item.thumb = object.getString("thumb");
	            			item.price = object.getString("price");
	            			item.desc = object.getString("desc");
	            			
	            			m_adtItemList.add(item);
	            			
	            			DepartInfo departInfo = new DepartInfo();
	            			departInfo.id = object.getInt("depart_id");
	            			departInfo.title = object.getString("depart");
	            			aryDepartInfoList.add(departInfo);
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

	@Override
	public void onItemClick(int position) {
		
		DepartInfo departInfo = (DepartInfo)aryDepartInfoList.get(position);
		ItemInfo itemInfo = (ItemInfo)m_adtItemList.getItem(position);
		
		((MainActivity)getActivity()).goItemDetails(departInfo, itemInfo);
	}

	@Override
	public void onDeleteClick(final int position) {
		
		MessageBox.YesNo(getActivity(), "Remove Favourite Item", "Are you sure", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				
				DepartInfo departInfo = (DepartInfo)aryDepartInfoList.get(position);
				ItemInfo itemInfo = (ItemInfo)m_adtItemList.getItem(position);

				updateFavouriteItem(false, departInfo, itemInfo);
			}
		});
	}
	
	private void updateFavouriteItem(final boolean add, final DepartInfo departInfo, final ItemInfo itemInfo) {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/insert_Cfav.php?order_type=regular&depart=restaurant&item_id=15&hotel_id=6759&title=asdas&cid=10&depart_id=1
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("depart", departInfo.title);
			params.add("depart_id", String.valueOf(departInfo.id));
			params.add("order_type", "regular");
			params.add("item_id", itemInfo.id);
			params.add("title", itemInfo.title);
			params.add("cid", cid);
			if(!add)
				params.add("status", "delete");
			
			pbItem.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/insert_Cfav.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbItem.setVisibility(View.INVISIBLE);
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
	            		String status = jsonObject.getString("status");
	            		if(status.equalsIgnoreCase("true")) {
	            			
	            			String msg = jsonObject.getString("message"); 
	            			MessageBox.OK(getActivity(), "Alert", msg);
	            			
	            			aryDepartInfoList.remove(departInfo);
	            			m_adtItemList.remove(itemInfo);
	            			m_adtItemList.notifyDataSetChanged();
	            			
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
