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

import com.appybite.customer.info.AllDepartInfo;
import com.appybite.customer.info.CategoryInfo;
import com.appybite.customer.info.DepartInfo;
import com.appybite.customer.info.ItemInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.image.Utils;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.PRJFUNC;

public class ItemListFragment extends Fragment {

	private DepartInfo departInfo;
	private CategoryInfo subCategoryInfo;

	private ListView lvItemList;
	private ItemListAdapter m_adtItemList;
	private ProgressBar pbItem;
	private com.appybite.customer.HorizontalListView horizontalListView;
	private RelativeLayout rlSubcategory;
	private boolean loadFlag = false;

	public ItemListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		Log.e("OnCreate", "onCreate");

		View v = inflater.inflate(R.layout.frag_subcategory, container, false);
		rlSubcategory = (RelativeLayout) v.findViewById(R.id.rlSubcategory);
		lvItemList = (ListView) v.findViewById(R.id.lvCategoryList);
		horizontalListView = (HorizontalListView) v
				.findViewById(R.id.hlvCustomList);
		if (!loadFlag && NetworkUtils.haveInternet(getActivity())) {
			loadBackgroundImage();
			loadFlag = true;
		}

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}

		boolean isDemo = PrefValue.getBoolean(getActivity(),
				R.string.pref_app_demo);
		if (isDemo)
			loadDemoItemList();
		else
			loadCategoryList();

		return v;
	}

	private void loadBackgroundImage() {
		new AsyncTask<String, Void, String>() {
			Bitmap bitmap = null;

			@Override
			protected String doInBackground(String... params) {
				// TODO Auto-generated method stub
				try {
					bitmap = Utils.getBitmapFromURL(PrefValue.getString(
							getActivity(), R.string.pref_hotel_background_image));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

	private void updateLCD(View v) {

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {

			if (PRJFUNC.mGrp == null) {
				PRJFUNC.resetGraphValue(getActivity());
			}

			m_adtItemList = new ItemListAdapter(getActivity(),
					R.layout.item_item, new ArrayList<ItemInfo>());

			if (horizontalListView != null && lvItemList != null) {
				horizontalListView.setVisibility(View.GONE);
				lvItemList.setVisibility(View.VISIBLE);

			}
			// lvCategoryList.setSelector(new ColorDrawable(Color.TRANSPARENT));
			lvItemList.setCacheColorHint(Color.TRANSPARENT);
			lvItemList.setDividerHeight(0);
			lvItemList.setAdapter(m_adtItemList);
			lvItemList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {

					ItemInfo itemInfo = (ItemInfo) m_adtItemList
							.getItem(position);
					((MainActivity) getActivity()).goItemDetails(departInfo,
							itemInfo);
				}
			});

			pbItem = (ProgressBar) v.findViewById(R.id.pbCategory);
			pbItem.setVisibility(View.INVISIBLE);

		} else {

			// landscape mode

			if (lvItemList != null && horizontalListView != null) {
				lvItemList.setVisibility(View.GONE);
				horizontalListView.setVisibility(View.VISIBLE);
			}
			if (PRJFUNC.mGrp == null) {
				PRJFUNC.resetGraphValue(getActivity());
			}

			m_adtItemList = new ItemListAdapter(getActivity(),
					R.layout.land_item_item, new ArrayList<ItemInfo>());
			// horizontalListView = (com.appybite.customer.HorizontalListView) v
			// .findViewById(R.id.hlvCustomList);

			// lvCategoryList.setSelector(new ColorDrawable(Color.TRANSPARENT));
			// horizontalListView.setCacheColorHint(Color.TRANSPARENT);
			// horizontalListView.setDividerHeight(0);
			// lvItemList.setOverScrollMode(HorizontalListView.FOCUS_LEFT);
			horizontalListView.setAdapter(m_adtItemList);

			horizontalListView
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {

							ItemInfo itemInfo = (ItemInfo) m_adtItemList
									.getItem(position);
							((MainActivity) getActivity()).goItemDetails(
									departInfo, itemInfo);
						}
					});

			pbItem = (ProgressBar) v.findViewById(R.id.pbCategory);
			pbItem.setVisibility(View.INVISIBLE);
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
		this.subCategoryInfo = categoryInfo;
	}

	public void loadCategoryList() {

		m_adtItemList.clear();

		if (NetworkUtils.haveInternet(getActivity())) {

			// . Restaurant :
			// https://www.appyorder.com/pro_version/webservice_smart_app/new/GetDiningMenu.php?hotel_id=6759&s_cat=977
			// . Department :
			// https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetItems.php?dept_id=1&hotel_id=6759&s_cat=1001
			String hotel_id = PrefValue.getString(getActivity(),
					R.string.pref_hotel_id);
			String url = "new/GetDiningMenu.php";

			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("s_cat", subCategoryInfo.id);

			if (departInfo.isRestaurant != true) {
				url = "Department/GetItems.php";
				params.add("dept_id", String.valueOf(departInfo.id));
			}

			pbItem.setVisibility(View.VISIBLE);
			CustomerHttpClient.get(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {

					pbItem.setVisibility(View.INVISIBLE);
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
						 * { "status": "true", "data": [ { "id": "2524",
						 * "thumb":
						 * "http://roomallocator.com/appybiteRestaurant/predefineItems/item_thumbnail/small_801274"
						 * , "title": "Clean Room", "price": "0.00", "desc":
						 * "We are happy to clean your room within the hours of 6am till 5pm afternoon please send your request and our maid will be with you shortly"
						 * "rate": "2.3" } ] }
						 */

						String result = new String(response);
						result = result.replace("({", "{");
						result = result.replace("})", "}");
						Log.i("HTTP Response <<<", result);
						JSONObject jsonObject = new JSONObject(result);
						String status = jsonObject.getString("status");
						if (status.equalsIgnoreCase("true")) {

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

								m_adtItemList.add(item);
							}
						} else {

							String msg = jsonObject.getString("message");
							MessageBox.OK(getActivity(), "Alert", msg);
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

	public void updateItem(ItemInfo itemInfo) {

		for (int i = 0; i < m_adtItemList.getCount(); i++) {

			if (m_adtItemList.getItem(i).id == itemInfo.id)
				m_adtItemList.getItem(i).rate = itemInfo.rate;
		}
	}

	public void loadDemoItemList() {

		m_adtItemList.clear();

		if (departInfo.isRestaurant) {

			if (NetworkUtils.haveInternet(getActivity())) {

				// . Restaurant :
				// http://www.roomallocator.com/appcreator/services/getresturantitem.php?hotel_id=18&rest_id=8
				// . Department :
				// http://www.roomallocator.com/appcreator/services/getdepartmentitem.php?hotel_id=18&dep_id=68
				String hotel_id = PrefValue.getString(getActivity(),
						R.string.pref_hotel_id);
				String url;

				RequestParams params = new RequestParams();
				params.add("hotel_id", hotel_id);

				url = "http://www.roomallocator.com/appcreator/services/getresturantitem.php";
				params.add("rest_id", String.valueOf(departInfo.id));

				pbItem.setVisibility(View.VISIBLE);
				CustomerHttpClient.getFromFullService(url, params,
						new AsyncHttpResponseHandler() {
							@Override
							public void onFinish() {

								pbItem.setVisibility(View.INVISIBLE);
								super.onFinish();
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, byte[] errorResponse,
									Throwable e) {

								Toast.makeText(
										getActivity(),
										"Connection was lost (" + statusCode
												+ ")", Toast.LENGTH_LONG)
										.show();
								super.onFailure(statusCode, headers,
										errorResponse, e);
							}

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, byte[] response) {
								// Pull out the first event on the public
								// timeline
								try {
									/*
									 * { "status": "true", "data": [ { "id":
									 * "22", "name": "mohamed", "image":
									 * "http://www.roomallocator.com/appcreator/uploads/46.jpg"
									 * } ] }
									 */
									String result = new String(response);
									result = result.replace("({", "{");
									result = result.replace("})", "}");
									Log.i("HTTP Response <<<", result);
									JSONObject jsonObject = new JSONObject(
											result);
									String status = jsonObject
											.getString("status");
									if (status.equalsIgnoreCase("true")) {

										JSONArray data = jsonObject
												.getJSONArray("data");

										for (int i = 0; i < data.length(); i++) {

											ItemInfo item = new ItemInfo();

											JSONObject object = data
													.getJSONObject(i);
											item.id = object.getString("id");
											item.title = object
													.getString("name");
											item.thumb = object
													.getString("image");
											item.dep_id = String
													.valueOf(departInfo.id);
											item.dept_name = departInfo.title;
											item.price = object
													.getString("price");
											item.desc = object
													.getString("description");

											m_adtItemList.add(item);
										}
									} else {

										String msg = jsonObject
												.getString("message");
										MessageBox.OK(getActivity(), "Alert",
												msg);
									}

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Toast.makeText(getActivity(),
											"Invalid Data", Toast.LENGTH_LONG)
											.show();
								}
							}
						});
			} else {
				Toast.makeText(getActivity(), "No Internet Connection",
						Toast.LENGTH_LONG).show();
			}
		} else {
			ArrayList<AllDepartInfo> allItems = ((MainActivity) getActivity())
					.getAllDepartList();
			for (int i = 0; i < allItems.size(); i++) {
				ItemInfo item = new ItemInfo();

				AllDepartInfo object = allItems.get(i);

				if (departInfo.id == object.dep_id) {
					item.id = String.valueOf(object.id);
					item.title = object.name;
					item.thumb = object.image;
					item.dep_id = String.valueOf(object.dep_id);
					item.dept_name = object.dept_name;
					item.price = object.price;
					item.desc = object.desc;

					m_adtItemList.add(item);
				}
			}
		}
	}
}
