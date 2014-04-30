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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appybite.customer.db.MessageDatabase;
import com.appybite.customer.info.AllDepartInfo;
import com.appybite.customer.info.CategoryInfo;
import com.appybite.customer.info.DepartInfo;
import com.appybite.customer.info.ItemInfo;
import com.appybite.customer.weather.Conversor;
import com.appybite.customer.weather.Values;
import com.appybite.customer.weather.Weather;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
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

public class MainActivity extends SlidingFragmentActivity implements
		OnInitializedListener {

	private Fragment m_Fragment;
	private ArrayList<DepartInfo> aryDepartList = new ArrayList<DepartInfo>();
	private DisplayImageOptions options, optionsForBg, optionsMenuHouse,
			optionsMenuSpa, optionsMenuNews, optionsMenuTour;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private ArrayList<AllDepartInfo> aryAllDepartList = new ArrayList<AllDepartInfo>();

	// ///////////////////////////////////////////////////////////////////////////
	// . Controls
	private TextView tvDepartName;
	private Button btBack, btCart, btMenu, btMsg, btWeather;
	private LinearLayout llDepartList;

	// ///////////////////////////////////////////////////////////////////////////
	// . Tab Controls
	private ImageView ivHotelLogo, ivHotelBg;
	private TextView tvHotelName, tvTime, tvWeather;

	private ArrayList<Fragment> aryFragList = new ArrayList<Fragment>();
	private ArrayList<String> aryTitle = new ArrayList<String>();

	// . YouTube
	YouTubePlayerSupportFragment youTubePlayerFragment;

	private boolean isDemo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!DeviceUtil.isTabletByRes(this))
			setContentView(R.layout.activity_main);
		else
			setContentView(R.layout.activity_main_tab);

		setBehindContentView(R.layout.menu_frame);

		isDemo = PrefValue.getBoolean(this, R.string.pref_app_demo);

		updateLCD();
		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
		}

		if (DeviceUtil.isTabletByRes(this))
			goHome_Tab();
		else
			goHome();

		if (isDemo)
			loadHotelInfo();
		else
			loadDepartList();

		loadWeather();

		initSlidingMenu(savedInstanceState);

		instance = this;

		// String strMsg = String.format("isTab = " +
		// DeviceUtil.isTabletByRes(this) +
		// "\nScale: " + PRJFUNC.X_Z + " x " + PRJFUNC.Y_Z +
		// "\nDPI: " + PRJFUNC.DPI +
		// "\nWIDTH: " + PRJFUNC.W_LCD +
		// "\nHEIGHT: " + PRJFUNC.H_LCD
		// );
		// MessageBox.OK(this, "", strMsg);
	}

