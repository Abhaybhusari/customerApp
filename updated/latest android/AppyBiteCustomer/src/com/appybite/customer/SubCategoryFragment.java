package com.appybite.customer;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.appybite.customer.info.CategoryInfo;
import com.appybite.customer.info.DepartInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yj.commonlib.image.Utils;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.PRJFUNC;

public class SubCategoryFragment extends Fragment {

	private DepartInfo departInfo;
	private CategoryInfo categoryInfo;

	private ListView lvCategoryList;
	private CategoryListAdapter m_adtCategoryList;
	private ProgressBar pbCategory;
	private com.appybite.customer.HorizontalListView horizontalListView;
	private RelativeLayout rlSubcategory;

	public SubCategoryFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		View v = inflater.inflate(R.layout.frag_subcategory, container, false);
		lvCategoryList = (ListView) v.findViewById(R.id.lvCategoryList);
		rlSubcategory = (RelativeLayout) v.findViewById(R.id.rlSubcategory);
		horizontalListView = (HorizontalListView) v
				.findViewById(R.id.hlvCustomList);

		if (NetworkUtils.haveInternet(getActivity())) {
			loadBackgroundImage();
		}
		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}

		loadCategoryList();

		return v;
	}

	private void updateLCD(View v) {

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {

			if (PRJFUNC.mGrp == null) {
				PRJFUNC.resetGraphValue(getActivity());
			}
			if (horizontalListView != null && lvCategoryList != null) {
				horizontalListView.setVisibility(View.GONE);
				lvCategoryList.setVisibility(View.VISIBLE);

			}
			m_adtCategoryList = new CategoryListAdapter(getActivity(),
					R.layout.item_category, new ArrayList<CategoryInfo>());

			// lvCategoryList.setSelector(new ColorDrawable(Color.TRANSPARENT));
			lvCategoryList.setCacheColorHint(Color.TRANSPARENT);
			lvCategoryList.setDividerHeight(0);
			lvCategoryList.setAdapter(m_adtCategoryList);
			lvCategoryList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {

					CategoryInfo categoryInfo = (CategoryInfo) m_adtCategoryList
							.getItem(position);
					((MainActivity) getActivity()).goItemList(departInfo,
							categoryInfo);
				}
			});

			pbCategory = (ProgressBar) v.findViewById(R.id.pbCategory);
			pbCategory.setVisibility(View.INVISIBLE);
		} else {
			if (PRJFUNC.mGrp == null) {
				PRJFUNC.resetGraphValue(getActivity());
			}

			if (lvCategoryList != null && horizontalListView != null) {
				lvCategoryList.setVisibility(View.GONE);
				horizontalListView.setVisibility(View.VISIBLE);
			}
			m_adtCategoryList = new CategoryListAdapter(getActivity(),
					R.layout.land_item_category, new ArrayList<CategoryInfo>());

			// lvCategoryList.setSelector(new ColorDrawable(Color.TRANSPARENT));
			// lvCategoryList.setCacheColorHint(Color.TRANSPARENT);
			// lvCategoryList.setDividerHeight(0);
			horizontalListView.setAdapter(m_adtCategoryList);
			horizontalListView
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {

							CategoryInfo categoryInfo = (CategoryInfo) m_adtCategoryList
									.getItem(position);
							((MainActivity) getActivity()).goItemList(
									departInfo, categoryInfo);
						}
					});

			pbCategory = (ProgressBar) v.findViewById(R.id.pbCategory);
			pbCategory.setVisibility(View.INVISIBLE);
		}
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

		m_adtCategoryList.clear();

		if (NetworkUtils.haveInternet(getActivity())) {

			// . Restaurant :
			// https://www.appyorder.com/pro_version/webservice_smart_app/new/GetSubCategory.php?hotel_id=6759&m_cat=1063
			// . Department :
			// https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetSubCategory.php?dept_id=68&hotel_id=6759&m_cat=1063
			String hotel_id = PrefValue.getString(getActivity(),
					R.string.pref_hotel_id);
			String url = "new/GetSubCategory.php";

			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("m_cat", categoryInfo.id);

			if (departInfo.id > 0) {
				url = "Department/GetSubCategory.php";
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
				public void onFailure(int statusCode, Header[] headers,
						byte[] errorResponse, Throwable e) {

					Toast.makeText(getActivity(),
							"Connection was lost (" + statusCode + ")",
							Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}

				@Override
				public void onSuccess(int statusCode, Header[] headers,
						byte[] response) {
					// Pull out the first event on the public timeline
					try {

						/*
						 * { "status": "true", "data": [ { "id": "1065",
						 * "thumb":
						 * "http://roomallocator.com/appybiteRestaurant/category_thumbnail/small_427507haircutwomanjpg"
						 * , "title": "Woman Haircut" } ] }
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
							item.name = object.has("name") ? object
									.getString("name") : object
									.getString("title");
							item.thumb = object.getString("thumb");

							m_adtCategoryList.add(item);
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(getActivity(), "Invalid Data",
								Toast.LENGTH_LONG).show();
					}
				}
			});

		} else {
			Toast.makeText(getActivity(), "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void loadBackgroundImage() {

		new AsyncTask<String, Void, String>() {
			Bitmap bitmap = null;

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub

			//	bitmap = Utils.getBitmapFromURL(PrefValue.getString(
			//			getActivity(), R.string.pref_hotel_background_image));
				
				bitmap = Utils.getBitmapFromURL(PrefValue.getString(getActivity(), R.string.pref_item_bg));
				
				Log.e("**Bg Bitmap_subCategeory**",""+PrefValue.getString(getActivity(), R.string.pref_item_bg));
				return null;
			}

			protected void onPostExecute(String result) {

				try {
					if (bitmap != null) {
						rlSubcategory.setBackgroundDrawable(new BitmapDrawable(
								getResources(), bitmap));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				super.onPostExecute(result); 
			}
		}.execute();

	}
}
