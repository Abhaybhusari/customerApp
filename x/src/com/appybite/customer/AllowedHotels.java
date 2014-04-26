package com.appybite.customer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.info.CategoryInfo;
import com.appybite.customer.info.DepartInfo;
import com.appybite.customer.info.HotelInfo;
import com.appybite.customer.info.ItemInfo;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.dialog.DialogUtils;
import com.yj.commonlib.dialog.MessageBox;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.network.NetworkUtils;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;
import com.yj.commonlib.util.DeviceUtil;

public class AllowedHotels extends SlidingFragmentActivity implements OnInitializedListener {
	private Fragment m_Fragment;
	private ArrayList<HotelInfo> aryHotelList = new ArrayList<HotelInfo>();
	private DisplayImageOptions options, optionsForBg;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	/////////////////////////////////////////////////////////////////////////////
	//. Controls
	private TextView tvHotelHeaderName;
	private Button btBack, btMenu, btHotelChoose;
	private LinearLayout llHotelList;
	
	/////////////////////////////////////////////////////////////////////////////
	//. Tab Controls
	private ImageView ivHotelLogo, ivHotelBg;
	private TextView tvHotelName, tvTime; 
	
	private ArrayList<Fragment> aryFragList = new ArrayList<Fragment>();
	private ArrayList<String> aryTitle = new ArrayList<String>();
	
	public int hotel_id;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(!DeviceUtil.isTabletByRes(this))
			setContentView(R.layout.activity_allowedhotel);
		else
			setContentView(R.layout.activity_allowedhotel_tab);
		
		setBehindContentView(R.layout.menu_frame);
		
