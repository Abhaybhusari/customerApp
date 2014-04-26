package com.appybite.customer;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HotelHomeFragment extends Fragment {
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	private ImageView ivHotelBg, ivHotelLogo;
	private RelativeLayout rlLogo;
	private TextView tvHotelName;
	
	public HotelHomeFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_hotel_home, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
	
		loadHotelInfo();
		
		return v;
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.home_hotel_bg)
			.showImageForEmptyUri(R.drawable.home_hotel_bg)
			.showImageOnFail(R.drawable.home_hotel_bg)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		ivHotelBg = (ImageView)v.findViewById(R.id.ivHotelBg);
		ivHotelLogo = (ImageView)v.findViewById(R.id.ivHotelLogo);
		tvHotelName = (TextView)v.findViewById(R.id.tvHotelName);
	}
	
	private void scaleView(View v) {
		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		rlLogo = (RelativeLayout)v.findViewById(R.id.rlLogo);
		PRJFUNC.mGrp.relayoutView(rlLogo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(ivHotelLogo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvHotelName);
		PRJFUNC.mGrp.relayoutView(tvHotelName, LayoutLib.LP_RelativeLayout);
		TextView tvWelcome = (TextView)v.findViewById(R.id.tvWelcome);
		PRJFUNC.mGrp.relayoutView(tvWelcome, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvWelcome);
	}
	
	@Override
	public void onDestroy() {
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();
		super.onDestroy();
	}

	public void loadHotelInfo()
	{
		if (!PrefValue.getString(getActivity(), R.string.pref_hotel_first).equalsIgnoreCase("-1")) {
			//. Hotel Info
			ImageLoader.getInstance().displayImage(PrefValue.getString(getActivity(), R.string.pref_hotel_bg), ivHotelBg, options, animateFirstListener);
			ImageLoader.getInstance().displayImage(PrefValue.getString(getActivity(), R.string.pref_hotel_logo), ivHotelLogo, null, animateFirstListener);
			tvHotelName.setText(PrefValue.getString(getActivity(), R.string.pref_hotel_name));
		}
	}
}
