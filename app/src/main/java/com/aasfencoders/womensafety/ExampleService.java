package com.aasfencoders.womensafety;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.aasfencoders.womensafety.Class.DataModel;
import com.aasfencoders.womensafety.Class.RootModel;
import com.aasfencoders.womensafety.api.ApiClient;
import com.aasfencoders.womensafety.api.ApiInterface;
import com.aasfencoders.womensafety.data.DataContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;

import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

public class ExampleService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ArrayList<String> phoneNumber;
    private ArrayList<String> phoneName;

    private String userPhoneNumber;

    private SharedPreferences sharedPreferences;

    private static final int ID_SERVICE = 101;

    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    Notification notification;
    String channelId;

    private Boolean flag;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getBaseContext().getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        userPhoneNumber = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_PHONE};

        Cursor cursor;

        flag = true;

        phoneName = new ArrayList<String>();
        phoneNumber = new ArrayList<String>();
        String selection = DataContract.DataEntry.COLUMN_STATUS_INVITATION + " =? ";
        String[] selectionArgs = new String[]{getString(R.string.matched)};
        cursor = getBaseContext().getContentResolver().query(DataContract.DataEntry.CONTENT_URI, projection, selection, selectionArgs, null);
        int nameColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
        int numberColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_PHONE);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameColumnIndex);
                String number = cursor.getString(numberColumnIndex);
                phoneName.add(name);
                phoneNumber.add(number);
            }
        }

        cursor.close();

        Intent intentAction = new Intent(getBaseContext(), NotificationCancelReceiver.class);
        PendingIntent cancelP = PendingIntent.getBroadcast(getBaseContext(), 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        notificationBuilder = new NotificationCompat.Builder(getBaseContext(), channelId);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setPriority(PRIORITY_MAX);
        notificationBuilder.setOnlyAlertOnce(true);
        notificationBuilder.setContentTitle("Uploading Location...");
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.shield));
        notificationBuilder.setContentText("Cancel when you want to stop uploading location");
        notificationBuilder.setOnlyAlertOnce(true);
        notificationBuilder.addAction(R.drawable.ic_warning_pink_24dp, "Cancel Upload", cancelP);
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.shield);
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimary));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.shield);
        }

        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "_ID1";
        String channelName = "Uploading status of Location";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        notification = notificationBuilder.build();
        startForeground(ID_SERVICE, notification);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                sendDataToFragment(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                sendLocationToMatchedContacts(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));

                if (flag) {
                    flag = false;
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        sendSMS();
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        return Service.START_NOT_STICKY;
    }

    private void sendDataToFragment(String lat, String lng) {
        Intent sendLoc = new Intent();
        sendLoc.setAction("GET_LOCATION_CUR");
        sendLoc.putExtra("lat_DATA", lat);
        sendLoc.putExtra("lng_DATA", lng);
        sendBroadcast(sendLoc);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendSMS() {
        int i;

        final ArrayList<Integer> simCardList = new ArrayList<>();
        SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(getApplicationContext());
        @SuppressLint("MissingPermission") final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager
                .getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            int subscriptionId = subscriptionInfo.getSubscriptionId();
            simCardList.add(subscriptionId);
        }

        int smsToSendFrom;

        String val = sharedPreferences.getString(getString(R.string.SIM), getString(R.string.SIMNO));
        if (!val.equals(getString(R.string.SIMNO))) {
            String messageToSend = "I AM IN DANGER. Track me immediately in Women Safety App by connecting your phone to network connection";
            if (val.equals(getString(R.string.SIM1))) {
                smsToSendFrom = simCardList.get(0);
            } else {
                smsToSendFrom = simCardList.get(1);
            }
            for (i = 0; i < phoneName.size(); i++) {
                SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom).sendTextMessage(phoneNumber.get(i), null, messageToSend, null, null);
            }
        }

    }

    private void sendLocationToMatchedContacts(String Lat, String Long) {

        int i;

        for (i = 0; i < phoneNumber.size(); i++) {
            int length = phoneNumber.get(i).length();
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Date dateObject = new Date();
            long date = dateObject.getTime();
            RootModel rootModel = new RootModel("/topics/" + phoneNumber.get(i).substring(1, length), new DataModel(Lat, Long, userPhoneNumber, java.lang.Long.toString(date)));
            retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendLocation(rootModel);

            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                }
            });
        }

    }

}
