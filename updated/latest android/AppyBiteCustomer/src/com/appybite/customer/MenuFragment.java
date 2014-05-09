package com.appybite.customer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class MenuFragment extends Fragment {

	private ImageView ivProfile;
	private TextView tvName;
	private Button btChange, btLogout;
	
	private boolean isDemo; 
	
	public MenuFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_menu, container, false);

		isDemo = PrefValue.getBoolean(getActivity(), R.string.pref_app_demo);
		
		updateLCD(v);

		// - update position
//fgh
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		return v;
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		RelativeLayout rlPrinter = (RelativeLayout)v.findViewById(R.id.rlPrinter);
		if(isDemo) rlPrinter.setVisibility(View.GONE);
		rlPrinter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goPrinter();
			}
		});
		
		RelativeLayout rlFavourites = (RelativeLayout)v.findViewById(R.id.rlFavourites);
		if(isDemo) rlFavourites.setVisibility(View.GONE);
		rlFavourites.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				if(DeviceUtil.isTabletByRes(getActivity()))
					((MainActivity)getActivity()).goFavourites_Tab();
				else
					((MainActivity)getActivity()).goFavourites();
			}
		});
		
		RelativeLayout rlBookTable = (RelativeLayout)v.findViewById(R.id.rlBookTable);
		if(isDemo) rlBookTable.setVisibility(View.GONE);
		rlBookTable.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goBookTable();
			}
		});

		RelativeLayout rlReserveRoom = (RelativeLayout)v.findViewById(R.id.rlReserveRoom);
		if(isDemo) rlReserveRoom.setVisibility(View.GONE);
		rlReserveRoom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goBookRoom(true, null);
			}
		});
		
		RelativeLayout rlExtendStay = (RelativeLayout)v.findViewById(R.id.rlExtendStay);
		if(isDemo) rlExtendStay.setVisibility(View.GONE);
		rlExtendStay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goExtendStay();
			}
		});
		
		RelativeLayout rlRoomBill = (RelativeLayout)v.findViewById(R.id.rlRoomBill);
		if(isDemo) rlRoomBill.setVisibility(View.GONE);
		rlRoomBill.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goRoomBill();
			}
		});
		
		RelativeLayout rlAlarm = (RelativeLayout)v.findViewById(R.id.rlAlarm);
		if(isDemo) rlAlarm.setVisibility(View.GONE);
		rlAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				Intent i = new Intent(AlarmClock.ACTION_SET_ALARM); 
				i.putExtra(AlarmClock.EXTRA_MESSAGE, "New Alarm"); 
