package com.aasfencoders.womensafety.api;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.aasfencoders.womensafety.MainActivity;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.updateBroadcastReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.Map;

// This Messaging Service detects for any incoming location data from other users, and once data fetched,
// It is stored in the local database with STATUS = 1, i.e, currently that contact is in danger.
// We also aware the user with a pop-up notification.
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    PendingIntent notifyPendingIntent;

    AlarmManager alarmManager;
    PendingIntent broadcast;
    Intent notificationIntent;

    String id;

    // whenever new location data appears, this function onMessageReceived is called.
    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String Lat = data.get(getString(R.string.latitude));
        String Long = data.get(getString(R.string.longitude));
        String phone = data.get(getString(R.string.Phone));
        String stamp = data.get(getString(R.string.stamp));

        // after parsing the local data, we are storing it into the local database.
        storeInDatabase(Lat, Long, phone, stamp);

    }

    // received location data is stored in database
    private void storeInDatabase(String Lat, String Long, String phone, String stamp) {

        String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
        String[] selectionArgs = new String[]{phone};
        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_STATUS};

        // creating the cursor with the appropriate selection and projection
        Cursor cursor = getContentResolver().query(DataContract.DataEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            // First to create the notification we need basic details of the contact, i.e, name
            int idColumnIndex = cursor.getColumnIndex(DataContract.DataEntry._ID);
            id = cursor.getString(idColumnIndex);
            int nameColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
            String name = cursor.getString(nameColumnIndex);
            int statusColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_STATUS);
            String status = cursor.getString(statusColumnIndex);

            // After receiving the location data, for about 120 seconds he is notified as currently in danger.
            // After 120 seconds, we change his status back to zero, i.e, not currently in danger
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            notificationIntent = new Intent(getApplicationContext(), updateBroadcastReceiver.class);
            notificationIntent.putExtra("phone", phone);
            broadcast = PendingIntent.getBroadcast(getApplicationContext(), Integer.parseInt(id), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 120 * 1000, broadcast);

            // make the notification call to the local user
            if (status.equals(getString(R.string.zero))) {
                makeNotification(name);
            }

            // update values in the local database
            ContentValues values = new ContentValues();
            values.put(DataContract.DataEntry.COLUMN_CURRENT_LAT, Lat);
            values.put(DataContract.DataEntry.COLUMN_CURRENT_LONG, Long);
            values.put(DataContract.DataEntry.COLUMN_STATUS, getString(R.string.one));
            values.put(DataContract.DataEntry.COLUMN_STAMP, stamp);

            Integer rowsAffected = getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values, selection, selectionArgs);
        }

        if (cursor != null) {
            cursor.close();
        }

    }

    // Notification shown to make the user aware that a matched connection of his is in danger currently.
    private void makeNotification(String name) {

        final int requestCode = (getString(R.string.app_name) + " " + System.currentTimeMillis()).hashCode();

        // Pending intent of what action to perform when user click on the notification
        Intent notifyIntent;
        notifyIntent = new Intent(getBaseContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notifyPendingIntent = PendingIntent.getActivity(getBaseContext(), requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    getString(R.string.notiChannelId),
                    getString(R.string.notiChannelName),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }


        String title = "Help!!! Am in Danger!";
        String body = "Track your matched contact " + name + " immediately";

        // notification builder
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.notiChannelId))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.shield))
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(false);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.shield);
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.shield);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        // notification notified to the user
        notificationManager.notify(Integer.parseInt(id), notificationBuilder.build());
    }
}
