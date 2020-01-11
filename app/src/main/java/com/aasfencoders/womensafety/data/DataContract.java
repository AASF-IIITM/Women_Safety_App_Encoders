package com.aasfencoders.womensafety.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DataContract {

    //adding URI
    public static final String CONTENT_AUTHORITY = "com.example.android.Data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_Data = "Data";


    public static final class DataEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of Data_
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_Data;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single Data_
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_Data;

        // Uri to access the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_Data);

        //table name and column names
        public static final String TABLE_NAME = "Data";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_CURRENT_LAT = "currentlat";
        public static final String COLUMN_CURRENT_LONG = "currentlong";
        public static final String COLUMN_SOURCE_LAT = "sourcelat";
        public static final String COLUMN_SOURCE_LONG = "sourcelong";
        public static final String COLUMN_DEST_LAT = "destlat";
        public static final String COLUMN_DEST_LONG = "destlong";
        
    }
}
