package com.appybite.customer.info;

public class OrderInfo
{
	public String order_type;
	public String order_token;
	public String no; //. room_no, table_no, ...
	public String s_time;
	public String e_time;
	public String ord_time;
	public CustomerInfo customer;
	public String order_status; //. "pending", "open", "close"
}