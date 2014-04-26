package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.info.CategoryInfo;
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

public class ItemListFragment_Tab extends Fragment {

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private DepartInfo departInfo;
	private CategoryInfo subCategoryInfo;
	
	private ArrayList<ItemInfo> aryCategoryList = new ArrayList<ItemInfo>();
	private LinearLayout llCategoryList;
	private ProgressBar pbCategory;
	
	public ItemListFragment_Tab()
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
		
		boolean isDemo = PrefValue.getBoolean(getActivity(), R.string.pref_app_demo);
		if(isDemo)
			loadDemoItemList();
		else
			loadCategoryList();
		
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
	
	public void setDepartInfo(DepartInfo departInfo, CategoryInfo categoryInfo) {
		
		this.departInfo = departInfo;
		this.subCategoryInfo = categoryInfo;
	}
	
	public void loadCategoryList() {
		
		aryCategoryList.clear();
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. Restaurant :  https://www.appyorder.com/pro_version/webservice_smart_app/new/GetDiningMenu.php?hotel_id=6759&s_cat=977
			//. Department : https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetItems.php?hotel_id=6759&dept_id=1&s_cat=93
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String url = "new/GetDiningMenu.php";
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("s_cat", subCategoryInfo.id);
			
			if(departInfo.isRestaurant != true) {
				url = "Department/GetItems.php";
				params.add("dept_id", String.valueOf(departInfo.id));
			}
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.get(url, params, new AsyncHttpResponseHandler() {
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
						            "id": "2524",
						            "thumb": "http://roomallocator.com/appybiteRestaurant/predefineItems/item_thumbnail/small_801274",
						            "title": "Clean Room",
						            "price": "0.00",
						            "desc": "We are happy to clean your room within the hours of 6am till 5pm afternoon please send your request and our maid will be with you shortly"
						            "rate": "2.3"
						        }
						    ]
						}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");
	            		if(status.equalsIgnoreCase("true")) {
	            			
		            		JSONArray data = jsonObject.getJSONArray("data");
		            		
		            		for (int i = 0; i < data.length(); i++) {
								
		            			ItemInfo item = new ItemInfo();
		            			
		            			JSONObject object = data.getJSONObject(i);
		            			item.id = object.getString("id");
		            			item.title = object.getString("title");
		            			item.thumb = object.getString("thumb");
		            			item.price = object.getString("price");
		            			item.desc = object.getString("desc");
		            			item.rate = object.getDouble("rate");
		            			item.video = object.getString("youtupe");

		            			aryCategoryList.add(item);
		            		}
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
	
	private void updateCategoryList()
	{
		//. Department List
		llCategoryList.removeAllViews();
		
		for (int i = 0; i < aryCategoryList.size(); i++) {

			final ItemInfo value = aryCategoryList.get(i);

			LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View item = vi.inflate(R.layout.item_item_tab, llCategoryList, false);
			
			RelativeLayout rlParent = (RelativeLayout)item.findViewById(R.id.rlParent);
			
			ImageView ivThumb = (ImageView)item.findViewById(R.id.ivThumb);
			ImageLoader.getInstance().displayImage(value.thumb, ivThumb, options, animateFirstListener);
			
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setText(value.title);

			TextView tvDesc = (TextView)item.findViewById(R.id.tvDesc);
			tvDesc.setText(value.desc);

			item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

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
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
			}
			llCategoryList.addView(item);
		}
	}
	
	public void updateItem(ItemInfo itemInfo) {
		for (int i = 0; i < aryCategoryList.size(); i++) {
			
			if(aryCategoryList.get(i).id == itemInfo.id)
				aryCategoryList.get(i).rate = itemInfo.rate;
		}		
	}
	
	public void loadDemoItemList() {
		
		aryCategoryList.clear();
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. Restaurant :  http://www.roomallocator.com/appcreator/services/getresturantitem.php?hotel_id=18&rest_id=8
			//. Department :  http://www.roomallocator.com/appcreator/services/getdepartmentitem.php?hotel_id=18&dep_id=68
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String url;
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			if(departInfo.isRestaurant) {
				url = "http://www.roomallocator.com/appcreator/services/getresturantitem.php";
				params.add("rest_id", String.valueOf(departInfo.id));
			} else {
				url = "http://www.roomallocator.com/appcreator/services/getdepartmentitem.php";
				params.add("dep_id", String.valueOf(departInfo.id));
			}
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.get(url, params, new AsyncHttpResponseHandler() {
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
									"id": "22",
						            "name": "mohamed",
						            "image": "http://www.roomallocator.com/appcreator/uploads/46.jpg"						        
						        }
						    ]
						}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");
	            		if(status.equalsIgnoreCase("true")) {
	            			
		            		JSONArray data = jsonObject.getJSONArray("data");
		            		
		            		for (int i = 0; i < data.length(); i++) {
								
		            			ItemInfo item = new ItemInfo();
		            			
		            			JSONObject object = data.getJSONObject(i);
		            			item.id = object.getString("id");
		            			item.title = object.getString("name");
		            			item.thumb = object.getString("image");
		            					
		            			aryCategoryList.add(item);
		            		}
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
