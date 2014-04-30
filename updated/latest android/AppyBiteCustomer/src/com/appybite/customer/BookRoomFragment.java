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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.info.ItemInfo;
import com.appybite.customer.info.RoomInfo;
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

public class BookRoomFragment extends Fragment {

	private ItemInfo itemInfo;
	
	private ImageView ivImage;
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	//. Check Table
	private ScrollView svCheckTable;
	private Spinner spRoomType;
	private Button btDateFrom, btDateTo, btCheckTable;
	
	//. Book Table
	private ScrollView svBookTable;
	private LinearLayout llAvailTimes;
	private Button btBookTable, btBack;
	private TextView tvDate;
	private ProgressBar pbLoading;
	
    private Animation animShow;
    private Animation animHide;

    //. Checkout
    private ScrollView svCheckout;
	private Button btCheckout, btCheckoutBack;
	private EditText etCard_Num, etCard_ccv;
	private Spinner spCard_month, spCard_EXyear;
	private TextView tvCheckout;
    
	public BookRoomFragment()
	{
	}
	
	public void setItemInfo(ItemInfo itemInfo) {
		this.itemInfo = itemInfo;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_bookroom_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_bookroom, container, false);

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
		
		spRoomType = (Spinner)v.findViewById(R.id.spRoomType);

