package com.aasfencoders.womensafety.adapter;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.data.DataContract;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

// Adapter to populate values in the receiveAdapter in [ReceivedConnection.java]
public class ReceiveAdapter extends ArrayAdapter<ReceiveClass> {

    private Context mContext;
    private FirebaseFunctions firebaseFunction;
    private ReceiveClass currentCall;
    private SharedPreferences sharedPreferences;
    private Button accept;
    private Button reject;
    private TextView status;
    private ArrayList<ReceiveClass> receivedList;
    private int pos;

    public ReceiveAdapter(@NonNull Context context, ArrayList<ReceiveClass> receiveList) {
        super(context, 0, receiveList);
        mContext = context;
        firebaseFunction = FirebaseFunctions.getInstance();
        receivedList = receiveList;
        sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.package_name), Context.MODE_PRIVATE);
    }

    //  bind views(children) to it
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.single_receive_contact_item, parent, false);

        }
        accept = listItemView.findViewById(R.id.acceptButton);
        reject = listItemView.findViewById(R.id.rejectButton);
        status = listItemView.findViewById(R.id.person_receive_status);
        showButton();

        currentCall = getItem(position);
        pos = position;

        TextView name = listItemView.findViewById(R.id.person_receive_name);
        TextView number = listItemView.findViewById(R.id.person_receive_number);
        name.setText(currentCall.getName());
        number.setText(currentCall.getNumber());

        // button to accept the contact
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCall = getItem(position);
                String name = currentCall.getName();
                String phone = currentCall.getNumber();

                ContentValues values2 = new ContentValues();
                values2.put(DataContract.DataEntry.COLUMN_STATUS_INVITATION, mContext.getString(R.string.matched));
                values2.put(DataContract.DataEntry.COLUMN_STATUS, mContext.getString(R.string.zero));

                String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
                String[] selectionArgs = new String[]{phone};

                // search for the contact saved name in the device,
                // using the phone number once with country code and another time without the country code
                int rowsAffected = mContext.getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values2, selection, selectionArgs);
                if (rowsAffected == 0) {
                    String nameOfContact = null;
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        nameOfContact = getContactName(getContext(), phone);

                        if (nameOfContact == null) {
                            String code = sharedPreferences.getString(mContext.getString(R.string.ISONUMBER), mContext.getString(R.string.defaultISOCodeNumber));
                            String phonewithCode = phone.replace(code, "");
                            String nameOfContactWithoutCode = getContactName(getContext(), phonewithCode);

                            if (nameOfContactWithoutCode != null) {
                                nameOfContact = nameOfContactWithoutCode;
                            }
                        }
                    }

                    if (nameOfContact == null) {
                        nameOfContact = name;
                    }

                    // insert the contact in the local database as matched contact
                    ContentValues values = new ContentValues();
                    values.put(DataContract.DataEntry.COLUMN_NAME, nameOfContact);
                    values.put(DataContract.DataEntry.COLUMN_PHONE, phone);
                    values.put(DataContract.DataEntry.COLUMN_STATUS, mContext.getString(R.string.zero));
                    values.put(DataContract.DataEntry.COLUMN_STATUS_INVITATION, mContext.getString(R.string.matched));
                    mContext.getContentResolver().insert(DataContract.DataEntry.CONTENT_URI, values);
                }
                callFirebaseFunction(name, phone, mContext.getString(R.string.accept), position);

            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCall = getItem(position);
                String name = currentCall.getName();
                String phone = currentCall.getNumber();
                callFirebaseFunction(name, phone, mContext.getString(R.string.reject), position);

            }
        });

        return listItemView;
    }

    // function to search for the contact name using the phone number
    private String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }


    private void showButton() {
        accept.setVisibility(View.VISIBLE);
        reject.setVisibility(View.VISIBLE);
        status.setVisibility(View.INVISIBLE);
    }

    // after accepting/rejecting the contact, delete that contact information from the received connection list after 0.5 sec
    private void callTimer(final int position) {
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                deleteFromList(position);
            }
        }.start();
    }

    // delea
    private void deleteFromList(int position) {
        receivedList.remove(receivedList.get(position));
        notifyDataSetChanged();
    }

    // called a backend cloud function with the parameters, which eventually sets the value in the Firebase database
    private void callFirebaseFunction(String source_name, String source_no, final String selection, final int position) {

        String target_no = sharedPreferences.getString(mContext.getString(R.string.userNumber), mContext.getString(R.string.error));
        String target_name = sharedPreferences.getString(mContext.getString(R.string.username), mContext.getString(R.string.error));

        if (source_name.equals(mContext.getString(R.string.error)) || source_no.equals(mContext.getString(R.string.error))) {
            Toast.makeText(mContext, mContext.getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("source_no", source_no);
            data.put("target_no", target_no);
            data.put("selection", selection);
            data.put("source_name", source_name);
            data.put("target_name", target_name);

            // dialog shown to user about acceptance/rejection
            final SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            switch (selection) {
                case "accept":
                    pDialog.setTitleText("Accepting request...");
                    break;
                case "reject":
                    pDialog.setTitleText("Rejecting request...");
                    break;
            }
            pDialog.setCancelable(false);
            pDialog.show();

            firebaseFunction
                    .getHttpsCallable("sent_status")
                    .call(data)
                    .continueWith(new Continuation<HttpsCallableResult, String>() {
                        @Override
                        public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                            // dialog dismissed and success Toast displayed
                            pDialog.dismissWithAnimation();
                            String result = (String) task.getResult().getData();
                            if (result != null) {
                                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                            }
                            callTimer(position);
                            return result;
                        }
                    });
        }

    }
}
