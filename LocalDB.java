package com.deepwares.checkpointdwi.localdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.deepwares.checkpointdwi.entities.BACRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prasannapulagam on 2/22/18.
 */

public class CheckBACDB extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "CheckBACDatabase";
    // Temples table name
    private static final String CHECHBAC_RESULT_TABLE = "CheckBACResultTable";

    private static final String CHECKBAC_ACTIVE_TABLE = "CheckBACTable";
    private static final String NOTIFICATION_DATE = "notification_date";
    private static final String NOTIFICATION_TIME = "notification_time";
    private static final String NOTIFICATION_STATUS = "notification_status";
    private static final String BAC_RESULT_DATE = "bac_result_date";
    private static final String BAC_RESULT_TIME = "bac_result_time";
    private static final String BAC_RESULT_VALUE = "bac_result_value";
    private static final String BAC_RESULT_LATITUDE = "bac_result_latitude";
    private static final String BAC_RESULT_LONGITUDE = "bac_result_longitude";
    private static final String BAC_RESULT_VIDEO = "bac_result_video";
    private static final String BAC_RESULT_STATUS = "bac_result_status";
    private static final String FACE_MATCH_FAILED_BAC = "face_match_failed_bac";
    private static final String KEY_ID = "id";
    private static final String DEVICE_NAME = "device_name";

    public CheckBACDB(Context applicationContext) {
        super(applicationContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create Table for BACtrack Result
        String CREATE_CHECHBAC_RESULT_TABLE = "CREATE TABLE " + CHECHBAC_RESULT_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BAC_RESULT_DATE + " TEXT,"
                + BAC_RESULT_TIME + " TEXT,"
                + BAC_RESULT_VALUE + " TEXT,"
                + BAC_RESULT_LATITUDE + " TEXT,"
                + BAC_RESULT_LONGITUDE + " TEXT,"
                + BAC_RESULT_VIDEO + " TEXT,"
                + BAC_RESULT_STATUS + " TEXT,"
                + FACE_MATCH_FAILED_BAC + " TEXT,"
                + DEVICE_NAME + " TEXT " + ")";

        db.execSQL(CREATE_CHECHBAC_RESULT_TABLE);

        //create CheckBAC Active table
        String CREATE_CHECHBAC_ACTIVE_TABLE = "CREATE TABLE " + CHECKBAC_ACTIVE_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NOTIFICATION_DATE + " TEXT,"
                + NOTIFICATION_TIME + " TEXT,"
                + NOTIFICATION_STATUS + " TEXT " + ")";
        db.execSQL(CREATE_CHECHBAC_ACTIVE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CHECHBAC_RESULT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CHECKBAC_ACTIVE_TABLE);
    }

    //add the CheckBAC Active list
    public void addChecBACResultList(BACRecord bacResultRecord) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, bacResultRecord.getIndexID());
        values.put(BAC_RESULT_DATE, bacResultRecord.getDate());
        values.put(BAC_RESULT_TIME, bacResultRecord.getTime());
        values.put(BAC_RESULT_VALUE, bacResultRecord.getBac());
        values.put(BAC_RESULT_LATITUDE, bacResultRecord.getLatitudeValue());
        values.put(BAC_RESULT_LONGITUDE, bacResultRecord.getLongitudeValue());
        values.put(BAC_RESULT_VIDEO, bacResultRecord.getVideoFile());
        values.put(BAC_RESULT_STATUS, bacResultRecord.getStatus());
        values.put(FACE_MATCH_FAILED_BAC, bacResultRecord.isFaceMatchFailBAC());
        values.put(DEVICE_NAME, bacResultRecord.getDeviceName());

        //Inserting Row
        long result = database.insert(CHECHBAC_RESULT_TABLE, null, values);
        //close the local database
        database.close();

    }

    //get the list
    public List<BACRecord> getAllBACResultList() {
        List<BACRecord> bacRecordList = new ArrayList<BACRecord>();
        try {
            //Select Query
            String query = "SELECT * FROM " + CHECHBAC_RESULT_TABLE;
            SQLiteDatabase database = this.getWritableDatabase();
            Cursor cursor = database.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    BACRecord bacResultRecord = new BACRecord();
                    bacResultRecord.setIndexID(cursor.getString(0));
                    bacResultRecord.setDate(cursor.getString(1));
                    bacResultRecord.setTime(cursor.getString(2));
                    bacResultRecord.setBac(cursor.getDouble(3));
                    bacResultRecord.setLatitudeValue(cursor.getDouble(4));
                    bacResultRecord.setLongitudeValue(cursor.getDouble(5));
                    bacResultRecord.setVideoFile(cursor.getString(6));
                    bacResultRecord.setStatus(cursor.getString(7));
                    bacResultRecord.setFaceMatchFailBAC(Boolean.parseBoolean(cursor.getString(8)));
                    bacResultRecord.setDeviceName(cursor.getString(9));
                    bacRecordList.add(bacResultRecord);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bacRecordList;
    }

    //remove the local database list
    public void removeBACResultList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + CHECHBAC_RESULT_TABLE);
        //close the local database
        db.close();
    }

    public int updateRecord(BACRecord bacResultRecord) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, bacResultRecord.getIndexID());
        values.put(BAC_RESULT_DATE, bacResultRecord.getDate());
        values.put(BAC_RESULT_TIME, bacResultRecord.getTime());
        values.put(BAC_RESULT_VALUE, bacResultRecord.getBac());
        values.put(BAC_RESULT_LATITUDE, bacResultRecord.getLatitudeValue());
        values.put(BAC_RESULT_LONGITUDE, bacResultRecord.getLongitudeValue());
        values.put(BAC_RESULT_VIDEO, bacResultRecord.getVideoFile());
        values.put(BAC_RESULT_STATUS, bacResultRecord.getStatus());
        return db.update(CHECHBAC_RESULT_TABLE, values, KEY_ID + "=?", new String[]{String.valueOf(bacResultRecord.getIndexID())});
    }
}


