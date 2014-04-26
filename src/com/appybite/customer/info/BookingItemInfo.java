package com.appybite.customer.info;

import java.util.ArrayList;

public class BookingItemInfo
{
	public String session;
	public String b_id;
	public ArrayList<ItemDetails> ItemDetails;
	
	public class ItemDetails
	{
		public String sday;
		public String id;
	}
}