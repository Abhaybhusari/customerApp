package com.appybite.customer.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.appybite.customer.info.ReceiptInfo;

public class LocalOrderListDatabase {
	final String TAG = "LocalOrderDB";
	public static final String DATABASE_NAME = "LocalOrderListDB";
	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_NAME = "LocalOrderListTable";
	OpenHelper openHelper;

	/*
	 * === status ===
	 * pending: 아이템들을 선택했을때 place order 를 하기 전까지의 상태
	 * open: place order를 charge room 으로 진행하였을때
	 * closed: place order 를 by cash 로 진행하였을때 혹은 check room 으로 charge room 으로 되었던 아이템들의 돈을 지불하였을때  
	 * 
	 * === pay_type ===
	 * unspecified: 아이템들이 새로 추가되었을때 
	 * cash: place order 때 결정
	 * charge_room: place order 때 결정
	 */
	public final static String status_pending = "pending";
	public final static String status_open = "open";
	public final static String status_closed = "closed";
	
	public final static String pay_type_unspecified = "unspecified";
	public final static String pay_type_cash = "cash";
	public final static String pay_type_charge_room = "charge_room";
	
	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into "
			+ TABLE_NAME
			+ "(hotel_id,c_id,product_id,product_name,qnt,price,msg,depart_id,depart_name,order_type, pay_type, status) values (?,?,?,?,?,?,?,?,?,?,?,?)";

	public LocalOrderListDatabase(Context context) {
		this.context = context;
		openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
	}

	public LocalOrderListDatabase(Context context, CursorFactory factory) {
		this.context = context;
		openHelper = new OpenHelper(this.context);
		this.db = openHelper.getReadableDatabase();
	}

	/*---------------------------------------------------insert--------------------------------------------------------*/
	public void insert(
			String hotel_id,
			String c_id,
			String product_id,
			String product_name,
			String price,
			int qnt,
			String msg,
			String depart_id, 
			String depart_name, 
			String order_type
			) {
		
		int cnt = isAlreadyInserted(hotel_id, c_id, depart_id, product_id);
		if (cnt > 0) {
			updateQntByProductId(hotel_id, c_id, depart_id, product_id, cnt+qnt);
		} else {
			this.insertStmt.bindString(1, hotel_id);
			this.insertStmt.bindString(2, c_id);
			this.insertStmt.bindString(3, product_id);
			this.insertStmt.bindString(4, product_name);
			this.insertStmt.bindLong(5, qnt);
			this.insertStmt.bindString(6, price);
			this.insertStmt.bindString(7, msg);
			this.insertStmt.bindString(8, depart_id);
			this.insertStmt.bindString(9, depart_name);
			this.insertStmt.bindString(10, order_type);
			this.insertStmt.bindString(11, pay_type_unspecified);
			this.insertStmt.bindString(12, status_pending);
			
			this.insertStmt.executeInsert();
		}
	}
	
//	public void deleteAll() {
//		this.db.delete(TABLE_NAME, null, null);
//	}
//
//	public void deleteAllByHotel(String hotel_id) {
//		String where = "hotel_id = '" + hotel_id + "'";
//		this.db.delete(TABLE_NAME, where, null);
//	}
//	
//	public void deleteAllByOrderType(String hotel_id, String order_type)
//	{
//		String where = "hotel_id = '" + hotel_id + "' AND order_type='" + order_type + "'";
//		this.db.delete(TABLE_NAME, where, null);
//	}
//
//	public void deleteAllByDepart(String hotel_id, String depart_id)
//	{
//		String where = "hotel_id = '" + hotel_id + "' AND depart_id='" + depart_id + "'";
//		this.db.delete(TABLE_NAME, where, null);
//	}

	public void deleteItem(String hotel_id, String c_id, String depart_id, String product_id, String status)
	{
		String where = "hotel_id = '" + hotel_id + "' AND c_id='" + c_id + "' AND depart_id='" + depart_id + "' AND product_id='" + product_id + "'";
		where += " AND status='" + status + "'";
		this.db.delete(TABLE_NAME, where, null);
	}
	
	public void updatePayTypeByHotel(String hotel_id, String c_id, String pay_type, String status) {

		try {
			String qry = "UPDATE " + TABLE_NAME + " SET pay_type='" + pay_type + "'"
					+ ", status='" + status + "'"
					+ " WHERE hotel_id= '" + hotel_id + "' AND c_id='" + c_id + "'";
			qry += " AND status='" + status_pending + "'";
			this.db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		db.close();
		openHelper.close();
	}
	
	public ArrayList<ReceiptInfo> getFoodItemList(String hotel_id, String c_id, String status) {
		ArrayList<ReceiptInfo> aryFoodItemList = new ArrayList<ReceiptInfo>();
		String where = "hotel_id='" + hotel_id + "'";
		where += " AND c_id='" + c_id + "'";
		where += " AND status='" + status + "'";
		
		Cursor cursor = this.db.query(TABLE_NAME, new String[] { 
				"product_id", "product_name", "qnt", "price", "msg", "depart_id", "depart_name", "order_type" }, where, null, null, null, null);
		
		if (cursor.moveToFirst()) {
			do {
				ReceiptInfo info = new ReceiptInfo();
				info.type = 0;
				info.id = cursor.getString(0);
				info.title = cursor.getString(1);
				info.qnt = (int)cursor.getLong(2);
				info.price = cursor.getString(3);
				info.msg = cursor.getString(4);
				info.depart_id = cursor.getString(5);
				info.depart_name = cursor.getString(6);
				info.order_type = cursor.getString(7);
				
				aryFoodItemList.add(info);
			} while (cursor.moveToNext());
		}
		
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return aryFoodItemList;
	}
	
	public void updateFoodItem(String product_id, String status) {
		String where = "product_id='" + product_id;
		where += " AND status='" + status + "'";
		
		try {
			String qry = "UPDATE " + TABLE_NAME + " SET status='" + status_closed + "'"
					+ " WHERE product_id= '" + product_id + "'";
			this.db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateQntByProductId(String hotel_id, String c_id, String depart_id, String product_id, int qnt) {

		try {
			String qry = "UPDATE " + TABLE_NAME + " SET qnt=" + (qnt)
					+ " WHERE hotel_id= '" + hotel_id + "' AND c_id='" + c_id + "' AND product_id= '"
					+ product_id + "' AND depart_id='" + depart_id + "'";
			qry += " AND status='" + status_pending + "'";
			this.db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int isAlreadyInserted(String hotel_id, String c_id, String depart_id, String product_id) {
		int abc = 0;
		String where = "hotel_id='" + hotel_id + "' AND c_id='" + c_id + "' AND product_id='" + product_id + "' AND depart_id='" + depart_id + "'";
		where += " AND status='" + status_pending + "'";
		Cursor cursor = this.db.query(TABLE_NAME,
				new String[] { "qnt" }, where, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				abc = (int)cursor.getLong(0);;
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return abc;
	}

	/*---------------------------------------------------------------------------------------------------*/
	/*---------------------------------------------------------------------------------------------------*/
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE "
					+ TABLE_NAME
					+ " (id INTEGER PRIMARY KEY"
					+ ", hotel_id TEXT"
					+ ", c_id TEXT"
					+ ", product_id TEXT"
					+ ", product_name TEXT"
					+ ", qnt TEXT"
					+ ", price TEXT"
					+ ", msg TEXT"
					+ ", depart_id TEXT"
					+ ", depart_name TEXT"
					+ ", order_type TEXT"
					+ ", pay_type TEXT"
					+ ", status TEXT"
					+ ")");
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Example",
					"Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
