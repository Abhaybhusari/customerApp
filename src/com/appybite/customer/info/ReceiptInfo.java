package com.appybite.customer.info;

public class ReceiptInfo
{
	public int type; //. 0 : main, 1 : extra, 2 : modifier
	public String id;
	public String title;
	public String price;
	public int qnt;
	public String msg;
	public String depart_id;
	public String depart_name;
	public String order_type;
	public String product_id; //. extra/modifier's product id
}