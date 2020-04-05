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
import android.util.Log;

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

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    PendingIntent notifyPendingIntent;

    AlarmManager alarmManager;
    PendingIntent broadcast;
    Intent notificationIntent;

    String id;

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String Lat = data.get(getString(R.string.latitude));
        String Long = data.get(getString(R.string.longitude));
        String phone = data.get(getString(R.string.Phone));
        String stamp = data.get(getString(R.string.stamp));

        storeInDatabase(Lat , Long , phone , stamp);

    }

    private void storeInDatabase(String Lat, String Long , String phone , String stamp){

        String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
        String[] selectionArgs = new String[]{phone};

        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_STATUS};

        Cursor cursor = getContentResolver().query(DataContract.DataEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            int idColumnIndex = cursor.getColumnIndex(DataContract.DataEntry._ID);
            id = cursor.getString(idColumnIndex);
            int nameColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
            String name = cursor.getString(nameColumnIndex);
            int statusColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_STATUS);
            String status = cursor.getString(statusColumnIndex);

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            notificationIntent = new Intent(getApplicationContext(), updateBroadcastReceiver.class);
            notificationIntent.putExtra("phone", phone);
            broadcast = PendingIntent.getBroadcast(getApplicationContext(), Integer.parseInt(id), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+120*1000, broadcast);

            if(status.equals(getString(R.string.zero))){
                makeNotification(name);
            }

            ContentValues values = new ContentValues();
            values.put(DataContract.DataEntry.COLUMN_CURRENT_LAT, Lat);
            values.put(DataContract.DataEntry.COLUMN_CURRENT_LONG, Long);
            values.put(DataContract.DataEntry.COLUMN_STATUS, getString(R.string.one));
            values.put(DataContract.DataEntry.COLUMN_STAMP, stamp);

            Integer rowsAffected = getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values, selection, selectionArgs);
        }

        if(cursor != null){
            cursor.close();
        }

    }

    private void makeNotification(String name){

        final int requestCode = (getString(R.string.app_name) + " " + System.currentTimeMillis()).hashCode();

        Intent notifyIntent;
        notifyIntent = new Intent(getBaseContext(), MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notifyPendingIntent = PendingIntent.getActivity(getBaseContext(), requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    getString(R.string.notiChannelId),
                    getString(R.string.notiChannelName),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }


        String title = "Help!!! Am in Danger!";
        String body = "Track your matched contact " + name + " immediately";

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

        notificationManager.notify(Integer.parseInt(id), notificationBuilder.build());
    }

}
