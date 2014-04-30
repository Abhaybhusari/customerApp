package com.appybite.customer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appybite.customer.info.ReceiptInfo;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class CartListAdapter extends ArrayAdapter<ReceiptInfo> {

	@SuppressWarnings("unused")
	private Context m_Context;
	private int ITEM_LAYOUT = -1;
	private CallbackItemEvent callback;
	
	public CartListAdapter(Context context, CallbackItemEvent callback, int p_res,
			ArrayList<ReceiptInfo> arrayList) {

		super(context, p_res, arrayList);

		m_Context = context;
		ITEM_LAYOUT = p_res;
		this.callback = callback;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View item = convertView;
		final ViewHolder holder;

		// 뷰 설정
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

		// 뷰의 속성값 얻기
		ReceiptInfo value = getItem(position);
		if (value == null) {
			return item;
		}

		if (value.type == 0) {
			holder.tvTitle.setTypeface(null, Typeface.BOLD);
			holder.tvTitle.setText(value.title);
		} else if (value.type == 1) {
			holder.tvTitle.setTypeface(null, Typeface.NORMAL);
			holder.tvTitle.setText("   " + value.title);
		} else if (value.type == 2) {
			holder.tvTitle.setTypeface(null, Typeface.NORMAL);
			holder.tvTitle.setText("   " + value.title);
		}

		holder.tvQnt.setText(String.valueOf(value.qnt));
		
		float price = Float.parseFloat(value.price) * value.qnt;
		if (price > 0)
			holder.tvPrice.setText(String.format("%.2f", price));
		else
			holder.tvPrice.setText("");
		
		if(callback == null)
			holder.rlDelete.setVisibility(View.INVISIBLE);
		else {
			holder.rlDelete.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					callback.onDeleteClick(position);
				}
			});
		}
		
		return item;
	}

	// =====================================================================================================
	/**
	 * 아이템 UI 요소들
	 */
	private class ViewHolder {

		public RelativeLayout rlParent;
		public TextView tvTitle;
		public TextView tvQnt;
		public TextView tvPrice;
		public RelativeLayout rlDelete;
		public Button btDelete;

		public ViewHolder(View parent) {

			// get views
			rlParent = (RelativeLayout) parent.findViewById(R.id.rlParent);
			tvTitle = (TextView) parent.findViewById(R.id.tvTitle);
			tvQnt = (TextView) parent.findViewById(R.id.tvQnt);
			tvPrice = (TextView) parent.findViewById(R.id.tvPrice);
			rlDelete = (RelativeLayout) parent.findViewById(R.id.rlDelete);
			btDelete = (Button) parent.findViewById(R.id.btDelete);
			
			if (!PRJFUNC.DEFAULT_SCREEN) {
				scaleView();
			}
		}

		private void scaleView() {

			if (PRJFUNC.mGrp == null) {
				return;
			}

			// . Category
			PRJFUNC.mGrp.relayoutView(rlParent, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
			PRJFUNC.mGrp.setTextViewFontScale(tvQnt);
			PRJFUNC.mGrp.relayoutView(tvQnt, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.setTextViewFontScale(tvPrice);
			PRJFUNC.mGrp.relayoutView(tvPrice, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(rlDelete, LayoutLib.LP_RelativeLayout);
			PRJFUNC.mGrp.relayoutView(btDelete, LayoutLib.LP_RelativeLayout);
		}
	}
	
	public interface CallbackItemEvent {
		public void onDeleteClick(int position);
	}
}
