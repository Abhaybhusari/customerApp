package com.appybite.customer;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appybite.customer.info.CategoryInfo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yj.commonlib.image.AnimateFirstDisplayListener;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class CategoryListAdapter extends ArrayAdapter<CategoryInfo> {

	@SuppressWarnings("unused")
	private Context m_Context;
	private int ITEM_LAYOUT = -1;
	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private int orientation =0 ;
	private DisplayMetrics displaymetrics;
	
	public CategoryListAdapter(Context context, int p_res,
			ArrayList<CategoryInfo> arrayList) {
		
		super(context, p_res, arrayList);
		orientation = context.getResources().getConfiguration().orientation;
		 displaymetrics = context.getResources()
				.getDisplayMetrics();
		m_Context = context;
		ITEM_LAYOUT = p_res;
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.bg_default_depart)
			.showImageForEmptyUri(R.drawable.bg_default_depart)
			.showImageOnFail(R.drawable.bg_default_depart)
			.cacheInMemory(false)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		View item = convertView;
		final ViewHolder holder;

		// ë·° ì„¤ì •
		if (item == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			item = vi.inflate(ITEM_LAYOUT, parent, false);
			holder = new ViewHolder(item);
			item.setTag(holder);
		} else {
			holder = (ViewHolder) item.getTag();
		}
		item.setId(position);
		
		// ë·°ì�˜ ì†�ì„±ê°’ ì–»ê¸°
		final CategoryInfo value = getItem(position);
		if (value == null) {
			return item;
		}
		
		ImageLoader.getInstance().displayImage(value.thumb, holder.ivThumb, options, animateFirstListener);
		holder.tvTitle.setText(value.name);
		return item;
	}

	// =====================================================================================================
	/**
	 * ì•„ì�´í…œ UI ìš”ì†Œë“¤
	 */
	private class ViewHolder {

		public RelativeLayout rlParent;
		public RelativeLayout rlTextParent;
		
		public ImageView ivThumb;
		public TextView tvTitle;
		
		public ViewHolder(View parent) {

			// get views
			rlParent	= (RelativeLayout) parent.findViewById(R.id.rlParent);
			ivThumb		= (ImageView) parent.findViewById(R.id.ivThumb);
			tvTitle 	= (TextView) parent.findViewById(R.id.tvTitle);
			
//			if(orientation == Configuration.ORIENTATION_LANDSCAPE)
				rlTextParent	= (RelativeLayout) parent.findViewById(R.id.rlTextParent);
			
			if (!PRJFUNC.DEFAULT_SCREEN) {
				scaleView(parent);
			}
		}
		
		private void scaleView(View v) {

			if (PRJFUNC.mGrp == null) {
				return;
			}
			
			//. Category
			if(orientation == Configuration.ORIENTATION_LANDSCAPE && displaymetrics.widthPixels>700){
			
			PRJFUNC.mGrp.relayoutView(ivThumb, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(rlParent, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(tvTitle, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
			PRJFUNC.mGrp.relayoutView(rlTextParent, LayoutLib.LP_RelativeLayout);
			}
			
			
//			
			PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowTop), LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(v.findViewById(R.id.ivShadowBottom), LayoutLib.LP_RelativeLayout);
		}	
	}
}
