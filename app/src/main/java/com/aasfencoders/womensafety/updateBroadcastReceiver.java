package com.aasfencoders.womensafety;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.aasfencoders.womensafety.data.DataContract;

public class updateBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String phone;
        phone = intent.getStringExtra("phone");

        String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
        String[] selectionArgs = new String[]{phone};

        ContentValues values = new ContentValues();
        values.put(DataContract.DataEntry.COLUMN_STATUS, context.getString(R.string.zero));

        Integer rowsAffected = context.getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values, selection, selectionArgs);
    }
}
