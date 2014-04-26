package com.appybite.customer;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.db.ExtrasDatabase;
import com.appybite.customer.db.LocalOrderListDatabase;
import com.appybite.customer.db.ModifierDatabase;
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
import com.yj.commonlib.util.DeviceUtil;

public class BookItemFragment extends Fragment {

	private DepartInfo departInfo;
	private ItemInfo itemInfo;
	
	private ImageView ivImage;
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	//. Check Table
	private ScrollView svCheckTable;
	private Button btDateFrom, btCheckTable;
	
	//. Book Table
	private ScrollView svBookTable;
	private LinearLayout llAvailTimes;
	private Button btBookTable, btBack;
	private TextView tvDate;
	private ProgressBar pbLoading;
	
    private Animation animShow;
    private Animation animHide;
    
    private String session;
    private String b_id;
    
	public BookItemFragment()
	{
	}
	
	public void setItemInfo(DepartInfo departInfo, ItemInfo itemInfo) {
		this.departInfo = departInfo;
		this.itemInfo = itemInfo;
		
		//. For Test
		this.itemInfo.id = "2378";
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_bookitem_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_bookitem, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
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

		ivImage = (ImageView)v.findViewById(R.id.ivHotelBg);
		
		//. Check Table
		svCheckTable = (ScrollView)v.findViewById(R.id.svCheckTable);

		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.bg_default_room)
		.showImageForEmptyUri(R.drawable.bg_default_room)
		.showImageOnFail(R.drawable.bg_default_room)
		.cacheInMemory(false)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		ImageLoader.getInstance().displayImage(itemInfo.thumb, ivImage, options, animateFirstListener);

