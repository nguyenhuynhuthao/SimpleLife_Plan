package com.example.simplelife.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class PlansDBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "PlanBDOpenHelper";

    private static final String CREATE_EVENTS_TABLE = "create table " + PlansDBStructure.EVENT_TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            +PlansDBStructure.EVENT + " TEXT," + PlansDBStructure.EVENT_PLACE + " TEXT," + PlansDBStructure.TIME + " TEXT,"
            + PlansDBStructure.DATE + " TEXT," + PlansDBStructure.MONTH + " TEXT,"
            +PlansDBStructure.YEAR + " TEXT," + PlansDBStructure.EVENT_BRING + " TEXT," + PlansDBStructure.Notify + " TEXT)";



    private static final String DROP_EVENTS_TABLE = "DROP TABLE IF EXISTS " + PlansDBStructure.EVENT_TABLE_NAME;

    public PlansDBOpenHelper(@Nullable Context context) {
        super(context, PlansDBStructure.DB_NAME,null, PlansDBStructure.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("Tạo bảng", CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("Cập nhật bảng", CREATE_EVENTS_TABLE);
        db.execSQL(DROP_EVENTS_TABLE);
        onCreate(db);
    }

    //TODO: Dùng để lưu các sự kiện
    public void SaveEvent(String event, String place, String time, String date, String month, String year, String bring, String notify, SQLiteDatabase database){

        ContentValues contentValues = new ContentValues();
        contentValues.put(PlansDBStructure.EVENT,event);
        contentValues.put(PlansDBStructure.EVENT_PLACE,place);
        contentValues.put(PlansDBStructure.TIME,time);
        contentValues.put(PlansDBStructure.DATE,date);
        contentValues.put(PlansDBStructure.MONTH,month);
        contentValues.put(PlansDBStructure.YEAR,year);
        contentValues.put(PlansDBStructure.EVENT_BRING,bring);
        //set content values cho checkbox nhắc nhở
        contentValues.put(PlansDBStructure.Notify,notify);
        database.insert(PlansDBStructure.EVENT_TABLE_NAME, null, contentValues);
        Log.i("Thêm plan: ", "Thêm thành công");
    }

    //TODO: Đọc và hiển thị events lên theo ngày
    public Cursor ReadEvent(String date, SQLiteDatabase database){
        String [] Projections = {PlansDBStructure.EVENT, PlansDBStructure.EVENT_PLACE, PlansDBStructure.TIME, PlansDBStructure.DATE, PlansDBStructure.MONTH, PlansDBStructure.YEAR, PlansDBStructure.EVENT_BRING};
        String Selection = PlansDBStructure.DATE + "=?";
        String [] SelectionArgs = {date};

        return database.query(PlansDBStructure.EVENT_TABLE_NAME, Projections, Selection, SelectionArgs, null, null, null);
    }

    //TODO: Hàm thực hiện thao tác nhắc nhở
    public Cursor ReadIDEvent(String date, String event, String time, String place, String bring, SQLiteDatabase database){
        String [] Projections = {PlansDBStructure.ID, PlansDBStructure.Notify};
        String Selection = PlansDBStructure.DATE + "=? and " + PlansDBStructure.EVENT + "=? and " + PlansDBStructure.TIME + "=? and " + PlansDBStructure.EVENT_PLACE + "=? and " + PlansDBStructure.EVENT_BRING + "=? ";
        String [] SelectionArgs = {date,event,time,place,bring};

        return database.query(PlansDBStructure.EVENT_TABLE_NAME, Projections, Selection, SelectionArgs, null, null, null);
    }

    //TODO: Đọc và hiển thị events lên theo tháng, năm
    public Cursor ReadEventsperMonth(String month, String year, SQLiteDatabase database){
        String [] Projections = {PlansDBStructure.EVENT, PlansDBStructure.EVENT_PLACE, PlansDBStructure.TIME, PlansDBStructure.DATE,
                PlansDBStructure.MONTH, PlansDBStructure.YEAR, PlansDBStructure.EVENT_BRING};
        String Selection = PlansDBStructure.MONTH + "=? and " + PlansDBStructure.YEAR + "=?";
        String [] SelectionArgs = {month,year};

        return database.query(PlansDBStructure.EVENT_TABLE_NAME,Projections,Selection,SelectionArgs,null,null,null);
    }

    //TODO: Hàm xóa plan
    public void  deleteEvent(String event, String place, String date, String time, String bring, SQLiteDatabase database){
        String selection = PlansDBStructure.EVENT + "=? and " + PlansDBStructure.EVENT_PLACE + "=? and " + PlansDBStructure.DATE+ "=? and " + PlansDBStructure.TIME + "=? and " + PlansDBStructure.EVENT_BRING + "=?";
        String[] selectionArg = {event,place,date,time,bring};
        database.delete(PlansDBStructure.EVENT_TABLE_NAME,selection,selectionArg);
    }

    //TODO: hàm update plan
    public void updateEvent(String date, String event, String time, String place, String bring, String notify, SQLiteDatabase database){

        ContentValues contentValues = new ContentValues();
        contentValues.put(PlansDBStructure.Notify,event);
        String Selection = PlansDBStructure.DATE + "=? and " + PlansDBStructure.EVENT + "=? and " + PlansDBStructure.TIME + "=? and " + PlansDBStructure.EVENT_PLACE + "=? and " + PlansDBStructure.EVENT_BRING + "=? ";
        String [] SelectionArgs = {date,event,time, place,bring};
        database.update(PlansDBStructure.EVENT_TABLE_NAME,contentValues,Selection,SelectionArgs);

    }

}
