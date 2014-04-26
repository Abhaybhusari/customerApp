package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class DepartFragment extends Fragment {

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private DepartInfo departInfo;
	
	private RelativeLayout rlDepartInfo;
	private ImageView ivDepartBg;
	private TextView tvDepartDesc;
	private ListView lvCategoryList;
	private CategoryListAdapter m_adtCategoryList;
	private ProgressBar pbCategory;
	
	public DepartFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_depart, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		rlDepartInfo.setVisibility(View.VISIBLE);
		if(departInfo.id > 0)
			ImageLoader.getInstance().displayImage(departInfo.image, ivDepartBg, options, animateFirstListener);
		else
			ivDepartBg.setImageResource(R.drawable.bg_default_restaurant);
		tvDepartDesc.setText(departInfo.desc);

		loadCategoryList();
		
		return v;
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_category)
			.showImageForEmptyUri(R.drawable.bg_default_category)
			.showImageOnFail(R.drawable.bg_default_category)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		rlDepartInfo = (RelativeLayout)v.findViewById(R.id.rlDepartInfo);
		
		ivDepartBg = (ImageView)v.findViewById(R.id.ivDepartBg);
		tvDepartDesc = (TextView)v.findViewById(R.id.tvDepartDesc);
		
		m_adtCategoryList = new CategoryListAdapter(
				getActivity(), 
				R.layout.item_category, 
				new ArrayList<CategoryInfo>()
				);
		lvCategoryList = (ListView)v.findViewById(R.id.lvCategoryList);
		// lvCategoryList.setSelector(new ColorDrawable(Color.TRANSPARENT));
		lvCategoryList.setCacheColorHint(Color.TRANSPARENT);
		lvCategoryList.setDividerHeight(0);
		lvCategoryList.setAdapter(m_adtCategoryList);
		lvCategoryList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				CategoryInfo categoryInfo = (CategoryInfo)m_adtCategoryList.getItem(position);
				if(categoryInfo.hasSubCat == 0)
					((MainActivity)getActivity()).goItemList(departInfo, categoryInfo);
				else
					((MainActivity)getActivity()).goSubCategory(departInfo, categoryInfo);
			}
		});
		
		pbCategory = (ProgressBar)v.findViewById(R.id.pbCategory);
		pbCategory.setVisibility(View.INVISIBLE);
		
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		PRJFUNC.mGrp.relayoutView(rlDepartInfo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDepartDesc);
		PRJFUNC.mGrp.repaddingView(tvDepartDesc);
		
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
	}
	
	@Override
	public void onDestroy() {
		
		CustomerHttpClient.stop();
		
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();
		super.onDestroy();
	}
	
	public void setDepartInfo(DepartInfo departInfo) {
		this.departInfo = departInfo;
	}
	
	public void loadCategoryList() {
		
		m_adtCategoryList.clear();
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. Restaurant :  https://www.appyorder.com/pro_version/webservice_smart_app/new/GetMainCategory.php?hotel_id=6759
			//. Department : https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetMainCategory.php?hotel_id=6759&dept_id=1
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String url = "new/GetMainCategory.php";
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			if(departInfo.id > 0) {
				url = "Department/GetMainCategory.php";
				params.add("dept_id", String.valueOf(departInfo.id));
			}
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.get(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbCategory.setVisibility(View.INVISIBLE);
					super.onFinish();
				}

				@Override
				public void onProgress(int bytesWritten, int totalSize) {
					
					pbCategory.setProgress(bytesWritten / totalSize * 100);
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
						{
						    "status": "true",
						    "data": [
						        {
						            "id": "986",
						            "thumb": "http://roomallocator.com/appybiteRestaurant/category_thumbnail/small_455158room-service.jpg",
						            "title": "Roomservice",
						            "has_Sub_Category": "1"
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
							
	            			CategoryInfo item = new CategoryInfo();
	            			
	            			JSONObject object = data.getJSONObject(i);
	            			item.id = object.getString("id");
	            			item.name = object.has("name") ? object.getString("name") : object.getString("title");
	            			item.thumb = object.getString("thumb");
	            			item.hasSubCat = object.getInt("has_Sub_Category");
	            			
	            			m_adtCategoryList.add(item);
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
