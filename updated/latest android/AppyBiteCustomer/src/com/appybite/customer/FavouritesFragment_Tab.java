package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.info.DepartInfo;
import com.appybite.customer.info.ItemInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class FavouritesFragment_Tab extends Fragment {

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	private ArrayList<DepartInfo> aryDepartList = new ArrayList<DepartInfo>();
	private ArrayList<ItemInfo> aryItemList = new ArrayList<ItemInfo>();
	private LinearLayout llCategoryList;
	private ProgressBar pbCategory;
	
	public FavouritesFragment_Tab()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_depart_tab, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		loadFavourites();
		
		return v;
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_depart)
			.showImageForEmptyUri(R.drawable.bg_default_depart)
			.showImageOnFail(R.drawable.bg_default_depart)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		llCategoryList = (LinearLayout)v.findViewById(R.id.llCategoryList);
		
		pbCategory = (ProgressBar)v.findViewById(R.id.pbCategory);
		pbCategory.setVisibility(View.INVISIBLE);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
	}
	
	@Override
	public void onDestroy() {
		
		CustomerHttpClient.stop();
		
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();
		super.onDestroy();
	}
	
	public void loadFavourites() {
		
		aryDepartList.clear();
		aryItemList.clear();
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/Get_CFavList.php?hotel_id=6759&cid=9
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("cid", cid);
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/Get_CFavList.php", params, new AsyncHttpResponseHandler() {
				
				@Override
				public void onFinish() {
					
					pbCategory.setVisibility(View.INVISIBLE);
					updateCategoryList();
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
	            			
	            			aryItemList.add(item);
	            			
	            			DepartInfo departInfo = new DepartInfo();
	            			departInfo.id = object.getInt("depart_id");
	            			departInfo.title = object.getString("depart");
	            			aryDepartList.add(departInfo);
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
	
	private void updateCategoryList()
	{
		//. Department List
		llCategoryList.removeAllViews();
		
		for (int i = 0; i < aryItemList.size(); i++) {

			final ItemInfo value = aryItemList.get(i);
			final DepartInfo depart = aryDepartList.get(i);

			LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View item = vi.inflate(R.layout.item_item_tab, llCategoryList, false);
			
			RelativeLayout rlParent = (RelativeLayout)item.findViewById(R.id.rlParent);
			
			ImageView ivThumb = (ImageView)item.findViewById(R.id.ivThumb);
			ImageLoader.getInstance().displayImage(value.thumb, ivThumb, options, animateFirstListener);
			
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setText(value.title);

			TextView tvDesc = (TextView)item.findViewById(R.id.tvDesc);
			// tvDesc.setVisibility(View.GONE);
			tvDesc.setText(value.desc);

			Button btDelete = (Button)item.findViewById(R.id.btDelete);
			btDelete.setVisibility(View.VISIBLE);
			btDelete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					MessageBox.YesNo(getActivity(), "Remove Favourite Item", "Are you sure", new android.content.DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
							DepartInfo departInfo = depart;
							ItemInfo itemInfo = value;

							updateFavouriteItem(false, departInfo, itemInfo);
						}
					});
				}
			});
			
			item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

					DepartInfo departInfo = depart;
					ItemInfo itemInfo = value;
					
					((MainActivity)getActivity()).goItemDetails(departInfo, itemInfo);
				}
			});
			
			if (!PRJFUNC.DEFAULT_SCREEN) {
				
				PRJFUNC.mGrp.relayoutView(rlParent, LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
				PRJFUNC.mGrp.repaddingView(tvTitle);
				PRJFUNC.mGrp.setTextViewFontScale(tvDesc);
				PRJFUNC.mGrp.repaddingView(tvDesc);
				PRJFUNC.mGrp.relayoutView(btDelete, LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
			}
			llCategoryList.addView(item);
		}
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
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/insert_Cfav.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbCategory.setVisibility(View.INVISIBLE);
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
	            			
	            			aryDepartList.remove(departInfo);
	            			aryItemList.remove(itemInfo);
	            			updateCategoryList();
	            			
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
