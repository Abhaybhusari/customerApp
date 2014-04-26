package com.appybite.customer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.db.ExtrasDatabase;
import com.appybite.customer.db.LocalOrderListDatabase;
import com.appybite.customer.db.ModifierDatabase;
import com.appybite.customer.info.DepartInfo;
import com.appybite.customer.info.ExtraInfo;
import com.appybite.customer.info.ItemInfo;
import com.appybite.customer.info.ModifierInfo;
import com.appybite.customer.info.ReceiptInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.dialog.NumberPickerDlg;
import com.yj.commonlib.dialog.NumberPickerDlg.OnFinishedListener;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class ItemDetailsFragment extends Fragment {

	public static final String order_inrestaurant = "restaurant";
	public static final String order_inroom = "room";

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private DepartInfo departInfo;
	private ItemInfo itemInfo;

	private RelativeLayout rlItemInfo;
	private ImageView ivImage, ivFavourites, ivPlay;
	private TextView tvTitle, tvDesc, tvPrice;
	private EditText etMsg;
	private Spinner spCount;
	private Button btAddOrder, btBookItem;
	private TextView tvModifiers, tvExtras;
	private RatingBar rbScore;
	private LinearLayout llModifiers, llExtras;

	private ArrayList<ExtraInfo> aryExtraList = new ArrayList<ExtraInfo>();
	private ArrayList<ModifierInfo> aryModifierList = new ArrayList<ModifierInfo>();

	private ProgressBar pbCategory;
	private boolean bPlay, bBookable;

	private boolean isDemo;
	
	public ItemDetailsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = null;
		if (DeviceUtil.isTabletByRes(getActivity()))
			v = inflater.inflate(R.layout.frag_itemdetails_tab, container,
					false);
		else
			v = inflater.inflate(R.layout.frag_itemdetails, container, false);

		isDemo = PrefValue.getBoolean(getActivity(), R.string.pref_app_demo);
		
		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		if(departInfo.isRestaurant && !isDemo)
			loadSubInfo();
		
		return v;
	}
	  
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		if(departInfo.id > 0) {
			options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.bg_default_category)
				.showImageForEmptyUri(R.drawable.bg_default_category)
				.showImageOnFail(R.drawable.bg_default_category)
				.cacheInMemory(false)
				.cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		} else {
			options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.bg_default_food)
				.showImageForEmptyUri(R.drawable.bg_default_food)
				.showImageOnFail(R.drawable.bg_default_food)
				.cacheInMemory(false)
				.cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		}

		rlItemInfo = (RelativeLayout)v.findViewById(R.id.rlItemInfo);
		ivImage = (ImageView)v.findViewById(R.id.ivImage);
		ImageLoader.getInstance().displayImage(itemInfo.thumb, ivImage, options, animateFirstListener);
		
		ivPlay = (ImageView)v.findViewById(R.id.ivPlay);
		String videoID = getVideoID(itemInfo.video);
		// String videoID = itemInfo.video;
		if(videoID == null || videoID.trim().length() == 0) {
			ivPlay.setVisibility(View.GONE);
		} else {
			ivPlay.setVisibility(View.VISIBLE);
			
			Log.i("Youtube", itemInfo.video + " => " + videoID);
			((MainActivity)getActivity()).addYoutubeView(videoID);
		}
		ivPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(bPlay) {
					((MainActivity)getActivity()).hideYoutubeView();
					ivPlay.setBackgroundResource(R.drawable.btn_play);
					bPlay = false;
				} else {
					((MainActivity)getActivity()).showYoutubeView();
					ivPlay.setBackgroundResource(R.drawable.btn_pause);
					bPlay = true;
				}
			}
		});
		
		tvTitle = (TextView)v.findViewById(R.id.tvTitle);
		tvTitle.setText(itemInfo.title);
		
		ivFavourites = (ImageView)v.findViewById(R.id.ivFavourites);
		if(isDemo) ivFavourites.setVisibility(View.INVISIBLE);
		ivFavourites.setTag(0);
		ivFavourites.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				updateFavouriteItem(ivFavourites.getTag().toString().equalsIgnoreCase("0"));
			}
		});
		
		tvDesc = (TextView)v.findViewById(R.id.tvDesc);
		if(isDemo) 
			tvDesc.setText("Demo verion doesn't provide description");
		else
			tvDesc.setText(itemInfo.desc);
		
		tvPrice = (TextView)v.findViewById(R.id.tvPrice);
		if(isDemo) {
			tvPrice.setText("Unknown");
		} else {
			float price = Float.parseFloat(itemInfo.price);
			if(price == 0)
				tvPrice.setText("Free");
			else
				tvPrice.setText(String.format("%s %.2f", PrefValue.getString(getActivity(), R.string.pref_currency), price));
		}
		
		etMsg = (EditText)v.findViewById(R.id.etMsg);
		if (isDemo) {
			etMsg.setVisibility(View.GONE);
		} else {
			etMsg.setVisibility(View.VISIBLE);
		}
		
		spCount = (Spinner)v.findViewById(R.id.spCount);

		ArrayAdapter<CharSequence> adspin = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<String>());
		for (int i = 1; i <= 20; i++) {
			adspin.add(String.valueOf(i));	
		}
        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCount.setAdapter(adspin);
        
		btAddOrder = (Button)v.findViewById(R.id.btAddOrder);
		if(departInfo.id == 74 && departInfo.title.equalsIgnoreCase("Rooms/Suites")) {

			btAddOrder.setVisibility(View.INVISIBLE);
			bBookable = true;
		} 
		btAddOrder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				/*if(isDemo) {
					MessageBox.OK(getActivity(), "Alert", "You can't add order in demo version.");
					return;
				}*/
				if (isDemo) {
					String pay_type = LocalOrderListDatabase.pay_type_charge_room;
					String status = LocalOrderListDatabase.status_open;
					inserNewOrderForDemo();
				}
				else
					showOrderDlg();
			}
		});

		btBookItem = (Button)v.findViewById(R.id.btBookItem);
		btBookItem.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				if(isDemo) {
					MessageBox.OK(getActivity(), "Alert", "You can't book item in demo version.");
					return;
				}

				itemInfo.qnt = Integer.valueOf(spCount.getSelectedItem().toString());
				
				if(bBookable) {
					((MainActivity)getActivity()).goBookRoom(false, itemInfo);
					return;
				} else {
					((MainActivity)getActivity()).goBookItem(false, departInfo, itemInfo);
					return;
				}
			}
		});
		
		
		rbScore = (RatingBar) v.findViewById(R.id.rbScore);
		if(isDemo) rbScore.setClickable(false);
		rbScore.setMax(10);
		rbScore.setProgress((int) (itemInfo.rate * 2));
		rbScore.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if (event.getAction() == MotionEvent.ACTION_UP) {
					final Dialog dialog = new Dialog(getActivity());
					dialog.setTitle("Rate it!");
					dialog.setContentView(R.layout.dialog_rating);

					final RatingBar rbScore = (RatingBar)dialog.findViewById(R.id.reviewStars);
					
					Button btAccept = (Button)dialog.findViewById(R.id.reviewWriteAccept);
					btAccept.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							
							updateRatingScore(rbScore.getProgress());
							dialog.dismiss();
						}
					});
					
					Button btCancel = (Button)dialog.findViewById(R.id.reviewWriteCancel);
					btCancel.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							
							dialog.dismiss();
						}
					});
					
					dialog.show();
		        }
		        return true;
		    }
		});

		tvModifiers = (TextView)v.findViewById(R.id.tvModifiers);
		llModifiers = (LinearLayout)v.findViewById(R.id.llModifiers);
		tvExtras = (TextView)v.findViewById(R.id.tvExtras);
		llExtras = (LinearLayout)v.findViewById(R.id.llExtras);
		
		tvModifiers.setVisibility(View.GONE);
		llModifiers.setVisibility(View.GONE);
		tvExtras.setVisibility(View.GONE);
		llExtras.setVisibility(View.GONE);

		pbCategory = (ProgressBar)v.findViewById(R.id.pbCategory);
		pbCategory.setVisibility(View.INVISIBLE);
		
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		PRJFUNC.mGrp.relayoutView(rlItemInfo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
		PRJFUNC.mGrp.repaddingView(tvTitle);
		PRJFUNC.mGrp.relayoutView(ivFavourites, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(ivPlay, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.setTextViewFontScale(tvPrice);
		PRJFUNC.mGrp.relayoutView(tvPrice, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.repaddingView(tvPrice);

		PRJFUNC.mGrp.setTextViewFontScale(tvDesc);
		PRJFUNC.mGrp.relayoutView(tvDesc, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.repaddingView(tvDesc);

		PRJFUNC.mGrp.setEditTextFontScale(etMsg);
		PRJFUNC.mGrp.relayoutView(etMsg, LayoutLib.LP_LinearLayout);

		TextView tvCountLabel = (TextView)v.findViewById(R.id.tvCount); 
		PRJFUNC.mGrp.setTextViewFontScale(tvCountLabel);
		PRJFUNC.mGrp.relayoutView(tvCountLabel, LayoutLib.LP_LinearLayout);

		PRJFUNC.mGrp.setButtonFontScale(btAddOrder);

		PRJFUNC.mGrp.setTextViewFontScale(tvModifiers);
		PRJFUNC.mGrp.relayoutView(tvModifiers, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.repaddingView(tvModifiers);

		PRJFUNC.mGrp.setTextViewFontScale(tvExtras);
		PRJFUNC.mGrp.relayoutView(tvExtras, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.repaddingView(tvExtras);

		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
	}
	
	@Override
	public void onDestroy() {
		
		CustomerHttpClient.stop();
		
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();
		
		((MainActivity)getActivity()).removeYoutubeView();
		
		super.onDestroy();
	}
	@Override
	public void onHiddenChanged(boolean hidden) {

		if(bPlay) {
			((MainActivity)getActivity()).hideYoutubeView();
			ivPlay.setBackgroundResource(R.drawable.btn_play);
			bPlay = false;
		}

		super.onHiddenChanged(hidden);
	}
	
	public void setDepartInfo(DepartInfo departInfo, ItemInfo itemInfo) {
		
		this.departInfo = departInfo;
		this.itemInfo = itemInfo;
	}
	
	public void loadSubInfo() {
		
		aryExtraList.clear();
		aryModifierList.clear();

		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. Restaurant :  http://www.appyorder.com/pro_version/webservice_cashier_fd/new/GetExtraModifier.php?hotel_id=6759&id=2414
			String url = "http://www.appyorder.com/pro_version/webservice_cashier_fd/new/GetExtraModifier.php";
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("id", itemInfo.id);
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbCategory.setVisibility(View.INVISIBLE);
					updateExtras();
					updateModifiers();
					
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
            			if(jsonObject.has("extra") && !jsonObject.isNull("extra"))
            			{
    		            	/*
    						{
    						    "status": "true",
    						    "data": [
    				            {
    				                "id": "50",
    				                "price": "30",
    				                "title": "cheese",
    				                "image": "http://www.roomallocator.com/restaurant/extra_thumbnail/small_544686screenshot368jpg",
    				                "thumbnail": "http://www.roomallocator.com/restaurant/extra_thumbnail/small_544686screenshot368jpg",
    				                "qnt": "0"
    				            }
    						    ]
    						}
    		            	*/
            				JSONObject extra = jsonObject.getJSONObject("extra");
            				String status = extra.getString("status");
            				if(status.equalsIgnoreCase("true"))
            				{
    	            			JSONArray data = extra.getJSONArray("data");
    	            			for (int i = 0; i < data.length(); i++) {
    								JSONObject item = data.getJSONObject(i);
    								
    								ExtraInfo info = new ExtraInfo();
    								info.id = item.getString("id");
    								info.title = item.getString("title");
    								info.price = item.getString("price");
    								info.image = item.getString("image");
    								info.thumb = item.getString("thumbnail");
    								
    								aryExtraList.add(info);
    							}
            				}
            			}

            			if(jsonObject.has("modifier") && !jsonObject.isNull("modifier"))
            			{
            				/*
							{
							    "id": "487",
							    "price": "47",
							    "name": "Package of botatos",
							    "property": "",
							    "group": "0",
							    "qnt": "0"
							} 
            				 */
            				JSONObject modifier = jsonObject.getJSONObject("modifier");
            				String status = modifier.getString("status");
            				if(status.equalsIgnoreCase("true"))
            				{
    	            			JSONArray data = modifier.getJSONArray("data");
    	            			for (int i = 0; i < data.length(); i++) {
    								JSONObject item = data.getJSONObject(i);
    								
    								ModifierInfo info = new ModifierInfo();
    								info.id = item.getString("id");
    								info.title = item.getString("name");
    								info.price = item.getString("price");
    								
    								aryModifierList.add(info);
    							}
            				}
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
	
	private void updateExtras() {
		
		llExtras.removeAllViews();
		
		if(aryExtraList.size() > 0) {
			
			tvExtras.setVisibility(View.VISIBLE);
			llExtras.setVisibility(View.VISIBLE);
			
			for (int i = 0; i < aryExtraList.size(); i++) {

				final ExtraInfo value = aryExtraList.get(i);

				LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				View item = vi.inflate(R.layout.item_extralist, llExtras, false);

				CheckBox cbTitle = (CheckBox) item.findViewById(R.id.cbTitle);
				TextView tvPrice = (TextView) item.findViewById(R.id.tvPrice);
				final Button btQnt = (Button) item.findViewById(R.id.btQnt);

				if (!PRJFUNC.DEFAULT_SCREEN) {
					
					PRJFUNC.mGrp.setButtonFontScale(cbTitle);
					PRJFUNC.mGrp.relayoutView(cbTitle, LayoutLib.LP_RelativeLayout);
					PRJFUNC.mGrp.setTextViewFontScale(tvPrice);
					PRJFUNC.mGrp.setButtonFontScale(btQnt);
					PRJFUNC.mGrp.relayoutView(btQnt, LayoutLib.LP_RelativeLayout);
				}
				
				cbTitle.setText(value.title);
				if(value.qnt > 0) cbTitle.setChecked(true);
				else cbTitle.setChecked(false);
				cbTitle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						
						if(arg1 && value.qnt == 0)
							value.qnt = 1;
						else if(!arg1)
							value.qnt = 0;
						
						btQnt.setText(String.valueOf(value.qnt));
					}
				});
				float price = Float.parseFloat(value.price);
				if(price > 0)
					tvPrice.setText("+" + String.format("%.2f", price));
				else
					tvPrice.setText("");
				btQnt.setText(String.valueOf(value.qnt));
				final int position = i;
				btQnt.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						NumberPickerDlg countDlg = new NumberPickerDlg(getActivity(), value.qnt, new OnFinishedListener() {
							
							@Override
							public void onOk(int number) {
								
								applyCount(position, number);
							}
						});
						countDlg.show();
					}
				});
				
				llExtras.addView(item);
			}
		}
	}
	
	private void updateModifiers() {

		llModifiers.removeAllViews();
		
		if(aryModifierList.size() > 0) {

			tvModifiers.setVisibility(View.VISIBLE);
			llModifiers.setVisibility(View.VISIBLE);

			for (int i = 0; i < aryModifierList.size(); i++) {

				final ModifierInfo value = aryModifierList.get(i);

				LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				View item = vi.inflate(R.layout.item_modifierlist, llModifiers, false);
				CheckBox cbTitle = (CheckBox) item.findViewById(R.id.cbTitle);
				TextView tvPrice = (TextView) item.findViewById(R.id.tvPrice);
				
				if (!PRJFUNC.DEFAULT_SCREEN) {
					PRJFUNC.mGrp.setButtonFontScale(cbTitle);
					PRJFUNC.mGrp.relayoutView(cbTitle, LayoutLib.LP_RelativeLayout);
					PRJFUNC.mGrp.setTextViewFontScale(tvPrice);
				}
				
				cbTitle.setText(value.title);
				if(value.qnt > 0) cbTitle.setChecked(true);
				cbTitle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						
						if(arg1)
							value.qnt = 1;
						else
							value.qnt = 0;
					}
				});
				float price = Float.parseFloat(value.price);
				if(price > 0)
					tvPrice.setText("+" + String.format("%.2f", price));
				else
					tvPrice.setText("");
				
				llModifiers.addView(item);
			}
		}
	}
	
	private void applyCount(int position, int count)
	{
		ExtraInfo value = aryExtraList.get(position);
		value.qnt = count;
		updateExtras();
	}
	
	private void scanQRCode() {
		
		if (isIntentAvailable(getActivity())) {
			Log.i("Scanner", "BarCode Scanner is Available");
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_FORMATS", "QR_CODE_MODE");
			getActivity().startActivityForResult(intent, 101);
		} else if (isSdPresent() && MemoryStat(508220)) {
			if (saveas(R.raw.scan)) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/barcode.apk")), "application/vnd.android.package-archive");
				startActivity(intent);
			} else {
				market();
			}
		} else {
			market();
		}	
	}
	
	private boolean isIntentAvailable(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		Log.i("Scanner", "intent result=" + (list.size() > 0));
		return list.size() > 0;
	}

	private static boolean isSdPresent() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	private boolean MemoryStat(long bytesDownloading) {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		if (stat.getAvailableBlocks() > 0) {
			long bytesAvailable = (long) stat.getBlockSize()
					* (long) stat.getAvailableBlocks();
			if (bytesAvailable > bytesDownloading) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void market() {
		try {
			if (NetworkUtils.haveInternet(getActivity())) {
				Intent search = new Intent(Intent.ACTION_VIEW);
				search.setData(Uri
						.parse("market://details?id=com.google.zxing.client.android"));
				startActivity(search);
			} else {
				Toast.makeText(
						getActivity(),
						"Please insert Sd-Card or connect to internet to install Barcode Scanner.",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean saveas(int resfile) {
		byte[] buffer = null;
		InputStream fIn = getActivity().getResources().openRawResource(resfile);
		int size = 0;
		try {
			size = fIn.available();
			buffer = new byte[size];
			fIn.read(buffer);
			fIn.close();
		} catch (IOException e) {
			return false;
		}
		String path = Environment.getExternalStorageDirectory().getPath();// "/sdcard/";
		String filename = "/barcode" + ".apk";
		boolean exists = (new File(path)).exists();
		if (!exists) {
			new File(path).mkdirs();
		}
		FileOutputStream save;
		try {
			save = new FileOutputStream(path + filename);
			save.write(buffer);
			save.flush();
			save.close();
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public void addToCart() {
		
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		
		String msg = etMsg.getText().toString();
		String order_type = PrefValue.getString(getActivity(), R.string.pref_order_type);
		itemInfo.qnt = Integer.valueOf(spCount.getSelectedItem().toString());

		LocalOrderListDatabase foodDB = new LocalOrderListDatabase(getActivity());
		ExtrasDatabase extraDB = new ExtrasDatabase(getActivity());
		ModifierDatabase modifierDB = new ModifierDatabase(getActivity());
		
		// For demo. it will be replace with real price.
		if (itemInfo.price == null)
			itemInfo.price = "0";

		foodDB.insert(hotel_id, c_id, itemInfo.id, itemInfo.title, itemInfo.price, itemInfo.qnt, msg, String.valueOf(departInfo.id), departInfo.title, order_type);
		
		for (int i = 0; i < aryExtraList.size(); i++) {
			
			ExtraInfo info = aryExtraList.get(i);
			if(info.qnt > 0)
				extraDB.insert(hotel_id, c_id, itemInfo.id, info.id, info.title, info.price, info.qnt);
		}
		
		for (int i = 0; i < aryModifierList.size(); i++) {
			
			ModifierInfo info = aryModifierList.get(i);
			if(info.qnt > 0)
				modifierDB.insert(hotel_id, c_id, itemInfo.id, info.id, info.title, info.price, info.qnt);
		}
		
		foodDB.close();
		extraDB.close();
		modifierDB.close();
		
		if(DeviceUtil.isTabletByRes(getActivity()))
			((MainActivity)getActivity()).goCart_Tab();
		else
			((MainActivity)getActivity()).goCart();
	}
	
	private void updateFavouriteItem(final boolean add) {
		
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
	            			
	            			if(add) {
	            				ivFavourites.setBackgroundResource(R.drawable.menu_favourites_on);
	            				ivFavourites.setTag(1);
	            			} else {
	            				ivFavourites.setBackgroundResource(R.drawable.menu_favourites);
	            				ivFavourites.setTag(0);
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
	
	public void updateRatingScore(int rate) {
		
		if (NetworkUtils.haveInternet(getActivity())) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/new/setrateitem.php?hotel_id=6759&item_id=554&user_id=5&rate=1
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("user_id", c_id);
			params.add("item_id", itemInfo.id);
			params.add("rate", String.valueOf(rate));
			
			pbCategory.setVisibility(View.VISIBLE);
			CustomerHttpClient.get("new/setrateitem.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					pbCategory.setVisibility(View.INVISIBLE);
					updateExtras();
					updateModifiers();
					
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
	            		
	            		String status = jsonObject.getString("status");
	            		if(status.equalsIgnoreCase("true")) {
	            			double rate = jsonObject.getDouble("rate");
	            			rbScore.setProgress((int) (rate * 2));
	            			itemInfo.rate = rate;
	            			((MainActivity)getActivity()).updateItemList(itemInfo);
	            		} else {
	            			MessageBox.OK(getActivity(), "Result", jsonObject.getString("message"));
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
	
	private String getVideoID(String url) {

		/*
		 	http://www.youtube.com/embed/Woq5iX9XQhA?html5=1
			http://www.youtube.com/watch?v=384IUU43bfQ
			http://gdata.youtube.com/feeds/api/videos/xTmi7zzUa-M&whatever
			
			Woq5iX9XQhA
			384IUU43bfQ
			xTmi7zzUa-M
		 * */
		if(url == null || url.trim().length() == 0) {
			return null;
		}
		
		String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

	    Pattern compiledPattern = Pattern.compile(pattern);
	    Matcher matcher = compiledPattern.matcher(url);

	    if(matcher.find()){
	        return matcher.group();
	    }
	    
	    return null;
	}
	
	private void showOrderDlg() {
		
		final Dialog dialog = new Dialog(getActivity(),
				android.R.style.Theme_Translucent_NoTitleBar);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_addorder);
		
		Button btOldCode = (Button)dialog.findViewById(R.id.btOldCode);
		btOldCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				addToCart();
				dialog.dismiss();
			}
		});
		Button btTableCode = (Button)dialog.findViewById(R.id.btTableCode);
		if (isDemo)
			btTableCode.setVisibility(View.GONE);
		btTableCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				PrefValue.setString(getActivity(), R.string.pref_order_type, order_inrestaurant);
				scanQRCode();
				dialog.dismiss();
			}
		});
		Button btRoomCode = (Button)dialog.findViewById(R.id.btRoomCode);
		if (isDemo)
			btRoomCode.setVisibility(View.INVISIBLE);
		btRoomCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				PrefValue.setString(getActivity(), R.string.pref_order_type, order_inroom);
				scanQRCode();
				dialog.dismiss();
			}
		});
		Button btCancel = (Button)dialog.findViewById(R.id.btCancel);
		btCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				dialog.dismiss();
			}
		});
		
		Button btTest = (Button)dialog.findViewById(R.id.btTest);
		btTest.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				PrefValue.setString(getActivity(), R.string.pref_order_type, order_inroom);
				PrefValue.setString(getActivity(), R.string.pref_order_code, "301");
				addToCart();
				
				dialog.dismiss();
			}
		});

		TextView tvOldCode = (TextView)dialog.findViewById(R.id.tvOldCode);
		String order_type = PrefValue.getString(getActivity(), R.string.pref_order_type, "");
		String order_code = PrefValue.getString(getActivity(), R.string.pref_order_code, "");
		if(order_type.length() > 0) {
			tvOldCode.setText(String.format("Your last order place is\n%s %s\nWould you use it?", 
					order_type.toUpperCase(),  
					order_code));
		} else {
			btOldCode.setVisibility(View.GONE);
			tvOldCode.setVisibility(View.GONE);
			ImageView iv1 = (ImageView)dialog.findViewById(R.id.iv1);
			iv1.setVisibility(View.GONE);
		}
		
		if (PRJFUNC.mGrp != null) {
			RelativeLayout rlAddOrderDlg = (RelativeLayout)dialog.findViewById(R.id.rlAddOrderDlg);
			PRJFUNC.mGrp.relayoutView(rlAddOrderDlg, LayoutLib.LP_RelativeLayout);
			ImageView ivQRCode = (ImageView)dialog.findViewById(R.id.ivQRCode);
			PRJFUNC.mGrp.relayoutView(ivQRCode, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvOldCode);
			PRJFUNC.mGrp.relayoutView(tvOldCode, LayoutLib.LP_RelativeLayout);
			
			PRJFUNC.mGrp.setButtonFontScale(btOldCode);
			PRJFUNC.mGrp.relayoutView(btOldCode, LayoutLib.LP_RelativeLayout);
			
			TextView tvNewCode = (TextView)dialog.findViewById(R.id.tvNewCode);
			if (isDemo)
				tvNewCode.setVisibility(View.GONE);
			PRJFUNC.mGrp.setTextViewFontScale(tvNewCode);
			PRJFUNC.mGrp.relayoutView(tvNewCode, LayoutLib.LP_RelativeLayout);
			
			PRJFUNC.mGrp.setTextViewFontScale(btTableCode);
			PRJFUNC.mGrp.relayoutView(btTableCode, LayoutLib.LP_RelativeLayout);
			
			PRJFUNC.mGrp.setTextViewFontScale(btRoomCode);
			PRJFUNC.mGrp.relayoutView(btRoomCode, LayoutLib.LP_RelativeLayout);
			
			PRJFUNC.mGrp.setTextViewFontScale(btCancel);
			PRJFUNC.mGrp.relayoutView(btCancel, LayoutLib.LP_RelativeLayout);
			
			PRJFUNC.mGrp.setTextViewFontScale(btTest);
			PRJFUNC.mGrp.relayoutView(btTest, LayoutLib.LP_RelativeLayout);
		}

		dialog.show();
	}
	
