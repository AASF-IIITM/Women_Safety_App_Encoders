package com.aasfencoders.womensafety.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Database table creation and updation.
public class DataDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Data.db";
    private static final int DATABASE_VERSION = 1;

    public DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // SQL create table command is executed here
        String SQL_CREATE_Data_TABLE =  "CREATE TABLE " + DataContract.DataEntry.TABLE_NAME + " ("
                + DataContract.DataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DataContract.DataEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + DataContract.DataEntry.COLUMN_PHONE + " TEXT NOT NULL, "
                + DataContract.DataEntry.COLUMN_STATUS + " TEXT NOT NULL, "
                + DataContract.DataEntry.COLUMN_CURRENT_LAT + " TEXT NOT NULL DEFAULT 'EMPTY', "
                + DataContract.DataEntry.COLUMN_CURRENT_LONG + " TEXT NOT NULL DEFAULT 'EMPTY', "
                + DataContract.DataEntry.COLUMN_STAMP + " TEXT NOT NULL DEFAULT 'EMPTY', "
                + DataContract.DataEntry.COLUMN_STATUS_INVITATION + " TEXT NOT NULL DEFAULT 'EMPTY');";

        sqLiteDatabase.execSQL(SQL_CREATE_Data_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // left empty
    }
}
