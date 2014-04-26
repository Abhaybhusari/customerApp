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

public class SubCategoryFragment_Tab extends Fragment {

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private DepartInfo departInfo;
	private CategoryInfo categoryInfo;
	
	private ArrayList<CategoryInfo> aryCategoryList = new ArrayList<CategoryInfo>();
	private LinearLayout llCategoryList;
	private ProgressBar pbCategory;
	
	public SubCategoryFragment_Tab()
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
		this.categoryInfo = categoryInfo;
	}
	
	public void loadCategoryList() {
		
		aryCategoryList.clear();
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. Restaurant :  https://www.appyorder.com/pro_version/webservice_smart_app/new/GetSubCategory.php?hotel_id=6759&m_cat=1063
			//. Department : https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetSubCategory.php?dept_id=68&hotel_id=6759&m_cat=1063
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String url = "new/GetSubCategory.php";
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("m_cat", categoryInfo.id);
			
			if(departInfo.id > 0) {
				url = "Department/GetSubCategory.php";
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
						            "id": "1065",
						            "thumb": "http://roomallocator.com/appybiteRestaurant/category_thumbnail/small_427507haircutwomanjpg",
						            "title": "Woman Haircut"
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
								
		            			CategoryInfo item = new CategoryInfo();
		            			
		            			JSONObject object = data.getJSONObject(i);
		            			item.id = object.getString("id");
		            			item.name = object.has("name") ? object.getString("name") : object.getString("title");
		            			item.thumb = object.getString("thumb");
		            			
		            			aryCategoryList.add(item);
		            		}
	            		} else {
	            			MessageBox.OK(getActivity(), "Alert", jsonObject.getString("message"));
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

			final CategoryInfo value = aryCategoryList.get(i);

			LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View item = vi.inflate(R.layout.item_item_tab, llCategoryList, false);
			
			RelativeLayout rlParent = (RelativeLayout)item.findViewById(R.id.rlParent);
			
			ImageView ivThumb = (ImageView)item.findViewById(R.id.ivThumb);
			ImageLoader.getInstance().displayImage(value.thumb, ivThumb, options, animateFirstListener);
			
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setText(value.name);

			TextView tvDesc = (TextView)item.findViewById(R.id.tvDesc);
			// tvDesc.setVisibility(View.GONE);
			tvDesc.setText("");

			item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

					CategoryInfo categoryInfo = value;
					((MainActivity)getActivity()).goItemList_Tab(departInfo, categoryInfo);
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
}