		btDateFrom = (Button)v.findViewById(R.id.btDateFrom);
		Calendar c = Calendar.getInstance();
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH);
		int cday = c.get(Calendar.DAY_OF_MONTH);
		String msg = DateFormat.format("MMM dd, yyyy", c.getTimeInMillis()).toString();
		String tag = String.format("%d-%02d-%02d", cyear, cmonth+1, cday);
		btDateFrom.setText(msg);
		btDateFrom.setTag(tag);
		btDateFrom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();
				int cyear = c.get(Calendar.YEAR);
				int cmonth = c.get(Calendar.MONTH);
				int cday = c.get(Calendar.DAY_OF_MONTH);

				new DatePickerDialog(getActivity(), dateFromSetListener, cyear, cmonth, cday).show();
			}
		});
		
		btCheckTable = (Button)v.findViewById(R.id.btCheckTable);
		btCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				llAvailTimes.removeAllViews();
				selectedTime = null;
				aryRoomList.clear();
				
				tvDate.setText(btDateFrom.getText().toString());
				
				svBookTable.startAnimation(animShow);

				checkAvailTime();
			}
		});
		
		//. Book Table
		svBookTable = (ScrollView)v.findViewById(R.id.svBookTable);
		llAvailTimes = (LinearLayout)v.findViewById(R.id.llAvailTime);
		tvDate = (TextView)v.findViewById(R.id.tvDate);
		btBack = (Button)v.findViewById(R.id.btBack);
		btBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				svBookTable.startAnimation(animHide);
			}
		});
		btBookTable = (Button)v.findViewById(R.id.btBookTable);
		btBookTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				if(selectedTime == null) {
					MessageBox.OK(getActivity(), "Alert", "Please select time");
					return;
				}

				String msg = String.format("From: %s\nTime: %s\n\nAre you sure?", 
						btDateFrom.getText().toString(),
						selectedTime
						);
						
				
				MessageBox.YesNo(getActivity(), "Book Item", msg, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
						bookItem();
					}
				});
			}
		});
	
		pbLoading = (ProgressBar)v.findViewById(R.id.pbLoading);
		pbLoading.setVisibility(View.INVISIBLE);
		
		svCheckTable.setVisibility(View.VISIBLE);
		svBookTable.setVisibility(View.GONE);
		initAnim();
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}

		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.rlItemInfo), LayoutLib.LP_RelativeLayout);

		TextView tvDateFrom = (TextView)v.findViewById(R.id.tvDateFrom);
		PRJFUNC.mGrp.relayoutView(tvDateFrom, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDateFrom);

		PRJFUNC.mGrp.relayoutView(btCheckTable, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btCheckTable);

		PRJFUNC.mGrp.relayoutView(btBookTable, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btBookTable);

		PRJFUNC.mGrp.relayoutView(btBack, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setButtonFontScale(btBack);

		PRJFUNC.mGrp.relayoutView(tvDate, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDate);
		
		TextView tvAvailTime = (TextView)v.findViewById(R.id.tvAvailTime);
		PRJFUNC.mGrp.relayoutView(tvAvailTime, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvAvailTime);
	}
	
	private void initAnim()
	{
		animShow = AnimationUtils.loadAnimation(getActivity(), R.anim.right_in);
		animShow.setAnimationListener(new AnimationListener() {
				
			@Override
			public void onAnimationStart(Animation animation) {

				svBookTable.setVisibility(View.VISIBLE);
			}
			@Override
			public void onAnimationRepeat(Animation animation){}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
			}
		});
		
		animHide = AnimationUtils.loadAnimation(getActivity(), R.anim.right_out);
		animHide.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation){}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				svBookTable.setVisibility(View.GONE);
			}
		});
	}

	private DatePickerDialog.OnDateSetListener dateFromSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String msg = DateFormat.format("MMM dd, yyyy", c.getTimeInMillis()).toString();
			String tag = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
			btDateFrom.setText(msg);
			btDateFrom.setTag(tag);
		}
	};

	String selectedTime;
			
	ArrayList<String> aryRoomList = new ArrayList<String>();
	private void checkAvailTime() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. http://www.appyorder.com/pro_version/webservice_smart_app/ondayinfoitem.php?id=6759&d=1&i=2378&date=2014-01-02
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("id", hotel_id);
			params.add("d", String.valueOf(departInfo.id));
			params.add("i", itemInfo.id);
			params.add("date", (String)btDateFrom.getTag());
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("ondayinfoitem.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbLoading.setVisibility(View.INVISIBLE);
					
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

						String responseBody = new String(response);
						Log.i("HTTP Response <<<", responseBody);

						/*
						{
						    "status": true,
						    "info": [
						        {
						            "session": "2",
						            "b_id": "1",
						            "parent": [
						                {
						                    "id": "11",
						                    "parent_id": null,
						                    "sday": "2014-01-02",
						                    "b_id": "1",
						                    "ctimes": null,
						                    "createdat": "2014-01-04 06:59:14"
						                }
						            ],
						            "times": [
						                {
						                    "ctimes": "03:00:00",
						                    "id": "13"
						                }
						            ]
						        }
						    ],
						    "msg": "success"
						}
						 */
						
						JSONObject jsonObject = new JSONObject(responseBody);
						String status = jsonObject.getString("status");
						if(status.equalsIgnoreCase("true")) {
							
							JSONArray jsonArray = jsonObject.getJSONArray("info");
							for (int i = 0; i < jsonArray.length(); i++) {
								
								JSONObject object = jsonArray.getJSONObject(i);
								
								JSONArray parentArray = object.getJSONArray("times");
								for (int j = 0; j < parentArray.length(); j++) {
									
									JSONObject jsonTime = parentArray.getJSONObject(j);
									String ctime = jsonTime.getString("ctimes");
									aryRoomList.add(ctime);
								}
								
								session = object.getString("session");
								b_id = object.getString("b_id");
							}
							
							createUI(aryRoomList, llAvailTimes);
							
						} else {
							Toast.makeText(getActivity(), jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
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
	
	private void createUI(ArrayList<String> time_list, LinearLayout layout) {
		
		LinearLayout.LayoutParams btn_dimension_param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1f);
		btn_dimension_param.rightMargin=5;
		LinearLayout row_layout = null;
		final Button[] btn_array = new Button[time_list.size()];
		for (int i = 0; i < btn_array.length; i++) {
			String roomInfo =  time_list.get(i);
			
			if (i % 4 == 0) {
				row_layout = new LinearLayout(getActivity());
				row_layout.setWeightSum(4);
				LinearLayout.LayoutParams row_param = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT
						, LayoutParams.WRAP_CONTENT);
				row_param.setMargins(5, 3, 5, 3);
				       
				row_layout.setLayoutParams(row_param);
				layout.addView(row_layout);

				btn_array[i] = new Button(getActivity());
				btn_array[i].setId(i + 1);
				btn_array[i].setLayoutParams(btn_dimension_param);
				btn_array[i].setTextColor(Color.parseColor("#016d72"));
				btn_array[i].setGravity(Gravity.CENTER);
				btn_array[i].setText(roomInfo/* + "(" + roomInfo.room_name + ")"*/);
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
				btn_array[i].setTag(roomInfo);
				row_layout.addView(btn_array[i]);
			} else {
				
				btn_array[i] = new Button(getActivity());
				btn_array[i].setId(i + 1);
				btn_array[i].setLayoutParams(btn_dimension_param);
				btn_array[i].setTextColor(Color.parseColor("#016d72"));
				btn_array[i].setGravity(Gravity.CENTER);
				btn_array[i].setText(roomInfo/* + "(" + roomInfo.room_name + ")"*/);
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
				btn_array[i].setTag(roomInfo);
				row_layout.addView(btn_array[i]);
			}

			btn_array[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Button btn = (Button) v;
					for (int i = 0; i < btn_array.length; i++) {
						if (btn_array[i].getId() == btn.getId()) {
							
							btn_array[i].setTextColor(Color.parseColor("#b20e08"));
							btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.DimGray));
							selectedTime=(String)btn_array[i].getTag();
						} else {
							
							btn_array[i].setTextColor(Color.parseColor("#016d72"));
							btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
						}
					}
				}
			});
		}
	}
	
	private void bookItem() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. http://www.appyorder.com/pro_version/webservice_smart_app/addnewbookableitem.php?id=6759&uid=28&sid=4&sd=2014-06-27&iid=2378
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("id", hotel_id);
			params.add("uid", c_id);
			params.add("sid", session);
			params.add("sd", (String)btDateFrom.getTag());
			params.add("iid", itemInfo.id);
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("addnewbookableitem.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbLoading.setVisibility(View.INVISIBLE);
					
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

						String responseBody = new String(response);
						Log.i("HTTP Response <<<", responseBody);

						/*
						 * {"status":true,"msg":"success"}
						 */
						JSONObject jsonObject = new JSONObject(responseBody);
						String status = jsonObject.getString("status");
						if(status.equalsIgnoreCase("true")) {
							MessageBox.OK(getActivity(), "Booking Success", "Booking succeeded.\nThis item will go to your Room Bill.");
							addToRoomBill();
							((MainActivity)getActivity()).onBackPressed();
						} else {
							Toast.makeText(getActivity(), jsonObject.getString("msg"),Toast.LENGTH_LONG).show();	
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
	
	private void addToRoomBill() {

		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
 
		LocalOrderListDatabase foodDB = new LocalOrderListDatabase(getActivity());
		ExtrasDatabase extraDB = new ExtrasDatabase(getActivity());
		ModifierDatabase modifierDB = new ModifierDatabase(getActivity());

		foodDB.insert(hotel_id, c_id, itemInfo.id, itemInfo.title, itemInfo.price, itemInfo.qnt, "", String.valueOf(departInfo.id), departInfo.title, "regular");
		
		foodDB.updatePayTypeByHotel(hotel_id, c_id, LocalOrderListDatabase.pay_type_charge_room, LocalOrderListDatabase.status_open);
		extraDB.updatePayTypeByHotel(hotel_id, c_id, LocalOrderListDatabase.pay_type_charge_room, LocalOrderListDatabase.status_open);
		modifierDB.updatePayTypeByHotel(hotel_id, c_id, LocalOrderListDatabase.pay_type_charge_room, LocalOrderListDatabase.status_open);

		foodDB.close();
		extraDB.close();
		modifierDB.close();
	}
}
