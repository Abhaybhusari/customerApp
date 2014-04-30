package com.appybite.customer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.dialog.NumberPickerDlg;
import com.yj.commonlib.dialog.NumberPickerDlg.OnFinishedListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class BookTableFragment extends Fragment {

	//. Check Table
	private ScrollView svCheckTable;
	private Button btDate, btPersons, btCheckTable;
	
	//. Book Table
	private ScrollView svBookTable;
	private LinearLayout llAvailTimes, llAvailTables;
	private Button btBookTable, btBack;
	private TextView tvDate;
	private ProgressBar pbLoading;
	
    private Animation animShow;
    private Animation animHide;

	public BookTableFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = null;
		if(DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_booktable_tab, container, false);
		else
			v = inflater.inflate(R.layout.frag_booktable, container, false);

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

		//. Check Table
		svCheckTable = (ScrollView)v.findViewById(R.id.svCheckTable);
		btDate = (Button)v.findViewById(R.id.btDate);
		Calendar c = Calendar.getInstance();
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH);
		int cday = c.get(Calendar.DAY_OF_MONTH);
		String msg = DateFormat.format("MMM dd, yyyy", c.getTimeInMillis()).toString();
		String tag = String.format("%02d/%02d/%d", cday, cmonth+1, cyear);
		btDate.setText(msg);
		btDate.setTag(tag);
		btDate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();
				int cyear = c.get(Calendar.YEAR);
				int cmonth = c.get(Calendar.MONTH);
				int cday = c.get(Calendar.DAY_OF_MONTH);

				new DatePickerDialog(getActivity(), dateSetListener, cyear, cmonth, cday).show();
			}
		});

		btPersons = (Button)v.findViewById(R.id.btPersons);
		btPersons.setText("1");
		btPersons.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				NumberPickerDlg countDlg = new NumberPickerDlg(getActivity(), Integer.parseInt(btPersons.getText().toString()), new OnFinishedListener() {
					
					@Override
					public void onOk(int number) {
						
						btPersons.setText(String.valueOf(number));
					}
				});
				countDlg.show();
			}
		});
		
		btCheckTable = (Button)v.findViewById(R.id.btCheckTable);
		btCheckTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				llAvailTimes.removeAllViews();
				llAvailTables.removeAllViews();
				selected_table = "";
				selected_time = "";
				pick_in_time_status_list.clear();
				available_time_list.clear();
				available_status_list.clear();
				available_table_list.clear();
				table_seats_list.clear();

				svBookTable.startAnimation(animShow);
				tvDate.setText(btDate.getText().toString());
				
				checkAvailTime();
			}
		});
		
		//. Book Table
		svBookTable = (ScrollView)v.findViewById(R.id.svBookTable);
		llAvailTimes = (LinearLayout)v.findViewById(R.id.llAvailTime);
		llAvailTables = (LinearLayout)v.findViewById(R.id.llAvailTable);
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
			
				if(selected_time.length() == 0) {
					MessageBox.OK(getActivity(), "Alert", "Please select time");
					return;
				}

				if(selected_table.length() == 0) {
					MessageBox.OK(getActivity(), "Alert", "Please select table");
					return;
				}

				String msg = String.format("%s %s\nTable No: %s\nNumber of persons: %s\n\nAre you sure?", 
						btDate.getText().toString(),
						selected_time,
						selected_table,
						btPersons.getText().toString());
						
				
				MessageBox.YesNo(getActivity(), "Alert", msg, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
						bookTable();		
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
		
		TextView tvDateLabel = (TextView)v.findViewById(R.id.tvDateLabel);
		PRJFUNC.mGrp.relayoutView(tvDateLabel, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDateLabel);

		PRJFUNC.mGrp.relayoutView(btDate, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btDate);

		TextView tvPersonsLabel = (TextView)v.findViewById(R.id.tvPersonsLabel);
		PRJFUNC.mGrp.relayoutView(tvPersonsLabel, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvPersonsLabel);

		PRJFUNC.mGrp.relayoutView(btPersons, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btPersons);

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

		TextView tvAvailTable = (TextView)v.findViewById(R.id.tvAvailTable);
		PRJFUNC.mGrp.relayoutView(tvAvailTable, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvAvailTable);
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
	
	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			String msg = DateFormat.format("MMM dd, yyyy", c.getTimeInMillis()).toString();
			String tag = String.format("%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
			btDate.setText(msg);
			btDate.setTag(tag);
		}
	};
	
	ArrayList<String> pick_in_time_status_list = new ArrayList<String>();
	ArrayList<String> available_time_list = new ArrayList<String>();
	ArrayList<String> available_status_list = new ArrayList<String>();
	ArrayList<String> available_table_list = new ArrayList<String>();
	ArrayList<String> table_seats_list = new ArrayList<String>();
	String selected_time="",selected_table="";
			
	private void checkAvailTime() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/available_times.php?hotel_id=6759&date=20/10/2014&session_type=lunch
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("date", (String)btDate.getTag());
			params.add("session_type", "lunch");
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("available_times.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbLoading.setVisibility(View.INVISIBLE);
					
					checkAvailTable();
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

						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document doc = builder.parse(new InputSource(new StringReader(responseBody)));
						if (responseBody.contains("fail")) {
							MessageBox.OK(getActivity(), "Alert","Error Occur Try Again");
						} else {
							NodeList total_table = doc.getElementsByTagName("total_table");

							String total_tables = ((Element) total_table.item(0)).getTextContent();

							// available_table_list.add(total_tables);
							// Log.i(TAG, "total_tables: " +
							// total_tables);
							// if(total_tables != null &&
							// total_tables.trim().length() != 0)
							// table =Integer.parseInt(total_tables);

							NodeList node_pick_in_hours = doc.getElementsByTagName("peak_in_hours");
							String pick_in_hours = ((Element) node_pick_in_hours.item(0)).getTextContent();

							NodeList table_list = doc.getElementsByTagName("available_time");
							for (int i = 0; i < table_list.getLength(); i++) {
								NodeList time_node_list = doc.getElementsByTagName("time");
								String time = ((Element) time_node_list.item(i)).getTextContent();
								// Log.i(TAG, "time= " + time);
								available_time_list.add(time);
								if (pick_in_hours.equalsIgnoreCase(":-:")
										|| pick_in_hours.contains("none")) {
									pick_in_time_status_list.add("2");
								} else {
									String pick_in_start_time = pick_in_hours.substring(0, pick_in_hours.indexOf("-"));
									String pick_in_end_time = pick_in_hours.substring(pick_in_hours.indexOf("-") + 1);
									String pick_in_start_hour = pick_in_start_time.substring(0,pick_in_start_time.indexOf(":"));
									if (Integer.parseInt(pick_in_start_hour) > 12) {
										pick_in_start_hour = String.valueOf(Integer.parseInt(pick_in_start_hour) - 12);
									}
									String pick_in_end_hour = pick_in_end_time.substring(0,pick_in_end_time.indexOf(":"));
									if (Integer.parseInt(pick_in_end_hour) > 12) {
										pick_in_end_hour = String.valueOf(Integer.parseInt(pick_in_end_hour) - 12);
									}
									String pick_in_start_min = pick_in_start_time.substring(pick_in_start_time.indexOf(":") + 1);
									String pick_in_end_min = pick_in_end_time.substring(pick_in_end_time.indexOf(":") + 1);
									if (time.contains(":")) {
										// Log.i(TAG, "true");
										if (Integer.parseInt(time.substring(0, time.indexOf(":"))) >= Integer.parseInt(pick_in_start_hour)
												&& Integer.parseInt(time.substring(0,time.indexOf(":"))) < Integer.parseInt(pick_in_end_hour)) {
											pick_in_time_status_list.add("1");
										} else {
											pick_in_time_status_list.add("2");
										}
									} else {
										// Log.i(TAG, "false");
										if (Integer.parseInt(time) >= Integer.parseInt(pick_in_start_hour)
												&& Integer.parseInt(time) < Integer.parseInt(pick_in_end_hour)) {
											pick_in_time_status_list.add("1");
										} else {
											pick_in_time_status_list.add("2");
										}
									}
								}
								NodeList time_status_list = doc.getElementsByTagName("status");
								String time_status = ((Element) time_status_list.item(i)).getTextContent();
								available_status_list.add(time_status);
							}
							
							createUI(available_time_list, llAvailTimes);
						}

					} catch (Exception e) {
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
	
	private void checkAvailTable() {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/table_status.php?hotel_id=6759
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("table_status.php", params, new AsyncHttpResponseHandler() {
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
						
						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document doc = builder.parse(new InputSource(new StringReader(responseBody)));
						if (responseBody.contains("fail")) {
							
							MessageBox.OK(getActivity(), "Alert", "Error Occur Try Again");

						} else {
							NodeList table_list = doc.getElementsByTagName("table_list");

							int count = Integer.parseInt(btPersons.getText().toString());
							for (int i = 0; i < table_list.getLength(); i++) {
								
								NodeList table_no_list = doc.getElementsByTagName("table_no");
								String table_no = ((Element) table_no_list.item(i)).getTextContent();
								
								NodeList no_of_person_list = doc.getElementsByTagName("no_of_person");
								String no_of_person = ((Element) no_of_person_list.item(i)).getTextContent();
								
								int seat_count = Integer.parseInt(no_of_person);
								if(seat_count >= count)
								{
									available_table_list.add(table_no);
									table_seats_list.add(no_of_person);						
								}
							}
							
							createAvailTableUI(llAvailTables);
						}

					} catch (Exception e) {
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
				btn_array[i].setText(" "+time_list.get(i));
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
				row_layout.addView(btn_array[i]);
			} else {
				
				btn_array[i] = new Button(getActivity());
				btn_array[i].setId(i + 1);
				btn_array[i].setLayoutParams(btn_dimension_param);
				btn_array[i].setTextColor(Color.parseColor("#016d72"));
				btn_array[i].setGravity(Gravity.CENTER);
				btn_array[i].setText(" "+time_list.get(i));
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
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
							selected_time=btn_array[i].getText().toString();
						} else {
							
							btn_array[i].setTextColor(Color.parseColor("#016d72"));
							btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
						}
					}
				}
			});
		}
	}
	
	public void createAvailTableUI(LinearLayout ll_table) {
		
		LinearLayout.LayoutParams btn_dimension_param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1f);
		
		btn_dimension_param.rightMargin=5;
		LinearLayout row_layout = null;
		final Button[] btn_array = new Button[available_table_list.size()];
		for (int i = 0; i < available_table_list.size(); i++) {
			if (i % 4 == 0) {
				row_layout = new LinearLayout(getActivity());
				row_layout.setWeightSum(0);
				LinearLayout.LayoutParams row_param = new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT
						, LayoutParams.WRAP_CONTENT);
				row_param.setMargins(5, 3, 5, 3);
				       
				row_layout.setLayoutParams(row_param);
				ll_table.addView(row_layout);

				btn_array[i] = new Button(getActivity());
				btn_array[i].setId(i + 1);
				btn_array[i].setLayoutParams(btn_dimension_param);
				btn_array[i].setTextColor(Color.parseColor("#016d72"));
				btn_array[i].setGravity(Gravity.CENTER);
				btn_array[i].setText(String.format("%s(%s)", available_table_list.get(i), table_seats_list.get(i)));
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
				row_layout.addView(btn_array[i]);
			} else {
				
				btn_array[i] = new Button(getActivity());
				btn_array[i].setId(i + 1);
				btn_array[i].setLayoutParams(btn_dimension_param);
				btn_array[i].setTextColor(Color.parseColor("#016d72"));
				btn_array[i].setGravity(Gravity.CENTER);
				btn_array[i].setText(String.format("%s(%s)", available_table_list.get(i), table_seats_list.get(i)));
				btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
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
							selected_table = available_table_list.get(i);
						}
						else {
							btn_array[i].setTextColor(Color.parseColor("#016d72"));
							btn_array[i].setBackgroundColor(getActivity().getResources().getColor(R.color.Gray));
						}
					}
				}
			});
		}
	}
	
	public void bookTable() {

		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/reversetable.php?hotel_id=5632&cust_id=109&table_no=38&date=2013-08-30&s_time=15:00:00&no_of_person=5&session=lunch
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String cid = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("cust_id", cid);
			params.add("session", "lunch");
			params.add("date", (String)btDate.getTag());
			params.add("s_time", selected_time.trim());
			params.add("no_of_person", btPersons.getText().toString().trim());
			params.add("table_no", selected_table.trim());
			
			pbLoading.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/reversetable.php", params, new AsyncHttpResponseHandler() {
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
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	        			String msg = jsonObject.getString("message");
	        			MessageBox.OK(getActivity(), "Alert", msg);
	            		
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
