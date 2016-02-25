package com.trace.save_my_location.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trace.save_my_location.utils.Constants;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_LOCATION = "location";
    public static final String ROW_ID = "_id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_NOTIFICATION = "notification";
    public static final String COLUMN_TIME = "time";
    private static final String LOCATION_TABLE_CREATE = "create table "
            + TABLE_LOCATION + "( " + ROW_ID + " integer primary key,"
            + COLUMN_ADDRESS + " text,"
            + COLUMN_LATITUDE + " real,"
            + COLUMN_LONGITUDE + " real,"
            + COLUMN_TIME + " integer,"
            + COLUMN_NOTIFICATION + " text);";

    private static final int DATABASE_VERSION = 1;


    public SQLiteHelper(Context context) {
        super(context, Constants.DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(LOCATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS " + SQLiteHelper.TABLE_LOCATION);
        }
    }

}