        if(itemInfo == null) {
			ArrayAdapter<CharSequence> adspin = ArrayAdapter.createFromResource(getActivity(), R.array.selected,    android.R.layout.simple_spinner_item);
	        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spRoomType.setAdapter(adspin);
		} else {
			ArrayAdapter<CharSequence> adspin = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
			adspin.add(itemInfo.title);
	        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spRoomType.setAdapter(adspin);
	        spRoomType.setEnabled(false);
			
			options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.bg_default_room)
				.showImageForEmptyUri(R.drawable.bg_default_room)
				.showImageOnFail(R.drawable.bg_default_room)
				.cacheInMemory(false)
				.cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
			ImageLoader.getInstance().displayImage(itemInfo.thumb, ivImage, options, animateFirstListener);
		}
		
		btDateFrom = (Button)v.findViewById(R.id.btDateFrom);
		btDateTo = (Button)v.findViewById(R.id.btDateTo);
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

		btDateTo.setText(msg);
		btDateTo.setTag(tag);
		btDateTo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();
				int cyear = c.get(Calendar.YEAR);
				int cmonth = c.get(Calendar.MONTH);
				int cday = c.get(Calendar.DAY_OF_MONTH);

				new DatePickerDialog(getActivity(), dateToSetListener, cyear, cmonth, cday).show();
			}
		});

		btCheckTable = (Button)v.findViewById(R.id.btCheckTable);
		btCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				llAvailTimes.removeAllViews();
				selectedRoom = null;
				aryRoomList.clear();
				
				tvDate.setText(btDateFrom.getText().toString() + "~" + btDateTo.getText().toString());
				
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
			
				if(selectedRoom == null) {
					MessageBox.OK(getActivity(), "Alert", "Please select time");
					return;
				}

				String msg = String.format("%s~%s\nRoom No: %s\nRoom Type: %s\n\nAre you sure?", 
						btDateFrom.getText().toString(),
						btDateTo.getText().toString(),
						selectedRoom.room_no,
						(String)spRoomType.getSelectedItem()
						);
						
				
				MessageBox.YesNo(getActivity(), "Alert", msg, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
						svCheckout.setVisibility(View.VISIBLE);
					}
				});
			}
		});
	
		pbLoading = (ProgressBar)v.findViewById(R.id.pbLoading);
		pbLoading.setVisibility(View.INVISIBLE);
		
		//. Checkout
		svCheckout = (ScrollView)v.findViewById(R.id.svCheckout);
		tvCheckout = (TextView)v.findViewById(R.id.tvCheckout);
		btCheckoutBack = (Button)v.findViewById(R.id.btCheckoutBack);
		btCheckoutBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				svCheckout.setVisibility(View.GONE);;
			}
		});
		btCheckout = (Button)v.findViewById(R.id.btCheckout);
		btCheckout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				if(etCard_Num.getText().toString().trim().length() == 0) {
					MessageBox.OK(getActivity(), "Alert", "Please Input Card Number.");
					return;
				} else if(etCard_ccv.getText().toString().trim().length() == 0) {
					MessageBox.OK(getActivity(), "Alert", "Please Input ccv");
					return;
				} else if(spCard_month.getSelectedItemPosition() == -1) {
					MessageBox.OK(getActivity(), "Alert", "Please Select Month");
					return;
				} else if(spCard_EXyear.getSelectedItemPosition() == -1) {
					MessageBox.OK(getActivity(), "Alert", "Please Select Expire Year");
					return;
				}
					
				checkoutRoom();
			}
		});
		
		etCard_Num = (EditText)v.findViewById(R.id.etCard_Num);
		etCard_ccv = (EditText)v.findViewById(R.id.etCard_ccv);
		
		Calendar ca = Calendar.getInstance();
		int cayear = c.get(Calendar.YEAR);
		int camonth = c.get(Calendar.MONTH);

		spCard_month = (Spinner)v.findViewById(R.id.spCard_month);
		ArrayAdapter<CharSequence> aaMonth = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
		for (int i = 1; i < 13; i++) {
			aaMonth.add(String.valueOf(i));
		}
		aaMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCard_month.setAdapter(aaMonth);

		spCard_EXyear = (Spinner)v.findViewById(R.id.spCard_EXyear);
		spCard_EXyear.setSelection(-1);
		ArrayAdapter<CharSequence> aaExYear = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
		for (int i = cayear; i < cayear + 100; i++) {
			aaExYear.add(String.valueOf(i));
		}
		aaExYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCard_EXyear.setAdapter(aaExYear);
		
		svCheckTable.setVisibility(View.VISIBLE);
		svBookTable.setVisibility(View.GONE);
		svCheckout.setVisibility(View.GONE);
		initAnim();
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}

		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.rlItemInfo), LayoutLib.LP_RelativeLayout);

		TextView tvRoomType = (TextView)v.findViewById(R.id.tvRoomType);
		PRJFUNC.mGrp.relayoutView(tvRoomType, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvRoomType);
		
		TextView tvDateLabel = (TextView)v.findViewById(R.id.tvDateLabel);
		PRJFUNC.mGrp.relayoutView(tvDateLabel, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDateLabel);

		TextView tvDateFrom = (TextView)v.findViewById(R.id.tvDateFrom);
		PRJFUNC.mGrp.relayoutView(tvDateFrom, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDateFrom);

		PRJFUNC.mGrp.relayoutView(btDateFrom, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btDateFrom);

		TextView tvDateTo = (TextView)v.findViewById(R.id.tvDateTo);
		PRJFUNC.mGrp.relayoutView(tvDateTo, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDateTo);

		PRJFUNC.mGrp.relayoutView(btDateTo, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btDateTo);

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
		
		//. checkout
		PRJFUNC.mGrp.relayoutView(btCheckoutBack, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setButtonFontScale(btCheckoutBack);

		PRJFUNC.mGrp.relayoutView(btCheckout, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btCheckout);

		PRJFUNC.mGrp.relayoutView(tvCheckout, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCheckout);

		TextView tvCard_Num = (TextView)v.findViewById(R.id.tvCard_Num);
		PRJFUNC.mGrp.relayoutView(tvCard_Num, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCard_Num);

		PRJFUNC.mGrp.relayoutView(etCard_Num, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setEditTextFontScale(etCard_Num);

		TextView tvCard_ccv = (TextView)v.findViewById(R.id.tvCard_ccv);
		PRJFUNC.mGrp.relayoutView(tvCard_ccv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCard_ccv);

		PRJFUNC.mGrp.relayoutView(etCard_ccv, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setEditTextFontScale(etCard_ccv);

		TextView tvCard_month = (TextView)v.findViewById(R.id.tvCard_month);
		PRJFUNC.mGrp.relayoutView(tvCard_month, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCard_month);

		PRJFUNC.mGrp.relayoutView(spCard_month, LayoutLib.LP_RelativeLayout);

		TextView tvCard_EXyear = (TextView)v.findViewById(R.id.tvCard_EXyear);
		PRJFUNC.mGrp.relayoutView(tvCard_EXyear, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCard_EXyear);

		PRJFUNC.mGrp.relayoutView(spCard_EXyear, LayoutLib.LP_RelativeLayout);
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
	
	private DatePickerDialog.OnDateSetListener dateToSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String msg = DateFormat.format("MMM dd, yyyy", c.getTimeInMillis()).toString();
			String tag = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
			btDateTo.setText(msg);
			btDateTo.setTag(tag);
		}
	};
	
	RoomInfo selectedRoom;
			
	ArrayList<RoomInfo> aryRoomList = new ArrayList<RoomInfo>();
	private void checkAvailTime() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. http://www.appyorder.com/pro_version/webservice_smart_app/reserve.php?id=6759&from=2013-12-01&to=2014-02-28
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("id", hotel_id);
			params.add("from", (String)btDateFrom.getTag());
			params.add("to", (String)btDateTo.getTag());
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("reserve.php", params, new AsyncHttpResponseHandler() {
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
						 [
						    {
						        "room_id": "1",
						        "room_no": "101",
						        "room_name": "royale",
						        "no_of_beds": "2",
						        "type": "Apartment",
						        "price": "2"
						    }
    					 ]
						 */
						
						JSONArray jsonArray = new JSONArray(responseBody);
						for (int i = 0; i < jsonArray.length(); i++) {
							
							RoomInfo roomInfo = new RoomInfo();
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							
							roomInfo.room_id = jsonObject.getInt("room_id");
							roomInfo.room_no = jsonObject.getString("room_no");
							roomInfo.room_name = jsonObject.getString("room_name");
							roomInfo.no_of_beds = jsonObject.getString("no_of_beds");
							roomInfo.type = jsonObject.getString("type");
							roomInfo.price = jsonObject.getString("price");

							if(roomInfo.no_of_beds == null || roomInfo.no_of_beds.trim().length() == 0)
								continue;
							
							int no_of_beds = 0;
							try {
							
								no_of_beds = Integer.parseInt(roomInfo.no_of_beds.trim());
								
							} catch (Exception e2) {
								
							}
							
							if(no_of_beds == 0)
								continue;
								
							if((spRoomType.getSelectedItemId() == 0 && no_of_beds == 1) ||
								(spRoomType.getSelectedItemId() == 1 && no_of_beds == 2) ||
								(spRoomType.getSelectedItemId() == 2 && no_of_beds > 2))
								aryRoomList.add(roomInfo);
						}
						
						createUI(aryRoomList, llAvailTimes);

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
	
	private void createUI(ArrayList<RoomInfo> time_list, LinearLayout layout) {
		
		LinearLayout.LayoutParams btn_dimension_param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1f);
		btn_dimension_param.rightMargin=5;
		LinearLayout row_layout = null;
		final Button[] btn_array = new Button[time_list.size()];
		for (int i = 0; i < btn_array.length; i++) {
			RoomInfo roomInfo =  time_list.get(i);
			
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
				btn_array[i].setText(roomInfo.room_no/* + "(" + roomInfo.room_name + ")"*/);
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
				btn_array[i].setTag(roomInfo);
				row_layout.addView(btn_array[i]);
			} else {
				
				btn_array[i] = new Button(getActivity());
				btn_array[i].setId(i + 1);
				btn_array[i].setLayoutParams(btn_dimension_param);
				btn_array[i].setTextColor(Color.parseColor("#016d72"));
				btn_array[i].setGravity(Gravity.CENTER);
				btn_array[i].setText(roomInfo.room_no/* + "(" + roomInfo.room_name + ")"*/);
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
							selectedRoom=(RoomInfo)btn_array[i].getTag();
						} else {
							
							btn_array[i].setTextColor(Color.parseColor("#016d72"));
							btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
						}
					}
				}
			});
		}
	}
	
	public void checkoutRoom() {

		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://appyorder.com/pro_version/webservice_smart_app/paypalCheckout.php
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String email = PrefValue.getString(getActivity(), R.string.pref_customer_email_id);
			String first_name = PrefValue.getString(getActivity(), R.string.pref_customer_name);
			String last_name = PrefValue.getString(getActivity(), R.string.pref_customer_name);
			String sku = hotel_id + "-" + String.valueOf(selectedRoom.room_id);
			/*
			 	visa:'visa',
				num:'4111111111111111',
				month:'12',
				EXyear:'2014',
				ccv:'123',
				fname:'wwww',
				lname:'dddd',
				price:2000.00,
				sku:'6759-20',
				bk:1,
				email:'a@b.ccc',
				from:'2013-1-1',
				to:'2014-1-1',
				rid:1
			 * */
			RequestParams params = new RequestParams();
			params.add("visa", "visa");
			params.add("num", etCard_Num.getText().toString());
			params.add("month", (String)spCard_month.getSelectedItem());
			params.add("EXyear", (String)spCard_EXyear.getSelectedItem());
			params.add("ccv", etCard_ccv.getText().toString());
			params.add("fname", first_name);
			params.add("lname", last_name);
			params.add("price", selectedRoom.price);
			params.add("sku", sku);
			params.add("bk", "1");
			params.add("email", email);
			params.add("from", (String)btDateFrom.getTag());
			params.add("to", (String)btDateTo.getTag());
			params.add("rid", String.valueOf(selectedRoom.room_id));
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("paypalCheckout.php", params, new AsyncHttpResponseHandler() {
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

	            		String result = new String(response);
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		MessageBox.OK(getActivity(), "Alert", jsonObject.getString("status"));
	            		
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
