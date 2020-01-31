
//called when user press UPLOAD CANCEL to cancel upload , this is called to stop the service and notification

package com.aasfencoders.womensafety;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class NotificationCancelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
        Intent serviceIntent = new Intent(context, ExampleService.class);
        context.stopService(serviceIntent);
    }
}
