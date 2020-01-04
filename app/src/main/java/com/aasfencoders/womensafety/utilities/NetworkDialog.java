package com.aasfencoders.womensafety.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;

import com.aasfencoders.womensafety.R;

public class NetworkDialog {

    NetworkDialog(){

    }

    public static boolean showNetworkDialog(Context mContext) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(Html.fromHtml("<h6><font color='#465ba6'>You are offline!</font></h6>"));
        builder.setMessage( Html.fromHtml("Connect your phone to internet/WiFi connection and then press on RETRY button."));
        builder.setIcon(R.drawable.ic_warning_pink_24dp);
        builder.setNegativeButton(Html.fromHtml("<h7><font color='#465ba6'>RETRY</font></h7>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                return true;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
