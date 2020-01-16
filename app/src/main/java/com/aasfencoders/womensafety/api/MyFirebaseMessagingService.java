package com.aasfencoders.womensafety.api;


import android.content.ContentValues;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.data.DataContract;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String Lat = data.get(getString(R.string.latitude));
        String Long = data.get(getString(R.string.longitude));
        String phone = data.get(getString(R.string.Phone));

        storeInDatabase(Lat , Long , phone);

    }

    private void storeInDatabase(String Lat, String Long , String phone){

        ContentValues values = new ContentValues();
        values.put(DataContract.DataEntry.COLUMN_CURRENT_LAT, Lat);
        values.put(DataContract.DataEntry.COLUMN_CURRENT_LONG, Long);
        values.put(DataContract.DataEntry.COLUMN_STATUS, "one");

        String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
        String[] selectionArgs = new String[]{phone};

        Integer rowsAffected = getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values, selection, selectionArgs);


    }

}