/*	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		int ot = getResources().getConfiguration().orientation;
		switch (ot) {
		case Configuration.ORIENTATION_LANDSCAPE:
			setContentView(R.layout.activity_main_tab);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			setContentView(R.layout.activity_main);
			break;
		}
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean mIsFinish = false;

	// BACK key handler
	private Handler backKeyHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				mIsFinish = false;
		}
	};

	@Override
	public void onBackPressed() {

		if (aryFragList.size() == 0
				|| (m_Fragment != null && m_Fragment.getClass().getName() == HomeFragment.class
						.getName())) {

			if (!mIsFinish) {
				mIsFinish = true;
				Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT)
						.show();
				backKeyHandler.sendEmptyMessageDelayed(0, 2000);
				return;
			}

			super.onBackPressed();
		} else if (aryFragList.size() == 1) {

			if (DeviceUtil.isTabletByRes(this))
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

	public void initSlidingMenu(Bundle savedInstanceState) {
		Fragment mFrag;

		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager()
					.beginTransaction();
			mFrag = new MenuFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (MenuFragment) this.getSupportFragmentManager()
					.findFragmentById(R.id.menu_frame);
		}

		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.RIGHT);
		// sm.setShadowWidthRes(R.dimen.margin);
		sm.setShadowWidth((int) (getResources().getDimensionPixelOffset(
				R.dimen.margin) * LayoutLib.X_Z));
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		// sm.setShadowDrawable(R.drawable.shadow);
		// sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		int width = (int) (getResources().getDimensionPixelOffset(
				R.dimen.slidingmenu_offset) * LayoutLib.X_Z);
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
				.cacheInMemory(false).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsMenuHouse = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.housekeeping)
				.showImageForEmptyUri(R.drawable.housekeeping)
				.showImageOnFail(R.drawable.housekeeping).cacheInMemory(false)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsMenuSpa = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.massage)
				.showImageForEmptyUri(R.drawable.massage)
				.showImageOnFail(R.drawable.massage).cacheInMemory(false)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsMenuNews = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.news)
				.showImageForEmptyUri(R.drawable.news)
				.showImageOnFail(R.drawable.news).cacheInMemory(false)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsMenuTour = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.tour)
				.showImageForEmptyUri(R.drawable.tour)
				.showImageOnFail(R.drawable.tour).cacheInMemory(false)
				.cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();

		optionsForBg = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.bg_default_restaurant)
				.showImageForEmptyUri(R.drawable.bg_default_restaurant)
				.showImageOnFail(R.drawable.bg_default_restaurant)
				.cacheInMemory(false).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();

		tvDepartName = (TextView) findViewById(R.id.tvDepartName);
		tvDepartName.setSelected(true);

		btBack = (Button) findViewById(R.id.btBack);
		btBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				onBackPressed();
			}
		});

		btWeather = (Button) findViewById(R.id.btWeather);
		btWeather.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				/*
				 * if(isDemo) { MessageBox.OK(MainActivity.this, "Alert",
				 * "Demo version doesn't provide Weather"); return; }
				 */

				goWeather();
			}
		});

		btMsg = (Button) findViewById(R.id.btMsg);
		updateMsgButton();
		btMsg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (isDemo) {
					MessageBox.OK(MainActivity.this, "Alert",
							"Demo version doesn't provide Message In-Box");
					return;
				}

				goMsg();
			}
		});
		btCart = (Button) findViewById(R.id.btCart);
		btCart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				/*
				 * if(isDemo) { MessageBox.OK(MainActivity.this, "Alert",
				 * "Demo version doesn't provide Cart"); return; }
				 */

				if (DeviceUtil.isTabletByRes(MainActivity.this))
					goCart_Tab();
				else
					goCart();
			}
		});
		btMenu = (Button) findViewById(R.id.btMenu);
		btMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				getSlidingMenu().showMenu();
			}
		});
		llDepartList = (LinearLayout) findViewById(R.id.llDepartList);

		if (DeviceUtil.isTabletByRes(this)) {
			ivHotelLogo = (ImageView) findViewById(R.id.ivHotelLogo);
			ivHotelBg = (ImageView) findViewById(R.id.ivHotelBg);
			tvHotelName = (TextView) findViewById(R.id.tvHotelName);
			tvTime = (TextView) findViewById(R.id.tvTime);
			tvTime.setText("0:00");
			tvWeather = (TextView) findViewById(R.id.tvWeather);
			tvWeather.setText("");

			ImageLoader.getInstance().displayImage(
					PrefValue.getString(this, R.string.pref_hotel_bg),
					ivHotelBg, optionsForBg, animateFirstListener);
			ImageLoader.getInstance().displayImage(
					PrefValue.getString(this, R.string.pref_hotel_logo),
					ivHotelLogo, null, animateFirstListener);
			tvHotelName.setText(PrefValue.getString(this,
					R.string.pref_hotel_name));
		}

		setTitle("Welcome");
		showMessagePopup();
	}

	private void scaleView() {

		if (PRJFUNC.mGrp == null) {
			return;
		}

		RelativeLayout rlTopMenu = (RelativeLayout) findViewById(R.id.rlTopMenu);
		PRJFUNC.mGrp.relayoutView(rlTopMenu, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDepartName);
		PRJFUNC.mGrp.setButtonFontScale(btMsg);
		PRJFUNC.mGrp.relayoutView(btMsg, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(btCart, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(btMenu, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(llDepartList, LayoutLib.LP_FrameLayout);

		PRJFUNC.mGrp.relayoutView(btWeather, LayoutLib.LP_RelativeLayout);

		PRJFUNC.mGrp.relayoutView(findViewById(R.id.fl_fragment_youtube),
				LayoutLib.LP_RelativeLayout);

		if (DeviceUtil.isTabletByRes(this)) {

			RelativeLayout rlLogo = (RelativeLayout) findViewById(R.id.rlLogo);
			PRJFUNC.mGrp.relayoutView(rlLogo, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(ivHotelLogo, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(ivHotelBg, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(tvHotelName, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvHotelName);
			TextView tvWelcome = (TextView) findViewById(R.id.tvWelcome);
			PRJFUNC.mGrp.relayoutView(findViewById(R.id.tvWelcome),
					LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvWelcome);
			PRJFUNC.mGrp.relayoutView(tvTime, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvTime);
			PRJFUNC.mGrp.relayoutView(tvWeather, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvWeather);
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

	public void goHome() {
		setTitle("Welcome");

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == HomeFragment.class
						.getName())
			return;

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new HomeFragment();
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

	public void goDepart(DepartInfo departInfo) {
		// if(m_Fragment != null &&
		// m_Fragment.getClass().getName() == DepartFragment.class.getName()) {
		//
		// ((DepartFragment)m_Fragment).loadCategoryList();
		// return;
		// }

		setTitle(departInfo.title);

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		if (DeviceUtil.isTabletByRes(this)) {

			m_Fragment = new DepartFragment_Tab();
			((DepartFragment_Tab) m_Fragment).setDepartInfo(departInfo);
		} else {
			m_Fragment = new DepartFragment();
			((DepartFragment) m_Fragment).setDepartInfo(departInfo);
		}

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goSubCategory(DepartInfo departInfo, CategoryInfo categoryInfo) {
		setTitle(categoryInfo.name);

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == SubCategoryFragment.class
						.getName())
			return;

		hideFragment(m_Fragment);

		m_Fragment = new SubCategoryFragment();
		((SubCategoryFragment) m_Fragment).setDepartInfo(departInfo,
				categoryInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goSubCategory_Tab(DepartInfo departInfo,
			CategoryInfo categoryInfo) {
		setTitle(categoryInfo.name);

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == SubCategoryFragment_Tab.class
						.getName())
			return;

		hideFragment(m_Fragment);

		m_Fragment = new SubCategoryFragment_Tab();
		((SubCategoryFragment_Tab) m_Fragment).setDepartInfo(departInfo,
				categoryInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goItemList(DepartInfo departInfo, CategoryInfo subCatInfo) {
		if (subCatInfo == null)
			setTitle(departInfo.title);
		else
			setTitle(subCatInfo.name);

		hideFragment(m_Fragment);

		m_Fragment = new ItemListFragment();
		((ItemListFragment) m_Fragment).setDepartInfo(departInfo, subCatInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goItemList_Tab(DepartInfo departInfo, CategoryInfo subCatInfo) {
		if (subCatInfo == null)
			setTitle(departInfo.title);
		else
			setTitle(subCatInfo.name);

		hideFragment(m_Fragment);

		m_Fragment = new ItemListFragment_Tab();
		((ItemListFragment_Tab) m_Fragment).setDepartInfo(departInfo,
				subCatInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void updateItemList(ItemInfo itemInfo) {
		Fragment fg = aryFragList.get(aryFragList.size() - 1);
		if (m_Fragment.getClass().getName() == ItemListFragment.class.getName()) {
			((ItemListFragment) fg).updateItem(itemInfo);
		} else if (m_Fragment.getClass().getName() == ItemListFragment_Tab.class
				.getName()) {
			((ItemListFragment_Tab) fg).updateItem(itemInfo);
		}
	}

	public void goItemDetails(DepartInfo departInfo, ItemInfo itemInfo) {
		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == ItemDetailsFragment.class
						.getName())
			return;

		hideFragment(m_Fragment);

		m_Fragment = new ItemDetailsFragment();
		((ItemDetailsFragment) m_Fragment).setDepartInfo(departInfo, itemInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goCart() {
		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == CartListFragment.class
						.getName())
			return;

		hideFragment(m_Fragment);

		m_Fragment = new CartListFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		setTitle("MyCart");
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goCart_Tab() {
		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == CartListFragment.class
						.getName())
			return;

		// hideFragment(m_Fragment);

		m_Fragment = new CartListFragment();

		// addFragment(m_Fragment);
		if (m_Fragment == null)
			return;

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(R.anim.right_in, R.anim.hold);
		ft.add(R.id.fl_fragment_corner, m_Fragment);
		ft.commit();

		aryFragList.add(m_Fragment);
		setTitle("MyCart");
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goAboutUs() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == AboutUsFragment.class
						.getName()) {

			((AboutUsFragment) m_Fragment).loadHotelInfo();
			return;
		}

		setTitle("About Us");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new AboutUsFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goMsg() {

		setAsRead();
		updateMsgButton();

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == NotificationFragment.class
						.getName()) {

			((NotificationFragment) m_Fragment).loadMessages();

			return;
		}

		setTitle("Notifications");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new NotificationFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goPrinter() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == PrinterFragment.class
						.getName()) {

			((PrinterFragment) m_Fragment).loadPrinter();
			return;
		}

		setTitle("Printer Setting");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new PrinterFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goBookTable() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == BookTableFragment.class
						.getName())
			return;

		setTitle("Book Table");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new BookTableFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goBookRoom(boolean isReserveRoom, ItemInfo itemInfo) {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == BookRoomFragment.class
						.getName())
			return;

		setTitle("Reserve Room");

		if (isReserveRoom) {
			unselectDeparts();

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
		((BookRoomFragment) m_Fragment).setItemInfo(itemInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goBookItem(boolean isReserveRoom, DepartInfo departInfo,
			ItemInfo itemInfo) {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == BookItemFragment.class
						.getName())
			return;

		setTitle("Book Item");

		if (isReserveRoom) {
			unselectDeparts();

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
		((BookItemFragment) m_Fragment).setItemInfo(departInfo, itemInfo);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goExtendStay() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == ExtendStayFragment.class
						.getName())
			return;

		setTitle("Extend Stay");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new ExtendStayFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goFavourites() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == FavouritesFragment.class
						.getName()) {

			((FavouritesFragment) m_Fragment).loadFavourites();
			return;
		}

		setTitle("Favourites");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new FavouritesFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goFavourites_Tab() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == FavouritesFragment_Tab.class
						.getName()) {

			((FavouritesFragment_Tab) m_Fragment).loadFavourites();
			return;
		}

		setTitle("Favourites");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new FavouritesFragment_Tab();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goUpdateProfile() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == UpdateProfileFragment.class
						.getName())
			return;

		setTitle("Update Profile");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new UpdateProfileFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goWeather() {

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == WeatherFragment.class
						.getName())
			return;

		setTitle("Weather");
		unselectDeparts();

		// removeFragment(m_Fragment);
		for (int i = 0; i < aryFragList.size(); i++) {
			removeFragment(aryFragList.get(i));
		}
		aryFragList.clear();
		aryTitle.clear();

		m_Fragment = new WeatherFragment();
		((WeatherFragment) m_Fragment).setWeather(weather);

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goRoomBill() {
		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == RoomBillListFragment.class
						.getName())
			return;

		hideFragment(m_Fragment);

		m_Fragment = new RoomBillListFragment();

		addFragment(m_Fragment);
		aryFragList.add(m_Fragment);
		setTitle("My Room Bill");
		aryTitle.add(tvDepartName.getText().toString());
	}

	public void goAdditionalAccount() {
		hideFragment(m_Fragment);
		Intent i = new Intent(MainActivity.this, AllowedHotels.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}

	private void loadDepartList() {

		aryDepartList.clear();
		DepartInfo info = new DepartInfo();
		info.id = -1;
		info.title = "Welcome";
		info.desc = "Welcome";
		info.image = PrefValue.getString(this, R.string.pref_hotel_bg);
		aryDepartList.add(info);
		info = new DepartInfo();
		info.id = 0;
		info.title = "Restaurant";
		info.desc = "Restaurant";
		info.isRestaurant = true;
		aryDepartList.add(info);

		updateDepartList();

		if (NetworkUtils.haveInternet(this)) {

			// .
			// https://www.appyorder.com/pro_version/webservice_smart_app/Department/GetDepartments.php?hotel_id=6759
			String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);

			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);

			DialogUtils.launchProgress(this, "Please wait while loading Data");
			CustomerHttpClient.get("Department/GetDepartments.php", params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onFinish() {

							DialogUtils.exitProgress();

							super.onFinish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

							Toast.makeText(MainActivity.this,
									"Connection was lost (" + statusCode + ")",
									Toast.LENGTH_LONG).show();
							super.onFailure(statusCode, headers, errorResponse,
									e);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							// Pull out the first event on the public timeline
							try {

								/*
								 * { "status": "true", "data": { "depart": [ {
								 * "id": "1", "title": "House Keeping", "desc":
								 * "We are here to make your stay one of the best you have ever had please ask us for anything anytime we are happy to serve"
								 * , "depimg":
								 * "http://www.roomallocator.com/appybiteRestaurant/depimages/houskeeping.jpg"
								 * } ], "hotel_image": { "hotel_logo":
								 * "http://www.roomallocator.com/appybiteRestaurant/depimages/screenshot_1039_1.jpg"
								 * , "app_header":
								 * "http://www.roomallocator.com/appybiteRestaurant/depimages/screenshot_1091_1.jpg"
								 * , "app_icon":
								 * "http://www.roomallocator.com/appybiteRestaurant/depimages/xxxx.jpg"
								 * } } }
								 */

								String result = new String(response);
								result = result.replace("({", "{");
								result = result.replace("})", "}");
								Log.i("HTTP Response <<<", result);
								JSONObject jsonObject = new JSONObject(result);
								JSONObject data = jsonObject
										.getJSONObject("data");
								JSONArray departArray = data
										.getJSONArray("depart");

								for (int i = 0; i < departArray.length(); i++) {

									DepartInfo item = new DepartInfo();

									JSONObject object = departArray
											.getJSONObject(i);
									item.id = object.getInt("id");
									item.title = object.getString("title");
									item.desc = object.getString("desc");
									item.image = object.getString("depimg");

									aryDepartList.add(item);
								}

								JSONObject hotel_image = data
										.getJSONObject("hotel_image");
								PrefValue.setString(MainActivity.this,
										R.string.pref_hotel_logo,
										hotel_image.getString("hotel_logo"));
								PrefValue.setString(MainActivity.this,
										R.string.pref_hotel_bg,
										hotel_image.getString("app_header"));
								aryDepartList.get(0).image = hotel_image
										.getString("app_header");

								if (DeviceUtil.isTabletByRes(MainActivity.this)) {
									ImageLoader.getInstance().displayImage(
											PrefValue.getString(
													MainActivity.this,
													R.string.pref_hotel_bg),
											ivHotelBg, optionsForBg,
											animateFirstListener);
									ImageLoader.getInstance().displayImage(
											PrefValue.getString(
													MainActivity.this,
													R.string.pref_hotel_logo),
											ivHotelLogo, null,
											animateFirstListener);
									tvHotelName.setText(PrefValue.getString(
											MainActivity.this,
											R.string.pref_hotel_name));
								} else {
									if (m_Fragment != null
											&& m_Fragment.getClass().getName() == HomeFragment.class
													.getName())
										((HomeFragment) m_Fragment)
												.loadHotelInfo();
								}

								updateDepartList();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(MainActivity.this,
										"Invalid Data", Toast.LENGTH_LONG)
										.show();
							}
						}
					});

		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void loadRestaurantList() {

		aryDepartList.clear();
		DepartInfo info = new DepartInfo();
		info.id = -1;
		info.title = "Welcome";
		info.desc = "Welcome";
		info.image = PrefValue.getString(MainActivity.this,
				R.string.pref_hotel_bg);
		aryDepartList.add(info);

		updateDepartList();

		if (NetworkUtils.haveInternet(this)) {

			// .
			// http://www.roomallocator.com/appcreator/services/hotelresturant.php?hotel_id=18
			String url = "http://www.roomallocator.com/appcreator/services/hotelresturant.php";
			String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);

			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);

			DialogUtils.launchProgress(this, "Please wait while loading Data");
			CustomerHttpClient.getFromFullService(url, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onFinish() {

							loadDemoDepartments();
							super.onFinish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

							Toast.makeText(MainActivity.this,
									"Connection was lost (" + statusCode + ")",
									Toast.LENGTH_LONG).show();
							super.onFailure(statusCode, headers, errorResponse,
									e);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							// Pull out the first event on the public timeline
							try {

								/*
								 * { "status": "true", "data": [ { "id": "8",
								 * "name": "court", "description": "", "image":
								 * "http://www.roomallocator.com/appcreator/uploads/45.jpg"
								 * } ] }
								 */

								String result = new String(response);
								result = result.replace("({", "{");
								result = result.replace("})", "}");
								Log.i("HTTP Response <<<", result);
								JSONObject jsonObject = new JSONObject(result);
								JSONArray restArray = jsonObject
										.getJSONArray("data");

								for (int i = 0; i < restArray.length(); i++) {

									DepartInfo item = new DepartInfo();

									JSONObject object = restArray
											.getJSONObject(i);
									item.id = object.getInt("id");
									item.title = object.getString("name");
									item.desc = object.getString("description");
									item.image = object.getString("image");
									item.isRestaurant = true;

									aryDepartList.add(item);
								}

								if (DeviceUtil.isTabletByRes(MainActivity.this)) {
									ImageLoader.getInstance().displayImage(
											PrefValue.getString(
													MainActivity.this,
													R.string.pref_hotel_bg),
											ivHotelBg, optionsForBg,
											animateFirstListener);
									ImageLoader.getInstance().displayImage(
											PrefValue.getString(
													MainActivity.this,
													R.string.pref_hotel_logo),
											ivHotelLogo, null,
											animateFirstListener);
									tvHotelName.setText(PrefValue.getString(
											MainActivity.this,
											R.string.pref_hotel_name));
								} else {
									if (m_Fragment != null
											&& m_Fragment.getClass().getName() == HomeFragment.class
													.getName())
										((HomeFragment) m_Fragment)
												.loadHotelInfo();
								}

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(MainActivity.this,
										"Invalid Data", Toast.LENGTH_LONG)
										.show();
							}
						}
					});

		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void updateDepartList() {
		// . Department List
		llDepartList.removeAllViews();

		for (int i = 0; i < aryDepartList.size(); i++) {

			final DepartInfo value = aryDepartList.get(i);

			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View item = null;
			if (DeviceUtil.isTabletByRes(this))
				item = vi
						.inflate(R.layout.item_depart_tab, llDepartList, false);
			else
				item = vi.inflate(R.layout.item_depart, llDepartList, false);

			ImageView ivThumb = (ImageView) item.findViewById(R.id.ivThumb);
			if (isDemo) {
				if (value.id == 0) {
					ivThumb.setImageResource(R.drawable.bg_default_restaurant);
				} else if (value.id == 1) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuHouse, animateFirstListener);
				} else if (value.id == 2) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuSpa, animateFirstListener);
				} else if (value.id == 3) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuTour, animateFirstListener);
				} else if (value.id == 97) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuNews, animateFirstListener);
				} else {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, options, animateFirstListener);
				}
			} else {
				if (value.id == 0) {
					ivThumb.setImageResource(R.drawable.bg_default_restaurant);
				} else if (value.id == 1) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuHouse, animateFirstListener);
				} else if (value.id == 68) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuSpa, animateFirstListener);
				} else if (value.id == 97) {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, optionsMenuNews, animateFirstListener);
				} else {
					ImageLoader.getInstance().displayImage(value.image,
							ivThumb, options, animateFirstListener);
				}
			}

			TextView tvTitle = (TextView) item.findViewById(R.id.tvTitle);
			tvTitle.setText(value.title);
			tvTitle.setSelected(true);
			if (i == 0)
				tvTitle.setTextColor(getResources().getColor(R.color.Goldenrod));

			item.setTag(tvTitle);

			item.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					unselectDeparts();

					((TextView) arg0.getTag()).setTextColor(getResources()
							.getColor(R.color.Goldenrod));

					if (value.id == -1) {
						if (DeviceUtil.isTabletByRes(MainActivity.this))
							goHome_Tab();
						else
							goHome();
					} else if (isDemo) {
						if (DeviceUtil.isTabletByRes(MainActivity.this))
							goItemList_Tab(value, null);
						else
							goItemList(value, null);
					} else {
						goDepart(value);
					}
				}
			});

			if (!PRJFUNC.DEFAULT_SCREEN) {

				PRJFUNC.mGrp.relayoutView(item, LayoutLib.LP_LinearLayout);
				PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
				PRJFUNC.mGrp.repaddingView(tvTitle);
				PRJFUNC.mGrp.relayoutView(item.findViewById(R.id.ivShadowTop),
						LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.relayoutView(
						item.findViewById(R.id.ivShadowBottom),
						LayoutLib.LP_RelativeLayout);
			}
			llDepartList.addView(item);
		}
	}

	private void unselectDeparts() {
		for (int j = 0; j < llDepartList.getChildCount(); j++) {

			View item = llDepartList.getChildAt(j);
			TextView tvTitle = (TextView) item.getTag();
			tvTitle.setTextColor(getResources().getColor(R.color.White));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 101 && resultCode == Activity.RESULT_OK) {

			Bundle extras = data.getExtras();
			String qrcode = extras.getString("SCAN_RESULT");
			Log.i("Scanner", "QRcode: " + qrcode);

			String order_type = PrefValue.getString(this,
					R.string.pref_order_type);

			if (NetworkUtils.haveInternet(this)) {
				checkQRCode(order_type, qrcode);
			} else {

			}
		} else if (requestCode == RECOVERY_DIALOG_REQUEST) {
			// Retry initialization if user performed a recovery action
			if (youTubePlayerFragment != null)
				youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY,
						this);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void checkQRCode(final String order_type, String qrcode) {

		if (NetworkUtils.haveInternet(this)) {

			// . Restaurant :
			// http://www.appyorder.com/appybiteRestaurant/qrservice/Xqrservice.php?type=st&qrt=%s
			// . Room :
			// http://www.appyorder.com/appybiteRestaurant/qrservice/Xqrservice.php?type=st&qr=%s
			String url = "http://www.appyorder.com/appybiteRestaurant/qrservice/Xqrservice.php";
			RequestParams params = new RequestParams();
			params.add("type", "st");
			if (order_type
					.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
				params.add("qrt", qrcode);
			} else {
				params.add("qr", qrcode);
			}

			DialogUtils.launchProgress(this,
					"Please wait while checking QRCode");
			CustomerHttpClient.getFromFullService(url, params,
					new AsyncHttpResponseHandler() {

						String no = "";

						@Override
						public void onFinish() {

							DialogUtils.exitProgress();

							if (no.length() > 0)
								getDetailsFromQRCode(order_type, no);

							super.onFinish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

							Toast.makeText(MainActivity.this,
									"Connection was lost (" + statusCode + ")",
									Toast.LENGTH_LONG).show();
							super.onFailure(statusCode, headers, errorResponse,
									e);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							// Pull out the first event on the public timeline
							try {

								/*
								 * {"result":"true","hotelID":"6759","roomID":"4"
								 * }
								 */

								String result = new String(response);
								result = result.replace("({", "{");
								result = result.replace("})", "}");
								Log.i("HTTP Response <<<", result);
								JSONObject jsonObject = new JSONObject(result);
								String status = jsonObject.getString("result");
								if (status.equalsIgnoreCase("true")) {

									String qrHotelID = jsonObject
											.getString("hotelID");
									String hotel_id = PrefValue.getString(
											MainActivity.this,
											R.string.pref_hotel_id);
									if (qrHotelID.equalsIgnoreCase(hotel_id)) {

										if (order_type
												.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
											if (jsonObject.has("tableID"))
												no = jsonObject
														.getString("tableID");
											else
												MessageBox
														.OK(MainActivity.this,
																"QRCode Error",
																"You scanned Room QRCode.\nPlease select correct order type before scan");
										} else {

											if (jsonObject.has("roomID"))
												no = jsonObject
														.getString("roomID");
											else
												MessageBox
														.OK(MainActivity.this,
																"QRCode Error",
																"You scanned Table QRCode.\nPlease select correct order type before scan");
										}
									} else {

										MessageBox
												.OK(MainActivity.this,
														"QRCode Error",
														"You scanned QRCode of different Hotel\nPlease scan QRCode of this hotel");
									}
								} else {

									MessageBox.OK(MainActivity.this,
											"QRCode Error",
											"This QRCode has been expired");
								}

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(MainActivity.this,
										"Invalid Data", Toast.LENGTH_LONG)
										.show();
							}
						}
					});

		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void getDetailsFromQRCode(final String order_type, String no) {

		if (NetworkUtils.haveInternet(this)) {

			// . Table :
			// http://www.roomallocator.com/appybiteRestaurant/qrservice/qrservice.php?t=id&tid=1
			// . Room :
			// http://www.roomallocator.com/appybiteRestaurant/qrservice/qrservice.php?t=id&rid=1
			String url = "http://www.roomallocator.com/appybiteRestaurant/qrservice/qrservice.php";
			RequestParams params = new RequestParams();
			params.add("t", "id");
			if (order_type
					.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
				params.add("tid", no);
			} else {
				params.add("rid", no);
			}

			DialogUtils.launchProgress(this,
					"Please wait while loading QRCode Details");
			CustomerHttpClient.getFromFullService(url, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onFinish() {

							DialogUtils.exitProgress();
							super.onFinish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

							Toast.makeText(MainActivity.this,
									"Connection was lost (" + statusCode + ")",
									Toast.LENGTH_LONG).show();
							super.onFailure(statusCode, headers, errorResponse,
									e);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							// Pull out the first event on the public timeline
							try {

								/*
								 * table :
								 * {"result":[{"table_id":"1","table_code"
								 * :"1","type"
								 * :"","status":"1","no_of_persons":"4"}]} room
								 * : {"result":[{"room_id":"1","room_no":"e212",
								 * "room_name"
								 * :"royale","room_type":"class a","no_of_beds"
								 * :"2","room_desc":"dfhdghdhd"}]}
								 */

								String result = new String(response);
								result = result.replace("({", "{");
								result = result.replace("})", "}");
								Log.i("HTTP Response <<<", result);
								JSONObject jsonObject = new JSONObject(result);
								JSONArray array = jsonObject
										.getJSONArray("result");
								if (array.length() > 0) {
									String code = "";
									if (order_type
											.equalsIgnoreCase(ItemDetailsFragment.order_inrestaurant)) {
										code = array.getJSONObject(0)
												.getString("table_code");
									} else {
										code = array.getJSONObject(0)
												.getString("room_no");
									}

									if (m_Fragment != null
											&& m_Fragment.getClass().getName() == ItemDetailsFragment.class
													.getName()) {
										PrefValue.setString(MainActivity.this,
												R.string.pref_order_code, code);
										((ItemDetailsFragment) m_Fragment)
												.addToCart();

										return;
									}
								}

								MessageBox
										.OK(MainActivity.this, "QRCode Error",
												"Error occured while loading QRCode Details");

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(MainActivity.this,
										"Invalid Data", Toast.LENGTH_LONG)
										.show();
							}
						}
					});

		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void updateMsgButton() {

		int cnt = getUnreadMsg();

		if (cnt > 0) {
			btMsg.setText(String.valueOf(cnt));
			btMsg.setBackgroundResource(R.drawable.btn_msg_on);
		} else {
			btMsg.setText("");
			btMsg.setBackgroundResource(R.drawable.btn_msg_off);
		}
	}

	private int getUnreadMsg() {

		String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);
		String c_id = PrefValue.getString(this, R.string.pref_customer_id);

		MessageDatabase msg_db = new MessageDatabase(this);
		int cnt = msg_db.getUnreadCount(hotel_id, c_id);
		msg_db.close();

		return cnt;
	}

	private void setAsRead() {

		btMsg.setBackgroundResource(R.drawable.btn_msg_off);
		btMsg.setText("0");

		String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);
		String c_id = PrefValue.getString(this, R.string.pref_customer_id);

		MessageDatabase msg_db = new MessageDatabase(this);
		msg_db.setAsRead(hotel_id, c_id);
		msg_db.close();
	}

	@Override
	protected void onDestroy() {

		instance = null;

		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().clearMemoryCache();

		super.onDestroy();
	}

	static MainActivity instance = null;

	public static MainActivity getInstance() {
		return instance;
	}

	public static void updateGCM() {
		if (instance != null) {
			instance.updateMsgButton();
			instance.showMessagePopup();
		}
	}

	private void setTitle(String title) {

		tvDepartName.setText(title);

		if (title.compareTo("Welcome") == 0) {
			btBack.setVisibility(View.INVISIBLE);
			String userName = PrefValue.getString(this,
					R.string.pref_customer_name);
			tvDepartName.setText(userName);
		} else {
			btBack.setVisibility(View.VISIBLE);
		}
	}

	public void showMessagePopup() {

		int cnt = getUnreadMsg();
		if (cnt > 0) {
			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_newmsg);

			RelativeLayout rlNewMsg = (RelativeLayout) dialog
					.findViewById(R.id.rlNewMsg);
			rlNewMsg.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					dialog.dismiss();
					goMsg();
				}
			});

			ImageView ivNewMsg = (ImageView) dialog.findViewById(R.id.ivNewMsg);
			TextView tvNewMsgCnt = (TextView) dialog
					.findViewById(R.id.tvNewMsgCnt);
			tvNewMsgCnt.setText(String.valueOf(cnt));
			TextView tvNewMsg = (TextView) dialog.findViewById(R.id.tvNewMsg);
			tvNewMsg.setText(String.format("You have %d new message", cnt));

			if (PRJFUNC.mGrp != null) {
				RelativeLayout rlNewMsgDlg = (RelativeLayout) dialog
						.findViewById(R.id.rlNewMsgDlg);
				PRJFUNC.mGrp.relayoutView(rlNewMsgDlg,
						LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.repaddingView(rlNewMsgDlg);

				PRJFUNC.mGrp
						.relayoutView(rlNewMsg, LayoutLib.LP_RelativeLayout);

				PRJFUNC.mGrp
						.relayoutView(ivNewMsg, LayoutLib.LP_RelativeLayout);

				PRJFUNC.mGrp.relayoutView(tvNewMsgCnt,
						LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.setTextViewFontScale(tvNewMsgCnt);
				PRJFUNC.mGrp.repaddingView(tvNewMsgCnt);

				PRJFUNC.mGrp
						.relayoutView(tvNewMsg, LayoutLib.LP_RelativeLayout);
				PRJFUNC.mGrp.setTextViewFontScale(tvNewMsg);
			}

			dialog.show();
		}
	}

	private Weather weather;

	public void loadWeather() {

		if (NetworkUtils.haveInternet(this)) {

			// . http://api.openweathermap.org/data/2.5/weather?lat=35&lon=139
			String url = Values.getWeatherURL();
			RequestParams params = new RequestParams();
			params.add("lat",
					PrefValue.getString(this, R.string.pref_hotel_lat));
			params.add("lon",
					PrefValue.getString(this, R.string.pref_hotel_lon));

			// pbWeather.setVisibility(View.VISIBLE);
			CustomerHttpClient.getFromFullService(url, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onFinish() {

							// pbWeather.setVisibility(View.INVISIBLE);
							super.onFinish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

							Toast.makeText(MainActivity.this,
									"Connection was lost (" + statusCode + ")",
									Toast.LENGTH_LONG).show();
							super.onFailure(statusCode, headers, errorResponse,
									e);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							// Pull out the first event on the public timeline
							try {

								String result = new String(response);
								Log.i("HTTP Response <<<", result);
								JSONObject jsonObject = new JSONObject(result);
								weather = Conversor.jsonToWeather(jsonObject);

								updateWeather(weather);

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(MainActivity.this,
										"Invalid Data", Toast.LENGTH_LONG)
										.show();
							}
						}
					});

		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void updateWeather(Weather weather) {

		if (weather != null && weather.getData_receiving() != 0) {
			int weatherId = weather.getWeather_id();
			if (weatherId >= 200 && weatherId <= 232) {
				// . thunderstorm
				btWeather.setBackgroundResource(R.drawable.weather_rainy);
			}
			if (weatherId >= 300 && weatherId <= 321) {
				// . rain
				btWeather.setBackgroundResource(R.drawable.weather_rainy);
			}
			if (weatherId >= 500 && weatherId <= 522) {
				// . rain
				btWeather.setBackgroundResource(R.drawable.weather_rainy);
			}
			if (weatherId >= 600 && weatherId <= 621) {
				// . snow
				btWeather.setBackgroundResource(R.drawable.weather_snow);
			}
			if (weatherId >= 700 && weatherId <= 741) {
				// . mist
				btWeather.setBackgroundResource(R.drawable.weather_cloudy);
			}
			if (weatherId == 800) {
				// . clear
				btWeather.setBackgroundResource(R.drawable.weather_sunny);
			}
			if (weatherId == 801) {
				// . scattered_clouds
				btWeather
						.setBackgroundResource(R.drawable.weather_partly_cloudy);
			}
			if (weatherId == 802) {
				// . scattered_clouds
				btWeather
						.setBackgroundResource(R.drawable.weather_partly_cloudy);
			}
			if (weatherId == 803) {
				// . broken_clouds
				btWeather.setBackgroundResource(R.drawable.weather_cloudy);
			}
			if (weatherId == 804) {
				// . broken_clouds
				btWeather.setBackgroundResource(R.drawable.weather_cloudy);
			}

			if (tvWeather != null)
				tvWeather.setText(weather.getWeather_main());
		}

		if (m_Fragment != null
				&& m_Fragment.getClass().getName() == WeatherFragment.class
						.getName()) {
			((WeatherFragment) m_Fragment).setWeather(weather);
			((WeatherFragment) m_Fragment).loadWeather();
		}
	}

	@Override
	public void onResume() {

		if (DeviceUtil.isTabletByRes(this))
			showCurrentTime();

		super.onResume();
	}

	@Override
	public void onPause() {

		if (DeviceUtil.isTabletByRes(this))
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
				// Log.d(tag, curYear + "." + curMonth + "." + curDay + "."
				// + curHour + ":" + curMinute + "." + curSecond);
				Update();
			}
		};
		timerTime = new Timer();
		timerTime.schedule(task, 0, 60 * 1000);
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
				// tvCurrentTime.setText("í˜„ìž¬ ë‚ ì§œì™€ ì‹œê°„ì�€ " + curYear
				// + "ë…„ " + curMonth
				// + "ì›” " + curDay + "ì�¼ " + noon + curHour + "ì‹œ "
				// + curMinute + "ë¶„ " + curSecond + "ì´ˆ ìž…ë‹ˆë‹¤. ");
				tvTime.setText(String.format("%02d:%02d", curHour, curMinute));
			}
		};
		handler.post(updater);
	}

	String videoID;

	public void addYoutubeView(String videoID) {

		this.videoID = videoID;

		if (youTubePlayerFragment != null
				&& youTubePlayerFragment.getClass().getName() == YouTubePlayerSupportFragment.class
						.getName())
			return;

		youTubePlayerFragment = new YouTubePlayerSupportFragment();
		youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);

		FrameLayout fl = (FrameLayout) findViewById(R.id.fl_fragment_youtube);
		fl.setVisibility(View.GONE);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.fl_fragment_youtube, youTubePlayerFragment);
		ft.hide(youTubePlayerFragment);
		ft.commit();
	}

	public void showYoutubeView() {

		if (youTubePlayerFragment == null)
			return;

		FrameLayout fl = (FrameLayout) findViewById(R.id.fl_fragment_youtube);
		fl.setVisibility(View.VISIBLE);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.show(youTubePlayerFragment);
		ft.commit();

		if (player != null)
			player.play();
	}

	public void hideYoutubeView() {

		if (youTubePlayerFragment == null)
			return;

		FrameLayout fl = (FrameLayout) findViewById(R.id.fl_fragment_youtube);
		fl.setVisibility(View.GONE);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.hide(youTubePlayerFragment);
		ft.commit();

		if (player != null && player.isPlaying())
			player.pause();
	}

	public void removeYoutubeView() {

		if (youTubePlayerFragment == null)
			return;

		FrameLayout fl = (FrameLayout) findViewById(R.id.fl_fragment_youtube);
		fl.setVisibility(View.GONE);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.remove(youTubePlayerFragment);
		ft.commit();

		youTubePlayerFragment = null;
		player = null;
	}

	YouTubePlayer player = null;

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			YouTubePlayer player, boolean wasRestored) {
		if (!wasRestored) {
			// player.cueVideo("nCgQDjiotG0");
			player.cueVideo(videoID);
			player.setShowFullscreenButton(false);
			this.player = player;
		}
	}

	private static final int RECOVERY_DIALOG_REQUEST = 100;

	@Override
	public void onInitializationFailure(Provider provider,
			YouTubeInitializationResult error) {

		if (error.isUserRecoverableError()) {
			error.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
		} else {
			String errorMessage = String.format(
					"There was an error initializing the YouTubePlayer (%1$s)",
					error.toString());
			Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
		}
	}

	// private void updateBookableDepart() {
	//
	// if (NetworkUtils.haveInternet(this)) {
	//
	// //.
	// http://www.appyorder.com/pro_version/webservice_smart_app/allbookdept.php?id=6759
	// String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);
	//
	// RequestParams params = new RequestParams();
	// params.add("id", hotel_id);
	//
	// DialogUtils.launchProgress(this,
	// "Please wait while checking bookable department");
	// CustomerHttpClient.get("allbookdept.php", params, new
	// AsyncHttpResponseHandler() {
	//
	// @Override
	// public void onFinish() {
	//
	// DialogUtils.exitProgress();
	//
	// super.onFinish();
	// }
	//
	// @Override
	// public void onFailure(int statusCode, Header[] headers, byte[]
	// errorResponse, Throwable e) {
	//
	// Toast.makeText(MainActivity.this, "Connection was lost (" + statusCode +
	// ")", Toast.LENGTH_LONG).show();
	// super.onFailure(statusCode, headers, errorResponse, e);
	// }
	//
	// @Override
	// public void onSuccess(int statusCode, Header[] headers, byte[] response)
	// {
	// // Pull out the first event on the public timeline
	// try {
	//
	// /*
	// {
	// "status": true,
	// "dep": [
	// {
	// "id": "1",
	// }
	// ],
	// "msg": "success"
	// }
	// */
	//
	// String result = new String(response);
	// result = result.replace("({", "{");
	// result = result.replace("})", "}");
	// Log.i("HTTP Response <<<", result);
	// JSONObject jsonObject = new JSONObject(result);
	// String status = jsonObject.getString("status");
	// if(status.equalsIgnoreCase("true")) {
	//
	// JSONArray jsonArray = jsonObject.getJSONArray("dep");
	// for (int i = 0; i < jsonArray.length(); i++) {
	//
	// int id = jsonArray.getJSONObject(i).getInt("id");
	// for (int j = 0; j < aryDepartList.size(); j++) {
	//
	// if(aryDepartList.get(j).id == id) {
	// aryDepartList.get(j).bookable = true;
	// break;
	// }
	// }
	// }
	//
	// } else {
	//
	// MessageBox.OK(MainActivity.this, "Alert",
	// "Checking bookable department failed");
	// }
	//
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// Toast.makeText(MainActivity.this,
	// "Invalid Data",Toast.LENGTH_LONG).show();
	// }
	// }
	// });
	//
	// } else {
	// Toast.makeText(MainActivity.this,
	// "No Internet Connection",Toast.LENGTH_LONG).show();
	// }
	// }

	private void loadHotelInfo() {

		if (NetworkUtils.haveInternet(this)) {

			// .
			// http://www.roomallocator.com/appcreator/services/hoteldetails.php?hotel_id=18
			String url = "http://www.roomallocator.com/appcreator/services/hoteldetails.php";

			String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);

			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);

			DialogUtils.launchProgress(this,
					"Please wait while loading Hotel Info");
			CustomerHttpClient.getFromFullService(url, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onFinish() {

							DialogUtils.exitProgress();
							loadRestaurantList();
							super.onFinish();
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] errorResponse, Throwable e) {

							Toast.makeText(MainActivity.this,
									"Connection was lost (" + statusCode + ")",
									Toast.LENGTH_LONG).show();
							super.onFailure(statusCode, headers, errorResponse,
									e);
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] response) {
							// Pull out the first event on the public timeline
							try {

								/*
								 * { "status": "true", "data": { "id": "8",
								 * "hotel": "agaza", "logo":
								 * "http://www.roomallocator.com/appcreator/uploads/66767_539906172757012_1841677721_n.jpg"
								 * , "rooms":
								 * "http://www.roomallocator.com/appcreator/uploads/"
								 * , "resturantimg":
								 * "http://www.roomallocator.com/appcreator/uploads/"
								 * } }
								 */

								String result = new String(response);
								Log.i("HTTP Response <<<", result);
								JSONObject jsonObject = new JSONObject(result);
								String status = jsonObject.getString("status");
								if (status.equalsIgnoreCase("true")) {

									JSONObject data = jsonObject
											.getJSONObject("data");
									PrefValue.setString(MainActivity.this,
											R.string.pref_hotel_name,
											data.getString("hotel"));
									PrefValue.setString(MainActivity.this,
											R.string.pref_hotel_logo,
											data.getString("logo"));
									PrefValue.setString(MainActivity.this,
											R.string.pref_hotel_bg,
											data.getString("resturantimg"));

									if (DeviceUtil
											.isTabletByRes(MainActivity.this)) {
										ImageLoader
												.getInstance()
												.displayImage(
														PrefValue
																.getString(
																		MainActivity.this,
																		R.string.pref_hotel_bg),
														ivHotelBg,
														optionsForBg,
														animateFirstListener);
										ImageLoader
												.getInstance()
												.displayImage(
														PrefValue
																.getString(
																		MainActivity.this,
																		R.string.pref_hotel_logo),
														ivHotelLogo, null,
														animateFirstListener);
										tvHotelName.setText(PrefValue
												.getString(
														MainActivity.this,
														R.string.pref_hotel_name));
									} else {
										if (m_Fragment != null
												&& m_Fragment.getClass()
														.getName() == HomeFragment.class
														.getName())
											((HomeFragment) m_Fragment)
													.loadHotelInfo();
									}
								} else {
									Toast.makeText(MainActivity.this,
											jsonObject.getString("message"),
											Toast.LENGTH_SHORT).show();
								}

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(MainActivity.this,
										"Invalid Data", Toast.LENGTH_LONG)
										.show();
							}
						}
					});

		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	private void loadDemoDepartments() {

		aryAllDepartList.clear();
		if (NetworkUtils.haveInternet(this)) {

			String url = "http://www.roomallocator.com/appcreator/services/getDepartmentItemsAll.php";
			String hotel_id = PrefValue.getString(this, R.string.pref_hotel_id);

			RequestParams params = new RequestParams();
			params.add("hotel_id", hotel_id);

			// http://www.roomallocator.com/appcreator/services/getDepartmentItemsAll.php?hotel_id=126
			CustomerHttpClient
					.getFromFullService(
							"http://www.roomallocator.com/appcreator/services/getDepartmentItemsAll.php",
							params, new AsyncHttpResponseHandler() {
								@Override
								public void onFinish() {
									DialogUtils.exitProgress();
									super.onFinish();
								}

								@Override
								public void onFailure(int statusCode,
										Header[] headers, byte[] errorResponse,
										Throwable e) {

									Toast.makeText(
											MainActivity.this,
											"Connection was lost ("
													+ statusCode + ")",
											Toast.LENGTH_LONG).show();
									super.onFailure(statusCode, headers,
											errorResponse, e);
								}

								@Override
								public void onSuccess(int statusCode,
										Header[] headers, byte[] response) {
									// Pull out the first event on the public
									// timeline
									try {
										String result = new String(response);
										result = result.replace("({", "{");
										result = result.replace("})", "}");
										Log.i("HTTP Response <<<", result);
										JSONObject jsonObject = new JSONObject(
												result);
										JSONArray dataArray = jsonObject
												.getJSONArray("data");
										// JSONArray departArray =
										// data.getJSONArray("depart");

										int tmpPreviousDepId = 0;
										for (int i = 0; i < dataArray.length(); i++) {

											JSONObject object = dataArray
													.getJSONObject(i);

											AllDepartInfo demoItem = new AllDepartInfo();
											demoItem.id = object.getInt("id");
											demoItem.name = object
													.getString("name");
											demoItem.image = object
													.getString("image");
											demoItem.dep_id = object
													.getInt("dep_id");
											demoItem.dept_name = object
													.getString("dept_name");
											demoItem.price = object
													.getString("price");
											demoItem.desc = object
													.getString("description");
											aryAllDepartList.add(demoItem);

											if (tmpPreviousDepId != object
													.getInt("dep_id")) {
												DepartInfo item = new DepartInfo();
												item.id = object
														.getInt("dep_id");
												item.desc = object
														.getString("dept_name");
												item.title = object
														.getString("dept_name");
												aryDepartList.add(item);
											}
											tmpPreviousDepId = object
													.getInt("dep_id");
										}

										updateDepartList();
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										Toast.makeText(MainActivity.this,
												"Invalid Data",
												Toast.LENGTH_LONG).show();
									}
								}
							});
		} else {
			Toast.makeText(MainActivity.this, "No Internet Connection",
					Toast.LENGTH_LONG).show();
		}
	}

	public ArrayList<AllDepartInfo> getAllDepartList() {
		return this.aryAllDepartList;
	}
}
