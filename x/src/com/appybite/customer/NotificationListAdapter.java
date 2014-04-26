package com.appybite.customer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.appybite.customer.info.MessageInfo;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class NotificationListAdapter extends ArrayAdapter<MessageInfo> {

	@SuppressWarnings("unused")
	private Context m_Context;
	private int ITEM_LAYOUT = -1;
	private CallbackItemEvent callback;
	
	public NotificationListAdapter(Context context, CallbackItemEvent callback, int p_res,  
			ArrayList<MessageInfo> arrayList) {
		
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
		final MessageInfo value = getItem(position);
		if (value == null) {
			return item;
		}
		
		holder.tvTitle.setText(value.body);
		
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTimeInMillis(value.time);
		SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // yyyy/MMM/dd//HH:mm:ss
		String time = date_formatter.format(currentDate.getTime());
		holder.tvTime.setText(time);

		holder.btDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				callback.onDeleteClick(position);
			}
		});

		return item;
	}

	// =====================================================================================================
	/**
	 * 아이템 UI 요소들
	 */
	private class ViewHolder {

		public TextView tvTitle;
		public TextView tvTime;
		public Button btDelete;
		
		public ViewHolder(View parent) {

			// get views
			tvTitle 	= (TextView) parent.findViewById(R.id.tvTitle);
			tvTime		= (TextView) parent.findViewById(R.id.tvTime);
			btDelete	= (Button) parent.findViewById(R.id.btDelete);
			if (!PRJFUNC.DEFAULT_SCREEN) {
				scaleView();
			}
		}
		
		private void scaleView() {

			if (PRJFUNC.mGrp == null) {
				return;
			}
			
			//. Category
			PRJFUNC.mGrp.setTextViewFontScale(tvTitle);
			PRJFUNC.mGrp.setTextViewFontScale(tvTime);
			PRJFUNC.mGrp.relayoutView(tvTime, LayoutLib.LP_RelativeLayout);
			
			PRJFUNC.mGrp.setButtonFontScale(btDelete);
			PRJFUNC.mGrp.relayoutView(btDelete, LayoutLib.LP_RelativeLayout);
		}	
	}
	
	public interface CallbackItemEvent {
		public void onDeleteClick(int position);
	}
}
