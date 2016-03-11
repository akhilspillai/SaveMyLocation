package com.trace.save_my_location.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trace.save_my_location.models.LocationModel;

import java.util.ArrayList;
import java.util.List;

public class LocalDB {
    private final String[] COLUMNS_LOCATION = {SQLiteHelper.ROW_ID,
            SQLiteHelper.COLUMN_ADDRESS, SQLiteHelper.COLUMN_LONGITUDE,
            SQLiteHelper.COLUMN_LATITUDE, SQLiteHelper.COLUMN_NOTIFICATION};

    private Context context = null;
    private SQLiteHelper dbHelper;

    public LocalDB(Context context) {
        this.context = context;
    }

    public SQLiteDatabase open() {
        SQLiteDatabase database;
        dbHelper = new SQLiteHelper(context);
        database = dbHelper.getWritableDatabase();
        return database;
    }

    public void close() {
        dbHelper.close();
    }

    public long insert(String address, double longitude, double latitude, String notification) {
        SQLiteDatabase database = open();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_ADDRESS, address);
        values.put(SQLiteHelper.COLUMN_LONGITUDE, longitude);
        values.put(SQLiteHelper.COLUMN_LATITUDE, latitude);
        values.put(SQLiteHelper.COLUMN_TIME, System.currentTimeMillis());
        values.put(SQLiteHelper.COLUMN_NOTIFICATION, notification);
        long columnId = database.insert(SQLiteHelper.TABLE_LOCATION, null, values);
        close();
        return columnId;
    }

    public void updateNotification(long id, String notification) {
        SQLiteDatabase database = open();
        ContentValues args = new ContentValues();
        args.put(SQLiteHelper.COLUMN_NOTIFICATION, notification);
        database.update(SQLiteHelper.TABLE_LOCATION, args,
                "_id = ?", new String[]{String.valueOf(id)});
        close();
    }

    public List<LocationModel> retrieveAllSavedLocations() {
        List<LocationModel> locationModels = new ArrayList<>();
        LocationModel locationModel;
        SQLiteDatabase database = open();
        Cursor cursor = database.query(SQLiteHelper.TABLE_LOCATION, COLUMNS_LOCATION,
                null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                locationModel = new LocationModel();
                locationModel.setId(cursor.getLong(0));
                locationModel.setAddress(cursor.getString(1));
                locationModel.setLongitude(cursor.getDouble(2));
                locationModel.setLatitude(cursor.getDouble(3));
                locationModel.setNotification(cursor.getString(4));
                locationModels.add(locationModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return locationModels;
    }

    public LocationModel retrieveSavedLocation(long id) {
        LocationModel locationModel = null;
        SQLiteDatabase database = open();
        Cursor cursor = database.query(SQLiteHelper.TABLE_LOCATION, COLUMNS_LOCATION,
                "_id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor.moveToFirst()) {
            locationModel = new LocationModel();
            locationModel.setId(cursor.getLong(0));
            locationModel.setAddress(cursor.getString(1));
            locationModel.setLongitude(cursor.getDouble(2));
            locationModel.setLatitude(cursor.getDouble(3));
            locationModel.setNotification(cursor.getString(4));
        }
        cursor.close();
        close();
        return locationModel;
    }

    public void deleteLocation(long id) {
        LocationModel locationModel = null;
        SQLiteDatabase database = open();
        database.delete(SQLiteHelper.TABLE_LOCATION,
                "_id = ?", new String[]{String.valueOf(id)});
        close();
    }

}
