package com.appybite.customer.info;

public class CategoryInfo
{
	/*
    {
        "id": "1",
        "name": "Appetisers",
        "image": "subcategory.jpg",
        "thumb": "subcategory.jpg"
    }
	*/
	public String id;
	public String name;
	public String image;
	public String thumb; //. room_no, table_no, ...
	public int hasSubCat; //. 0: don't have, 1: have  
}