		updateLCD();
		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView();
		}
		
		if(DeviceUtil.isTabletByRes(this))
			goHome_Tab();
		else
			goHome();
		
		loadHotelList();
		initSlidingMenu(savedInstanceState);
		instance = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean mIsFinish = false;
	
    // BACK key handler
	private Handler backKeyHandler = new Handler()
	{
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 0)
            	mIsFinish = false;
        }
    };
    
	@Override
	public void onBackPressed() {
		
		if(aryFragList.size() == 0 || 
				(m_Fragment != null && 
				m_Fragment.getClass().getName() == HotelHomeFragment.class.getName())) {
			
			if (!mIsFinish)
			{
				mIsFinish = true;
				Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show();
				backKeyHandler.sendEmptyMessageDelayed(0, 2000);
				return;
			}
			
			super.onBackPressed();
		}
		else if(aryFragList.size() == 1) {
			
			if(DeviceUtil.isTabletByRes(this))
				goHome_Tab();
			else
				goHome();
			
		} else {
			removeFragment(aryFragList.get(aryFragList.size() - 1));
			aryFragList.remove(aryFragList.size() - 1);
			aryTitle.remove(aryTitle.size() - 1);
			
			showFragment(m_Fragment = aryFragList.get(aryFragList.size() - 1));
			setTitle(aryTitle.get(aryTitle.size() - 1));
		}
	}
	
	public void initSlidingMenu(Bundle savedInstanceState)
	{
		Fragment mFrag;
		
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			mFrag = new MenuFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (MenuFragment)this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}

		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.RIGHT);
		sm.setShadowWidthRes(R.dimen.margin);
		sm.setShadowWidth((int)(getResources().getDimensionPixelOffset(R.dimen.margin) * LayoutLib.X_Z));
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		// sm.setShadowDrawable(R.drawable.shadow);
		// sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		int width = (int)(getResources().getDimensionPixelOffset(R.dimen.slidingmenu_offset)* LayoutLib.X_Z);
		sm.setBehindOffset(width);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        setSlidingActionBarEnabled(true);
        sm.setBehindScrollScale(0.0f);
        // menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		// sm.setBehindCanvasTransformer(mTransformer);
	}
	
	private void updateLCD() {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(this);
		}

		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_depart)
			.showImageForEmptyUri(R.drawable.bg_default_depart)
			.showImageOnFail(R.drawable.bg_default_depart)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		
		optionsForBg = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_restaurant)
			.showImageForEmptyUri(R.drawable.bg_default_restaurant)
			.showImageOnFail(R.drawable.bg_default_restaurant)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build(); 
		
		tvHotelHeaderName = (TextView)findViewById(R.id.tvHotelHeaderName);
		tvHotelHeaderName.setSelected(true);

		btBack = (Button)findViewById(R.id.btBack);
		btBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				onBackPressed();
			}
		});
		
		btHotelChoose = (Button)findViewById(R.id.btHotelChoose);
		btHotelChoose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String choosedHotelId = PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_id);
				if (choosedHotelId != null && choosedHotelId.length() != 0) {
					Intent i = null;
					if(DeviceUtil.isTabletByRes(AllowedHotels.this)) {
						i = new Intent(AllowedHotels.this, MainActivity_Tab.class);
					} else {
						i = new Intent(AllowedHotels.this, MainActivity.class);
					}

					ImageLoader.getInstance().stop();
					ImageLoader.getInstance().clearMemoryCache();

					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					finish();
				}
			}
		});
		
		llHotelList = (LinearLayout)findViewById(R.id.llHotelList);
		
		if(DeviceUtil.isTabletByRes(this)) {
			ivHotelLogo = (ImageView)findViewById(R.id.ivHotelLogo);
			ivHotelBg = (ImageView)findViewById(R.id.ivHotelBg);
			tvHotelName = (TextView)findViewById(R.id.tvHotelName);
			tvTime = (TextView)findViewById(R.id.tvTime);
			tvTime.setText("0:00");
			
			ImageLoader.getInstance().displayImage(PrefValue.getString(this, R.string.pref_hotel_bg), ivHotelBg, optionsForBg, animateFirstListener);
			ImageLoader.getInstance().displayImage(PrefValue.getString(this, R.string.pref_hotel_logo), ivHotelLogo, null, animateFirstListener);
			tvHotelName.setText(PrefValue.getString(this, R.string.pref_hotel_name));
		}
		
		setTitle("Welcome");
	}

	private void scaleView() {

		if (PRJFUNC.mGrp == null) {
			return;
		}
		
		RelativeLayout rlTopMenu = (RelativeLayout)findViewById(R.id.rlTopMenu);
		PRJFUNC.mGrp.relayoutView(rlTopMenu, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvHotelHeaderName);
		PRJFUNC.mGrp.relayoutView(btMenu, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(llHotelList, LayoutLib.LP_FrameLayout);
		
		PRJFUNC.mGrp.relayoutView(findViewById(R.id.fl_fragment_youtube), LayoutLib.LP_RelativeLayout);
		
		if(DeviceUtil.isTabletByRes(this)) {
			
			RelativeLayout rlLogo = (RelativeLayout)findViewById(R.id.rlLogo);
			PRJFUNC.mGrp.relayoutView(rlLogo, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(ivHotelLogo, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(ivHotelBg, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(tvHotelName, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvHotelName);
			TextView tvWelcome = (TextView)findViewById(R.id.tvWelcome);
			PRJFUNC.mGrp.relayoutView(findViewById(R.id.tvWelcome), LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvWelcome);
			PRJFUNC.mGrp.relayoutView(tvTime, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvTime);
		}
	}

	public void removeFragment(Fragment fg) {
		if (fg == null)
			return;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.remove(fg);
		ft.commit();
	}

	public void addFragment(Fragment fg) {
		if (fg == null)
			return;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(R.anim.fade_short, R.anim.hold);
		ft.add(R.id.fl_fragment_corner, fg);
		ft.commit();
	}

	public void showFragment(Fragment fg) {
		if (fg == null)
			return;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(R.anim.fade_short, R.anim.hold);
		ft.show(fg);
		ft.commit();
	}

	public void hideFragment(Fragment fg) {
		if (fg == null)
			return;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		// ft.setCustomAnimations(R.anim.fade, R.anim.hold);
		ft.hide(fg);
		ft.commit();
	}
	
	public void goHome()
	{
		setTitle("Welcome");
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == HotelHomeFragment.class.getName())
			return;
		
		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new HotelHomeFragment();
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add("Welcome");
	}
	
	public void goHome_Tab() {
		
		setTitle("Welcome");
		
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();		
	}
	
	public void goHotel(HotelInfo hotelInfo)
	{
//		if(m_Fragment != null && 
//				m_Fragment.getClass().getName() == DepartFragment.class.getName()) {
//
//			((DepartFragment)m_Fragment).loadCategoryList();
//			return;
//		}

		hotel_id = hotelInfo.hotel_id;
		PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_id, String.valueOf(hotel_id));
		
		setTitle(hotelInfo.hotel_name);

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		if(DeviceUtil.isTabletByRes(this)) {

			m_Fragment = new HotelFragment_Tab();
			((HotelFragment_Tab)m_Fragment).setHotelInfo(hotelInfo);
		} else {
			m_Fragment = new HotelFragment();
			((HotelFragment)m_Fragment).setHotelInfo(hotelInfo);
		}
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goSubCategory(DepartInfo departInfo, CategoryInfo categoryInfo)
	{
		setTitle(categoryInfo.name);
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == SubCategoryFragment.class.getName())
			return;
		
		hideFragment(m_Fragment);

		m_Fragment = new SubCategoryFragment();
		((SubCategoryFragment)m_Fragment).setDepartInfo(departInfo, categoryInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goSubCategory_Tab(DepartInfo departInfo, CategoryInfo categoryInfo)
	{
		setTitle(categoryInfo.name);
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == SubCategoryFragment_Tab.class.getName())
			return;
		
		hideFragment(m_Fragment);

		m_Fragment = new SubCategoryFragment_Tab();
		((SubCategoryFragment_Tab)m_Fragment).setDepartInfo(departInfo, categoryInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goItemList(DepartInfo departInfo, CategoryInfo subCatInfo)
	{
		if(subCatInfo == null)
			setTitle(departInfo.title);
		else
			setTitle(subCatInfo.name);
		
		hideFragment(m_Fragment);

		m_Fragment = new ItemListFragment();
		((ItemListFragment)m_Fragment).setDepartInfo(departInfo, subCatInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goItemList_Tab(DepartInfo departInfo, CategoryInfo subCatInfo)
	{
		if(subCatInfo == null)
			setTitle(departInfo.title);
		else
			setTitle(subCatInfo.name);
		
		hideFragment(m_Fragment);

		m_Fragment = new ItemListFragment_Tab();
		((ItemListFragment_Tab)m_Fragment).setDepartInfo(departInfo, subCatInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void updateItemList(ItemInfo itemInfo)
	{
		Fragment fg = aryFragList.get(aryFragList.size() - 1);
		if(m_Fragment.getClass().getName() == ItemListFragment.class.getName()) {
			((ItemListFragment)fg).updateItem(itemInfo);
		} else if(m_Fragment.getClass().getName() == ItemListFragment_Tab.class.getName()) {
			((ItemListFragment_Tab)fg).updateItem(itemInfo);
		}
	}
	
	public void goItemDetails(DepartInfo departInfo, ItemInfo itemInfo)
	{
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == ItemDetailsFragment.class.getName())
			return;
		
		hideFragment(m_Fragment);

		m_Fragment = new ItemDetailsFragment();
		((ItemDetailsFragment)m_Fragment).setDepartInfo(departInfo, itemInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}

	public void goAboutUs() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == AboutUsFragment.class.getName()) {
			
			((AboutUsFragment)m_Fragment).loadHotelInfo();
			return;
		}

		setTitle("About Us");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new AboutUsFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goPrinter() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == PrinterFragment.class.getName()) {
			
			((PrinterFragment)m_Fragment).loadPrinter();
			return;
		}

		setTitle("Printer Setting");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new PrinterFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goBookTable() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == BookTableFragment.class.getName())
			return;

		setTitle("Book Table");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new BookTableFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goBookRoom(boolean isReserveRoom, ItemInfo itemInfo) {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == BookRoomFragment.class.getName())
			return;

		setTitle("Reserve Room");

		if(isReserveRoom) {
			unselectHotels();
			
			// removeFragment(m_Fragment);
			for (int i = 0; i < aryFragList.size(); i++) {
				removeFragment(aryFragList.get(i));
			}
			aryFragList.clear();
			aryTitle.clear();
		} else {
			hideFragment(m_Fragment);
		}
		
		m_Fragment = new BookRoomFragment();
		((BookRoomFragment)m_Fragment).setItemInfo(itemInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goBookItem(boolean isReserveRoom, DepartInfo departInfo, ItemInfo itemInfo) {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == BookItemFragment.class.getName())
			return;

		setTitle("Book Item");

		if(isReserveRoom) {
			unselectHotels();
			
			// removeFragment(m_Fragment);
			for (int i = 0; i < aryFragList.size(); i++) {
				removeFragment(aryFragList.get(i));
			}
			aryFragList.clear();
			aryTitle.clear();
		} else {
			hideFragment(m_Fragment);
		}
		
		m_Fragment = new BookItemFragment();
		((BookItemFragment)m_Fragment).setItemInfo(departInfo, itemInfo);
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goExtendStay() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == ExtendStayFragment.class.getName())
			return;

		setTitle("Extend Stay");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new ExtendStayFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goFavourites() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == FavouritesFragment.class.getName()) {
			
			((FavouritesFragment)m_Fragment).loadFavourites();
			return;
		}

		setTitle("Favourites");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new FavouritesFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}

	public void goFavourites_Tab() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == FavouritesFragment_Tab.class.getName()) {
			
			((FavouritesFragment_Tab)m_Fragment).loadFavourites();
			return;
		}

		setTitle("Favourites");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new FavouritesFragment_Tab();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	public void goUpdateProfile() {
		
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == UpdateProfileFragment.class.getName())
			return;

		setTitle("Update Profile");
		unselectHotels();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();
		
		m_Fragment = new UpdateProfileFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}

	public void goRoomBill()
	{
		if(m_Fragment != null && 
				m_Fragment.getClass().getName() == RoomBillListFragment.class.getName())
			return;
		
		hideFragment(m_Fragment);

		m_Fragment = new RoomBillListFragment();
		
		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		setTitle("My Room Bill");
		aryTitle.add(tvHotelHeaderName.getText().toString());
	}
	
	private void loadHotelList() {
		
		aryHotelList.clear();
		HotelInfo info = new HotelInfo();
		info.id = -1;
		info.hotel_name = "Welcome";
		info.hotel_desc = "Welcome";
		aryHotelList.add(info);
		
		updateHotelList();
		
		if (NetworkUtils.haveInternet(this)) {
			
			//. https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetDepartments.php?hotel_id=6759
			// http://www.appyorder.com/pro_version/webservice_smart_app/new/getGeoHotels.php?lat=37&long=-122&rad=30000&t=1
			// https://www.appyorder.com/pro_version/webservice_smart_app/new/loginBrand.php?email_id=test@test.com&password=123
			RequestParams params = new RequestParams();
			params.add("email_id", PrefValue.getString(AllowedHotels.this, R.string.pref_customer_email_id));
			params.add("password", Base64.encodeToString(PrefValue.getString(AllowedHotels.this, R.string.pref_customer_pwd).getBytes(), Base64.NO_WRAP));
			/*Base64.encodeToString(edt_password.getText().toString().getBytes(), Base64.NO_WRAP));*/
			
			/*params.add("email_id", "demo@appybite.com");
			params.add("password", Base64.encodeToString("demo".getBytes(), Base64.NO_WRAP));*/
			
			DialogUtils.launchProgress(this, "Please wait while loading Data");
			CustomerHttpClient.get("new/loginBrand.php", params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(AllowedHotels.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {
	            		String result = new String(response);
	            		result.replace("\n", "");
	            		result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = (JSONObject) new JSONObject(result);
						String status = jsonObject.getString("status");
						if(status.equalsIgnoreCase("true"))
						{
							JSONObject jsonAllowed = jsonObject.getJSONObject("allowed hotel list");
							String isExistAllowed = (String)jsonAllowed.getString("status");
							if (isExistAllowed.equalsIgnoreCase("true")) {
								
								JSONObject hotels = jsonAllowed.getJSONObject("hotels");
								
								JSONArray hotelProArray = hotels.getJSONArray("pro");
			            		for (int i = 0; i < hotelProArray.length(); i++) {
									
			            			HotelInfo item = new HotelInfo();
			            			
			            			JSONObject object = hotelProArray.getJSONObject(i);
			            			item.id = 2;
			            			item.hotel_id = object.getInt("id");
			            			item.hotel_desc = object.getString("hotel_desc");
			            			item.hotel_logo = object.getString("hotel_logo");
			            			item.hotel_name = object.getString("hotel_name");
			            			item.hotel_bg = object.getString("hotel_background");
			            			item.license = object.getString("pro");
			            			
			            			aryHotelList.add(item);
			            		}
			            		
			            		JSONArray hotelDemoArray = hotels.getJSONArray("demo");
			            		for (int i = 0; i < hotelDemoArray.length(); i++) {
									
			            			HotelInfo item = new HotelInfo();
			            			
			            			JSONObject object = hotelDemoArray.getJSONObject(i);
			            			item.id = 2;
			            			item.hotel_id = object.getInt("id");
			            			item.hotel_desc = object.getString("hotel_desc");
			            			item.hotel_logo = object.getString("hotel_logo");
			            			item.hotel_name = object.getString("hotel_name");
			            			item.hotel_bg = object.getString("hotel_background");
			            			item.license = object.getString("demo");
			            			
			            			aryHotelList.add(item);
			            		}
							} else {
								Toast.makeText(AllowedHotels.this, jsonAllowed.getString("message"), Toast.LENGTH_LONG).show();
							}
						}
	            		
						PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_logo, "welcome");
						PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_bg, "welcome");
						PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_first, "welcome");
						PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_name, "AppyBite World!");
						
	            		if(DeviceUtil.isTabletByRes(AllowedHotels.this)) {
	            			ImageLoader.getInstance().displayImage(PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_bg), ivHotelBg, optionsForBg, animateFirstListener);
	            			ImageLoader.getInstance().displayImage(PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_logo), ivHotelLogo, null, animateFirstListener);
	            			tvHotelName.setText(PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_name));
	            		} else {
		            		if(m_Fragment != null && m_Fragment.getClass().getName() == HotelHomeFragment.class.getName())
		            			((HotelHomeFragment)m_Fragment).loadHotelInfo();
	            		}
	            		
	            		updateHotelList();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(AllowedHotels.this, "Invalid Data",Toast.LENGTH_LONG).show();
					} 
	            }
	        });

		} else {
			Toast.makeText(AllowedHotels.this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}
	
	
	private void updateHotelList()
	{
		llHotelList.removeAllViews();
		
		for (int i = 0; i < aryHotelList.size(); i++) {

			final HotelInfo value = aryHotelList.get(i);

			LayoutInflater vi = (LayoutInflater) getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			
			View item = null;
			if(DeviceUtil.isTabletByRes(this))
				item = vi.inflate(R.layout.item_hotel_tab, llHotelList, false);
			else
				item = vi.inflate(R.layout.item_hotel, llHotelList, false);
			
			ImageView ivThumb = (ImageView)item.findViewById(R.id.ivThumb);
			if(value.id == -1) {
				ivThumb.setImageResource(R.drawable.home_hotel_bg);
			} else {
				ImageLoader.getInstance().displayImage(value.hotel_logo, ivThumb, options, animateFirstListener);
			}
			
			TextView tvTitle = (TextView)item.findViewById(R.id.tvTitle);
			tvTitle.setText(value.hotel_name);
			tvTitle.setSelected(true);
			if(i == 0)
				tvTitle.setTextColor(getResources().getColor(R.color.Goldenrod));

			item.setTag(tvTitle);

			item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {

					unselectHotels();
					
					((TextView)arg0.getTag()).setTextColor(getResources().getColor(R.color.Goldenrod));
					
					if (value.id == -1) {
						PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_id, "-1");
						if(DeviceUtil.isTabletByRes(AllowedHotels.this))
							goHome_Tab();
						else
							goHome();
					} else {
						PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_id, String.valueOf(value.hotel_id));
						goHotel(value);
					}
				}
			});
			
			if (!PRJFUNC.DEFAULT_SCREEN) {
				
				PRJFUNC.mGrp.relayoutView(item, LayoutLib.LP_LinearLayout);
				PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
				PRJFUNC.mGrp.repaddingView(tvTitle);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
			}
			llHotelList.addView(item);
		}
	}
	
	private void unselectHotels()
	{
		for (int j = 0; j < llHotelList.getChildCount(); j++) {
			
			View item = llHotelList.getChildAt(j);
			TextView tvTitle = (TextView)item.getTag();
			tvTitle.setTextColor(getResources().getColor(R.color.White));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
			
			Bundle extras = data.getExtras();
			String qrcode = extras.getString("SCAN_RESULT");
			Log.i("Scanner", "QRcode: " + qrcode);

			String order_type = PrefValue.getString(this, R.string.pref_order_type);
			
			 if (NetworkUtils.haveInternet(this)) {
				 checkQRCode(order_type, qrcode);
			 } else {

			 }
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void checkQRCode(final String order_type, String qrcode) {
		
		if (NetworkUtils.haveInternet(this)) {
			
			//. Restaurant : 	http://www.appyorder.com/appybiteRestaurant/qrservice/Xqrservice.php?type=st&qrt=%s
			//. Room : 			http://www.appyorder.com/appybiteRestaurant/qrservice/Xqrservice.php?type=st&qr=%s
			String url = "http://www.appyorder.com/appybiteRestaurant/qrservice/Xqrservice.php";
			RequestParams params = new RequestParams();
			params.add("type", "st");
			if(order_type.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
				params.add("qrt", qrcode);
			} else {
				params.add("qr", qrcode);
			}
			
			DialogUtils.launchProgress(this, "Please wait while checking QRCode");
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				
				String no = "";
				
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
        			if(no.length() > 0)
        				getDetailsFromQRCode(order_type, no);

					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(AllowedHotels.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {

		            	/*
						{"result":"true","hotelID":"6759","roomID":"4"}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("result");
	            		if(status.equalsIgnoreCase("true")) {
	            			
	            			String qrHotelID = jsonObject.getString("hotelID");
	            			String hotel_id = PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_id);
	            			if(qrHotelID.equalsIgnoreCase(hotel_id)) {
	            				
	            				if(order_type.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
	            					if(jsonObject.has("tableID"))
	            						no = jsonObject.getString("tableID");
	            					else
	            						MessageBox.OK(AllowedHotels.this, "QRCode Error", "You scanned Room QRCode.\nPlease select correct order type before scan");
	            				} else {

	            					if(jsonObject.has("roomID"))
	            						no = jsonObject.getString("roomID");
	            					else
	            						MessageBox.OK(AllowedHotels.this, "QRCode Error", "You scanned Table QRCode.\nPlease select correct order type before scan");
	            				}
	            			} else {
	            				
	            				MessageBox.OK(AllowedHotels.this, "QRCode Error", "You scanned QRCode of different Hotel\nPlease scan QRCode of this hotel");
	            			}
	            		} else {
	            			
	            			MessageBox.OK(AllowedHotels.this, "QRCode Error", "This QRCode has been expired");
	            		}
	            		
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(AllowedHotels.this, "Invalid Data",Toast.LENGTH_LONG).show();
					} 
	            }
	        });

		} else {
			Toast.makeText(AllowedHotels.this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}
	
	private void getDetailsFromQRCode(final String order_type, String no) {
		
		if (NetworkUtils.haveInternet(this)) {
			
			//. Table : http://www.roomallocator.com/appybiteRestaurant/qrservice/qrservice.php?t=id&tid=1
			//. Room : http://www.roomallocator.com/appybiteRestaurant/qrservice/qrservice.php?t=id&rid=1
			String url = "http://www.roomallocator.com/appybiteRestaurant/qrservice/qrservice.php";
			RequestParams params = new RequestParams();
			params.add("t", "id");
			if(order_type.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
				params.add("tid", no);
			} else {
				params.add("rid", no);
			}
			
			DialogUtils.launchProgress(this, "Please wait while loading QRCode Details");
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(AllowedHotels.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {

		            	/*
						table : {"result":[{"table_id":"1","table_code":"1","type":"","status":"1","no_of_persons":"4"}]}
						room : {"result":[{"room_id":"1","room_no":"e212","room_name":"royale","room_type":"class a","no_of_beds":"2","room_desc":"dfhdghdhd"}]}
		            	*/

	            		String result = new String(response);
	            		result = result.replace("({", "{");
	            		result = result.replace("})", "}");
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	    				JSONArray array = jsonObject.getJSONArray("result");
	    				if(array.length() > 0)
	    				{
	    					String code = "";
	    					if(order_type.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant))
	    					{
	    						code = array.getJSONObject(0).getString("table_code");
	    					}
	    					else
	    					{
	    						code = array.getJSONObject(0).getString("room_no");
	    					}
	    					
		    				if(m_Fragment != null && 
		    						m_Fragment.getClass().getName() == ItemDetailsFragment.class.getName())
		    				{
		    					PrefValue.setString(AllowedHotels.this, R.string.pref_order_code, code);
		    					((ItemDetailsFragment)m_Fragment).addToCart();
		    					
		    					return;
		    				}
	    				}

	    				MessageBox.OK(AllowedHotels.this, "QRCode Error", "Error occured while loading QRCode Details");

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(AllowedHotels.this, "Invalid Data",Toast.LENGTH_LONG).show();
					} 
	            }
	        });

		} else {
			Toast.makeText(AllowedHotels.this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy() {
		
		instance = null;
		
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();

		super.onDestroy();
	}
	
	static AllowedHotels instance = null;
	public static AllowedHotels getInstance()
	{
		return instance; 
	}
	
	private void setTitle(String title) {
		
		tvHotelHeaderName.setText(title);
		
		if(title.compareTo("Welcome") == 0) {
			btBack.setVisibility(View.INVISIBLE);
			String userName = PrefValue.getString(this, R.string.pref_customer_name);
			tvHotelHeaderName.setText(userName);
		} else {
			btBack.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onResume() {

		if(DeviceUtil.isTabletByRes(this))
			showCurrentTime();
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		
		if(DeviceUtil.isTabletByRes(this))
			timerTime.cancel();
		
		super.onPause();
	}
	
	Timer timerTime;
	int curYear, curMonth, curDay, curHour, curMinute, curNoon, curSecond;
	Calendar c;
	String noon = "";
	Date curMillis;
	private final Handler handler = new Handler();

	private void showCurrentTime() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
//				Log.d(tag, curYear + "." + curMonth + "." + curDay + "."
//						+ curHour + ":" + curMinute + "." + curSecond);
				Update();
			}
		};
		timerTime = new Timer();
		timerTime.schedule(task, 0, 60*1000);
	}

	protected void Update() {
		c = Calendar.getInstance();
		curMillis = c.getTime();
		curYear = c.get(Calendar.YEAR);
		curMonth = c.get(Calendar.MONTH) + 1;
		curDay = c.get(Calendar.DAY_OF_MONTH);
		curHour = c.get(Calendar.HOUR_OF_DAY);
		curNoon = c.get(Calendar.AM_PM);
		if (curNoon == 0) {
			noon = "AM";
		} else {
			noon = "PM";
			curHour -= 12;
		}
		curMinute = c.get(Calendar.MINUTE);
		curSecond = c.get(Calendar.SECOND);

		Runnable updater = new Runnable() {
			public void run() {
				tvTime.setText(String.format("%02d:%02d", curHour, curMinute));
			}
		};
		handler.post(updater);
	}

	private void loadHotelInfo() {
		
		if (NetworkUtils.haveInternet(this)) {
			
			//. http://www.roomallocator.com/appcreator/services/hoteldetails.php?hotel_id=18
			String url = "http://www.roomallocator.com/appcreator/services/hoteldetails.php";
			
			String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);
			
			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);
			
			DialogUtils.launchProgress(this, "Please wait while loading Hotel Info");
			CustomerHttpClient.getFromFullService(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onFinish() {
					
					DialogUtils.exitProgress();
					
					super.onFinish();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,	byte[] errorResponse, Throwable e) {
					
					Toast.makeText(AllowedHotels.this, "Connection was lost (" + statusCode + ")", Toast.LENGTH_LONG).show();
					super.onFailure(statusCode, headers, errorResponse, e);
				}
				
	            @Override
	            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
	                // Pull out the first event on the public timeline
	            	try {

		            	/*
						{
						    "status": "true",
						    "data": {
						        "id": "8",
						        "hotel": "agaza",
						        "logo": "http://www.roomallocator.com/appcreator/uploads/66767_539906172757012_1841677721_n.jpg",
						        "rooms": "http://www.roomallocator.com/appcreator/uploads/",
						        "resturantimg": "http://www.roomallocator.com/appcreator/uploads/"
						    }
						}
		            	*/

	            		String result = new String(response);
	            		Log.i("HTTP Response <<<", result);
	            		JSONObject jsonObject = new JSONObject(result);
	            		String status = jsonObject.getString("status");
	            		if(status.equalsIgnoreCase("true")) {
	            			
		            		JSONObject data = jsonObject.getJSONObject("data");
		            		PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_name, data.getString("hotel"));
		            		PrefValue.setString(AllowedHotels.this, R.string.pref_hotel_logo, data.getString("logo"));
		            		
		            		if(DeviceUtil.isTabletByRes(AllowedHotels.this)) {
		            			ImageLoader.getInstance().displayImage(PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_bg), ivHotelBg, optionsForBg, animateFirstListener);
		            			ImageLoader.getInstance().displayImage(PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_logo), ivHotelLogo, null, animateFirstListener);
		            			tvHotelName.setText(PrefValue.getString(AllowedHotels.this, R.string.pref_hotel_name));
		            		} else {
			            		if(m_Fragment != null && m_Fragment.getClass().getName() == HotelHomeFragment.class.getName())
			            			((HotelHomeFragment)m_Fragment).loadHotelInfo();
		            		}
	            		} else {
	            			Toast.makeText(AllowedHotels.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
	            		}
	            		
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast.makeText(AllowedHotels.this, "Invalid Data",Toast.LENGTH_LONG).show();
					} 
	            }
	        });

		} else {
			Toast.makeText(AllowedHotels.this, "No Internet Connection",Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onInitializationFailure(Provider arg0,
			YouTubeInitializationResult arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer arg1,
			boolean arg2) {
		// TODO Auto-generated method stub
		
	}

}