/*
 * For Demo Hotel
 * Direct Ordering	
 */
	String pay_type = "";
	String status = "";
	
	private void inserNewOrderForDemo()
	{
		//. http://www.roomallocator.com/appcreator/services/insertorder.php?
		//. hotel_id=18&item_id=22&item_name=mohamed&cust_id=1&qt=5
		if (NetworkUtils.haveInternet(getActivity())) {
			
			String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
			String customer_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
			
			/*ArrayList<ReceiptInfo> foodList = 
				foodDB.getFoodItemList(hotel_id, customer_id, LocalOrderListDatabase.status_pending);*/ 
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			params.add("item_id", itemInfo.id);
			params.add("item_name", itemInfo.title);
			params.add("cust_id", customer_id);
			params.add("qt", "1");
			
			/*try {
				params.add("data", getOrderJson().toString());
			} catch (JSONException e1) {
				
				Toast.makeText(getActivity(), "Invalid Order", Toast.LENGTH_LONG).show();
				e1.printStackTrace();
				return;
			}*/
			String url = "http://www.roomallocator.com/appcreator/services/insertorder.php";
			DialogUtils.launchProgress(getActivity(), "Please wait while send orders.");
			/*CustomerHttpClient.post("services/insertorder.php", params, new AsyncHttpResponseHandler() {*/
			/*CustomerHttpClient.get("services/insertorder.php", params, new AsyncHttpResponseHandler() {*/
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				
				boolean success = false;
				
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					if(success) {
					}
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
		            	 {"status":"true","message":"New Order Added"}	
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");

            			if(status.equalsIgnoreCase("true")) {
            				
            				Toast.makeText(getActivity(), "Successfully Ordered",Toast.LENGTH_LONG).show();
            				// MessageBox.OK(getActivity(), "Alert", "Successfully Ordered");
	            			success = true;
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
