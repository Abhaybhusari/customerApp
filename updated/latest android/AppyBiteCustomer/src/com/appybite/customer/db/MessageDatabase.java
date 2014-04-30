package com.appybite.customer.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.appybite.customer.info.MessageInfo;

public class MessageDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "MessageDb";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "MessageTable";
	String TAG = "MessageDatabase";
	private Context context;
	private static final String INSERT = "insert into "
			+ TABLE_NAME
			+ "(hotel_id,c_id,title,body,time,state) values (?,?,?,?,?,?)";

	public MessageDatabase(Context context) {
		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE "
				+ TABLE_NAME
				+ " (id INTEGER PRIMARY KEY"
				+ ",hotel_id TEXT"
				+ ",c_id TEXT"
				+ ",title TEXT"
				+ ",body TEXT"
				+ ",time INTEGER"
				+ ",state INTEGER"
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("Example",
				"Upgrading database, this will drop tables and recreate.");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	public void insert(String hotel_id, String c_id, String title, String body, long time, int state) {
		
		SQLiteDatabase db = getWritableDatabase();
		SQLiteStatement insertStmt = db.compileStatement(INSERT);
		
		insertStmt.bindString(1, hotel_id);
		insertStmt.bindString(2, c_id);
		insertStmt.bindString(3, title);
		insertStmt.bindString(4, body);
		insertStmt.bindLong(5, time);
		insertStmt.bindLong(6, state);

		insertStmt.executeInsert();
		
		db.close();
	}
	
	public void deleteAll(String hotel_id) {
		
		SQLiteDatabase db = getWritableDatabase();
		
		String where = "hotel_id = '" + hotel_id + "'";
		db.delete(TABLE_NAME, where, null);
		
		db.close();
	}

	public void deleteItem(String hotel_id, String c_id) {
		
		SQLiteDatabase db = getWritableDatabase();
		
		String where = "hotel_id = '" + hotel_id + "' AND " + "c_id = '" + c_id + "'";
		db.delete(TABLE_NAME, where, null);
		
		db.close();
	}

	public void deleteItem(String hotel_id, String c_id, long time) {
		
		SQLiteDatabase db = getWritableDatabase();
		
		String where = "hotel_id = '" + hotel_id + "' AND " + "c_id = '" + c_id + "' AND " + "time = " + time;
		db.delete(TABLE_NAME, where, null);
		
		db.close();
	}

	public ArrayList<MessageInfo> getMessageListByCID(String hotel_id, String c_id) {
		
		SQLiteDatabase db = getReadableDatabase();
		
		ArrayList<MessageInfo> aryExtraList = new ArrayList<MessageInfo>();
		String where = "hotel_id='" + hotel_id + "' AND c_id='" + c_id + "'";
		String orderBy = "time DESC";
		Cursor cursor = db.query(TABLE_NAME, new String[] {
				"title", "body", "time", "state" }, where, null, null, null, orderBy);
		if (cursor.moveToFirst()) {
			do {
				MessageInfo info = new MessageInfo();
				info.title = cursor.getString(0);
				info.body = cursor.getString(1);
				info.time = cursor.getLong(2);
				info.state = (int) cursor.getInt(3);
				
				aryExtraList.add(info);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		
		db.close();
		return aryExtraList;
	}
	
	public int getUnreadCount(String hotel_id, String c_id) {
		
		SQLiteDatabase db = getReadableDatabase();
		
		int count = 0;
		try {
			String qry = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE state = 0 AND hotel_id= '" + hotel_id + "' AND c_id= '" + c_id + "'";
			Cursor cursor = db.rawQuery(qry, null);
			while (cursor.moveToNext()) {
				count = cursor.getInt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		db.close();
		return count;
	}
	
	public void setAsRead(String hotel_id, String c_id) {
		
		SQLiteDatabase db = getReadableDatabase();
		
		try {
			String qry = "UPDATE " + TABLE_NAME + " SET state=1 WHERE hotel_id= '" + hotel_id + "' AND c_id= '" + c_id + "'";
			db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		db.close();
	}
	
	public void setAsRead(String hotel_id, String c_id, long time) {

		SQLiteDatabase db = getReadableDatabase();
		
		try {
			String qry = "UPDATE " + TABLE_NAME + " SET state=1 WHERE hotel_id= '" + hotel_id + "' AND c_id= '"
					+ c_id + "' AND time=" + time;
			db.execSQL(qry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		db.close();
	}
}
