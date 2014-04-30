package com.appybite.customer;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.appybite.customer.NotificationListAdapter.CallbackItemEvent;
import com.appybite.customer.db.MessageDatabase;
import com.appybite.customer.info.MessageInfo;
import com.yj.commonlib.pref.PrefValue;
import com.yj.commonlib.screen.PRJFUNC;

public class NotificationFragment extends Fragment implements CallbackItemEvent{

	private ListView lvMessage;
	private NotificationListAdapter m_adtNotificationList;
	
	public NotificationFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_notification, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		loadMessages();
		if(m_adtNotificationList.getCount() == 0)
			Toast.makeText(getActivity(), "There is no notifications.", Toast.LENGTH_SHORT).show();
		
		return v;
	}
	
	// //////////////////////////////////////////////////
	private void updateLCD(View v) {

		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}
		
		m_adtNotificationList = new NotificationListAdapter(
				getActivity(), this,
				R.layout.item_message, 
				new ArrayList<MessageInfo>()
				);
		
		lvMessage = (ListView)v.findViewById(R.id.lvMessage);
		// m_lvMenuList.setSelector(new ColorDrawable(Color.TRANSPARENT));
		lvMessage.setCacheColorHint(Color.TRANSPARENT);
		lvMessage.setDividerHeight(0);
		lvMessage.setAdapter(m_adtNotificationList);
	}

	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
	}
	
	public void loadMessages() {
		
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		
		MessageDatabase msg_db = new MessageDatabase(getActivity());
		ArrayList<MessageInfo> aryMessageList = msg_db.getMessageListByCID(hotel_id, c_id);
		msg_db.close();
		
		m_adtNotificationList = new NotificationListAdapter(
				getActivity(), this,
				R.layout.item_message, 
				aryMessageList
				);
		
		lvMessage.setAdapter(m_adtNotificationList);
	}
	
	private void deleteMessage(long time) {
		
		String hotel_id = PrefValue.getString(getActivity(), R.string.pref_hotel_id);
		String c_id = PrefValue.getString(getActivity(), R.string.pref_customer_id);
		
		MessageDatabase msg_db = new MessageDatabase(getActivity());
		msg_db.deleteItem(hotel_id, c_id, time);
		msg_db.close();
	}
	
	@Override
	public void onDeleteClick(int position) {

		MessageInfo info = m_adtNotificationList.getItem(position);
		deleteMessage(info.time);
		m_adtNotificationList.remove(info);
		m_adtNotificationList.notifyDataSetChanged();
	}
}