//				i.putExtra(AlarmClock.EXTRA_HOUR, 10); 
//				i.putExtra(AlarmClock.EXTRA_MINUTES, 30); 
				startActivity(i); 
			}
		});
		
		RelativeLayout rlAdditionalAcct = (RelativeLayout)v.findViewById(R.id.rlAdditionAccount);
		String allowed = PrefValue.getString(getActivity(), R.string.pref_hotel_allowed);
		//if(allowed.equalsIgnoreCase("false")) rlAdditionalAcct.setVisibility(View.GONE);
		rlAdditionalAcct.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goAdditionalAccount();
			}
		});
		
		RelativeLayout rlAboutus = (RelativeLayout)v.findViewById(R.id.rlAboutus);
		rlAboutus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goAboutUs();
			}
		});
		
		tvName = (TextView)v.findViewById(R.id.tvName);
		tvName.setText(PrefValue.getString(getActivity(), R.string.pref_customer_name));
		ivProfile = (ImageView)v.findViewById(R.id.ivProfile);
		
		btChange = (Button)v.findViewById(R.id.btChage);
		btChange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).getSlidingMenu().showContent();
				((MainActivity)getActivity()).goUpdateProfile();
			}
		});
		btLogout = (Button)v.findViewById(R.id.btLogout);
		btLogout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
				Editor prefsEditor = preference.edit();
				prefsEditor.clear();
				prefsEditor.commit();
				
				Intent intent = null;
				if(DeviceUtil.isTabletByRes(getActivity())) {
					intent = new Intent(getActivity(), LoginActivity_Tab.class);
				} else {
					intent = new Intent(getActivity(), LoginActivity.class);
				}
				startActivity(intent);
				getActivity().finish();
			}
		});
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		ImageView ivPrinter = (ImageView)v.findViewById(R.id.ivPrinter);
		PRJFUNC.mGrp.relayoutView(ivPrinter, LayoutLib.LP_RelativeLayout);
		
		TextView tvPrinter = (TextView)v.findViewById(R.id.tvPrinter);
		PRJFUNC.mGrp.relayoutView(tvPrinter, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvPrinter);

		ImageView ivFavourites = (ImageView)v.findViewById(R.id.ivFavourites);
		PRJFUNC.mGrp.relayoutView(ivFavourites, LayoutLib.LP_RelativeLayout);

		TextView tvFavourites = (TextView)v.findViewById(R.id.tvFavourites);
		PRJFUNC.mGrp.relayoutView(tvFavourites, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvFavourites);

		ImageView ivBookTable = (ImageView)v.findViewById(R.id.ivBookTable);
		PRJFUNC.mGrp.relayoutView(ivBookTable, LayoutLib.LP_RelativeLayout);

		TextView tvBookTable = (TextView)v.findViewById(R.id.tvBookTable);
		PRJFUNC.mGrp.relayoutView(tvBookTable, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvBookTable);

		ImageView ivReserveRoom = (ImageView)v.findViewById(R.id.ivReserveRoom);
		PRJFUNC.mGrp.relayoutView(ivReserveRoom, LayoutLib.LP_RelativeLayout);

		TextView tvReserveRoom = (TextView)v.findViewById(R.id.tvReserveRoom);
		PRJFUNC.mGrp.relayoutView(tvReserveRoom, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvReserveRoom);

		ImageView ivExtendStay = (ImageView)v.findViewById(R.id.ivExtendStay);
		PRJFUNC.mGrp.relayoutView(ivExtendStay, LayoutLib.LP_RelativeLayout);

		TextView tvExtendStay = (TextView)v.findViewById(R.id.tvExtendStay);
		PRJFUNC.mGrp.relayoutView(tvExtendStay, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvExtendStay);

		ImageView ivRoomBill = (ImageView)v.findViewById(R.id.ivRoomBill);
		PRJFUNC.mGrp.relayoutView(ivRoomBill, LayoutLib.LP_RelativeLayout);

		TextView tvRoomBill = (TextView)v.findViewById(R.id.tvRoomBill);
		PRJFUNC.mGrp.relayoutView(tvRoomBill, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvRoomBill);

		ImageView ivAlarm = (ImageView)v.findViewById(R.id.ivAlarm);
		PRJFUNC.mGrp.relayoutView(ivAlarm, LayoutLib.LP_RelativeLayout);

		TextView tvAlarm = (TextView)v.findViewById(R.id.tvAlarm);
		PRJFUNC.mGrp.relayoutView(tvAlarm, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvAlarm);

		ImageView ivAboutus = (ImageView)v.findViewById(R.id.ivAboutus);
		PRJFUNC.mGrp.relayoutView(ivAboutus, LayoutLib.LP_RelativeLayout);

		TextView tvAboutUs = (TextView)v.findViewById(R.id.tvAboutUs);
		PRJFUNC.mGrp.relayoutView(tvAboutUs, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvAboutUs);
		
		PRJFUNC.mGrp.relayoutView(ivProfile, LayoutLib.LP_RelativeLayout);
		
		PRJFUNC.mGrp.relayoutView(tvName, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvName);

		PRJFUNC.mGrp.relayoutView(btChange, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btChange);

		PRJFUNC.mGrp.relayoutView(btLogout, LayoutLib.LP_LinearLayout);
		PRJFUNC.mGrp.setButtonFontScale(btLogout);
	}
}
