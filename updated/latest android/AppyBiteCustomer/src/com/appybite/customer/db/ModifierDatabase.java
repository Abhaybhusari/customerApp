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

public class ModifierDatabase {
	private static final String DATABASE_NAME = "ModifiersDb";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "ModifiersTable";
	String TAG = "ModifiersDatabase";
	OpenHelper openHelper;

	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insertStmt;
	private static final String INSERT = "insert into "
			+ TABLE_NAME
			+ "(hotel_id,c_id,product_id,modifier_id,modifier_name,price,qnt,pay_type,status) values (?,?,?,?,?,?,?,?,?)";
	public ModifierDatabase(Context context) {
		this.context = context;
		openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
	}

	public ModifierDatabase(Context context, CursorFactory factory) {
		this.context = context;
		openHelper = new OpenHelper(this.context);
		this.db = openHelper.getReadableDatabase();
		this.insertStmt = this.db.compileStatement(INSERT);
	}

	public void insert(String hotel_id, String c_id, String product_id, String modifier_id, String modifier_title, String price, int qnt) {
		
		int old_qnt = isAlreadyInserted(hotel_id, c_id, product_id, modifier_id);
		if (old_qnt > 0) {
			updateQntByProductId(hotel_id, c_id, product_id, modifier_id, old_qnt+qnt);
		} else {
			this.insertStmt.bindString(1, hotel_id);
			this.insertStmt.bindString(2, c_id);
			this.insertStmt.bindString(3, product_id);
			this.insertStmt.bindString(4, modifier_id);
			this.insertStmt.bindString(5, modifier_title);
			this.insertStmt.bindString(6, price);
			this.insertStmt.bindLong(7, qnt);
			this.insertStmt.bindString(8, LocalOrderListDatabase.pay_type_unspecified);
			this.insertStmt.bindString(9, LocalOrderListDatabase.status_pending);

			this.insertStmt.executeInsert();
		}
	}
	
//	public void deleteAll(String hotel_id) {
//		String where = "hotel_id = '" + hotel_id + "'";
//		this.db.delete(TABLE_NAME, where, null);
//	}

	public void deleteItem(String hotel_id, String c_id, String product_id, String status) {
		String where = "hotel_id = '" + hotel_id + "' AND c_id = '" + c_id + "' AND product_id = '" + product_id + "'";
		where += " AND status='" + status + "'";
		this.db.delete(TABLE_NAME, where, null);
	}
	
//	public void deleteItem(String hotel_id, String product_id, String modifier_id) {
//		String where = "hotel_id='" + hotel_id + "' AND product_id = '" +product_id+"' AND modifier_id = '" + modifier_id + "'";
//		this.db.delete(TABLE_NAME, where, null);
//	}
	
	public void updatePayTypeByHotel(String hotel_id, String c_id, String pay_type, String status) {

		try {
			String qry = "UPDATE " + TABLE_NAME + " SET pay_type='" + pay_type + "'"
					+ ", status='" + status + "'"
					+ " WHERE hotel_id= '" + hotel_id + "' AND c_id = '" + c_id + "'";
			qry += " AND status='" + LocalOrderListDatabase.status_pending + "'";
			this.db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		db.close();
		openHelper.close();
	}

	public ArrayList<ReceiptInfo> getModifierListByProductId(String hotel_id, String c_id, String product_id, String status) {
		ArrayList<ReceiptInfo> aryModifierList = new ArrayList<ReceiptInfo>();
		String where = "hotel_id='" + hotel_id + "' AND product_id='" + product_id + "' AND c_id = '" + c_id + "'";
		where += " AND status='" + status + "'";
		Cursor cursor = this.db.query(TABLE_NAME, new String[] {
				"modifier_id", "modifier_name", "price", "qnt" }, where, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				ReceiptInfo info = new ReceiptInfo();
				info.type = 2;
				info.id = cursor.getString(0);
				info.title = cursor.getString(1);
				info.price = cursor.getString(2);
				info.qnt = (int) cursor.getLong(3);
				info.product_id = product_id;
				
				aryModifierList.add(info);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return aryModifierList;
	}
	
	public void updateQntByProductId(String hotel_id, String c_id, String product_id, String modifier_id,int qnt) {

		try {
			String qry = "UPDATE " + TABLE_NAME + " SET qnt=" + (qnt)
					+ " WHERE hotel_id= '" + hotel_id + "' AND c_id = '" + c_id + "' AND product_id= '"
					+ product_id + "' AND modifier_id='" + modifier_id + "'";
			qry += " AND status='" + LocalOrderListDatabase.status_pending + "'";
			this.db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int isAlreadyInserted(String hotel_id, String c_id, String product_id, String modifier_id) {
		int abc = 0;
		String where = "hotel_id='" + hotel_id + "' AND c_id = '" + c_id + "' AND product_id='" + product_id + "' AND modifier_id='" + modifier_id + "'";
		where += " AND status='" + LocalOrderListDatabase.status_pending + "'";
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
	
	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE "
					+ TABLE_NAME
					+ " (id INTEGER PRIMARY KEY"
					+ ",hotel_id TEXT"
					+ ",c_id TEXT"
					+ ",product_id TEXT"
					+ ",modifier_id TEXT"
					+ ",modifier_name TEXT"
					+ ",price TEXT"
					+ ",qnt INTEGER"
					+ ",pay_type TEXT"
					+ ",status TEXT"
					+ ")");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Example",
					"Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}// end of class openhelper

}
