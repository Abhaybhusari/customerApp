package com.appybite.customer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appybite.customer.info.HotelInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class HotelFragment extends Fragment {
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private HotelInfo hotelInfo;
	
	private RelativeLayout rlHotelInfo;
	private ImageView ivHotelBg;
	private TextView tvHotelDesc;
	
	public HotelFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_hotel, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		rlHotelInfo.setVisibility(View.VISIBLE);
		if(hotelInfo.id > 0)
			ImageLoader.getInstance().displayImage(hotelInfo.hotel_bg, ivHotelBg, options, animateFirstListener);
		else
			ivHotelBg.setImageResource(R.drawable.bg_default_restaurant);
		tvHotelDesc.setText(hotelInfo.hotel_desc);

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
		
		rlHotelInfo = (RelativeLayout)v.findViewById(R.id.rlHotelInfo);
		
		ivHotelBg = (ImageView)v.findViewById(R.id.ivHotelBg);
		tvHotelDesc = (TextView)v.findViewById(R.id.tvHotelDesc);
		
		PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		PRJFUNC.mGrp.relayoutView(rlHotelInfo, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvHotelDesc);
		PRJFUNC.mGrp.repaddingView(tvHotelDesc);
		
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
	
	public void setHotelInfo(HotelInfo hotelInfo) {
		this.hotelInfo = hotelInfo;
	}
}
