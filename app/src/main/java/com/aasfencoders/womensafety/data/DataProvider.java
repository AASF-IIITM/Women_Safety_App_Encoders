package com.aasfencoders.womensafety.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.Objects;

import timber.log.Timber;

// This class is responsible for holding the CRUD operation on the database
public class DataProvider extends ContentProvider {

    private DataDbHelper mDbHelper;

    //Uri Matcher
    private static final int Data = 100;
    private static final int Data_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // URI matcher related to the whole table operation
        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY,DataContract.PATH_Data,Data);

        // URI matcher related to a single data operation
        sUriMatcher.addURI(DataContract.CONTENT_AUTHORITY,DataContract.PATH_Data + "/#",Data_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DataDbHelper(getContext());
        return true;
    }

    // Over-ridding the function which accounts for querying a database
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch(match){
            // return the whole cursor based on the query
            case Data:
                cursor = db.query(DataContract.DataEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,order);
                break;

            // return the cursor based on the query of the particular field
            case Data_ID:
                selection = DataContract.DataEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DataContract.DataEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,order);
                break;

            default:
                throw new IllegalArgumentException(uri + "INVALID");
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    // Over-ridding the function which accounts for returning the URI type
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case Data:
                return DataContract.DataEntry.CONTENT_LIST_TYPE;

            case Data_ID:
                return DataContract.DataEntry.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match" + match);
        }
    }

    // Over-ridding the function which accounts for inserting a row into the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case Data:
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                long id = db.insert(DataContract.DataEntry.TABLE_NAME,null,values);

                if(id == -1){
                    Timber.i(" Insertion failed");
                    return null;
                }else{
                    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,null);
                    return ContentUris.withAppendedId(uri,id);
                }
            default:
                throw new IllegalArgumentException("Insertion Failed!");
        }
    }

    // Over-ridding the function which accounts for deletion of row/rows in database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case Data:
                rowsDeleted = database.delete(DataContract.DataEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case Data_ID:
                selection = DataContract.DataEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(DataContract.DataEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    // Over-ridding the function which accounts for updating a row into the database
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case Data:
                return updateData(uri, contentValues, selection, selectionArgs);
            case Data_ID:
                selection = DataContract.DataEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateData(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateData(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(DataContract.DataEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
