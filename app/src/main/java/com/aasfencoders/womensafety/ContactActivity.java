package com.aasfencoders.womensafety;


import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.ContactNameClass;
import com.aasfencoders.womensafety.adapter.ContactAdapter;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.warkiz.widget.ColorCollector;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ContactActivity extends AppCompatActivity {

    private ListView contactListView;
    private ProgressBar progressBar;
    private TextView loadingtextview;

    SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;
    int contactPosArray[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportActionBar().setTitle(getString(R.string.selectContact));

        IndicatorSeekBar seekBar = findViewById(R.id.seekbar);
        seekBar.setIndicatorTextFormat("${TICK_TEXT}");

        seekBar.customSectionTrackColor(new ColorCollector() {
            @Override
            public boolean collectSectionTrackColor(int[] colorIntArr) {
                //the length of colorIntArray equals section count
                colorIntArr[0] = getResources().getColor(R.color.color_blue, null);
                colorIntArr[1] = getResources().getColor(R.color.color_gray, null);
                colorIntArr[2] = getResources().getColor(R.color.color_green, null);
                colorIntArr[3] = getResources().getColor(R.color.color_red, null);
                colorIntArr[4] = getResources().getColor(R.color.color_green, null);
                colorIntArr[5] = getResources().getColor(R.color.color_yellow, null);
                colorIntArr[6] = getResources().getColor(R.color.color_blue, null);
                colorIntArr[7] = getResources().getColor(R.color.color_gray, null);
                colorIntArr[8] = getResources().getColor(R.color.color_green, null);
                colorIntArr[9] = getResources().getColor(R.color.color_red, null);
                colorIntArr[10] = getResources().getColor(R.color.color_green, null);
                colorIntArr[11] = getResources().getColor(R.color.color_yellow, null);
                colorIntArr[12] = getResources().getColor(R.color.color_blue, null);
                colorIntArr[13] = getResources().getColor(R.color.color_gray, null);
                colorIntArr[14] = getResources().getColor(R.color.color_green, null);
                colorIntArr[15] = getResources().getColor(R.color.color_red, null);
                colorIntArr[16] = getResources().getColor(R.color.color_green, null);
                colorIntArr[17] = getResources().getColor(R.color.color_yellow, null);
                colorIntArr[18] = getResources().getColor(R.color.color_blue, null);
                colorIntArr[19] = getResources().getColor(R.color.color_gray, null);
                colorIntArr[20] = getResources().getColor(R.color.color_green, null);
                colorIntArr[21] = getResources().getColor(R.color.color_red, null);
                colorIntArr[22] = getResources().getColor(R.color.color_green, null);
                colorIntArr[23] = getResources().getColor(R.color.color_yellow, null);
                colorIntArr[24] = getResources().getColor(R.color.color_blue, null);


                return true; //True if apply color , otherwise no change
            }
        });

        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                int cur_pos = contactPosArray[seekParams.progress - 1];
                contactListView.setSelection(cur_pos);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        contactListView = findViewById(R.id.contactNameListView);
        progressBar = findViewById(R.id.progress);
        loadingtextview = findViewById(R.id.progressTextViewContacts);
        sharedPreferences = ContactActivity.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();
        progressBar.setVisibility(View.VISIBLE);
        loadingtextview.setVisibility(View.VISIBLE);
        FetchContactAsyncTask task = new FetchContactAsyncTask();
        task.execute();
        getContactList();
    }

    private void getContactList() {

    }

    private class FetchContactAsyncTask extends AsyncTask<String, Integer, ArrayList<ContactNameClass>> {
        @Override
        protected ArrayList<ContactNameClass> doInBackground(String... args) {

            ArrayList<ContactNameClass> contactList = new ArrayList<ContactNameClass>();

            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

            if ((cur != null ? cur.getCount() : 0) > 0) {

                for (int i = 0; i < 26; i++) {
                    String id2 = getID(i);
                    String name = getName(i);
                    contactList.add(new ContactNameClass(id2, name));
                }


                while (cur != null && cur.moveToNext()) {
                    String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                    if (id != null && name != null) {
                        contactList.add(new ContactNameClass(id, name));
                    }

                }


                Collections.sort(contactList, new Comparator<ContactNameClass>() {
                    @Override
                    public int compare(ContactNameClass obj1, ContactNameClass obj2) {
                        return obj1.getName().compareToIgnoreCase(obj2.getName());
                    }
                });

                findPosContactGroup(contactList);

            }
            if (cur != null) {
                cur.close();
            }
            return contactList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(final ArrayList<ContactNameClass> contactList) {
            super.onPostExecute(contactList);
            progressBar.setVisibility(View.GONE);
            loadingtextview.setVisibility(View.GONE);
            ContactAdapter contactAdapter = new ContactAdapter(getBaseContext(), contactList, contactPosArray);
            contactListView.setAdapter(contactAdapter);
            contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position,
                                        long id) {
                    phone_number_display(contactList.get(position).getId(), contactList.get(position).getName());
                }
            });
        }

    }

    private void phone_number_display(String id, final String name) {


        if (id.length() > 6 && id.substring(0, 4).equals("CONT")) {
            return;
        }

        ArrayList<String> items = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, ContactsContract.Contacts._ID + " = ?",
                new String[]{id}, null);
        cur.moveToFirst();
        if (cur.getInt(cur.getColumnIndex(
                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
            Cursor pCur = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);
            while (pCur.moveToNext()) {
                String phoneNo = pCur.getString(pCur.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));

                phoneNo = phoneNo.replaceAll("\\s", "");


                boolean flagCommon = false;

                for (String item : items) {
                    if (item.contains(phoneNo))
                        flagCommon = true;
                }


                if (!flagCommon) {
                    String[] projection = {
                            DataContract.DataEntry._ID,
                            DataContract.DataEntry.COLUMN_NAME,
                            DataContract.DataEntry.COLUMN_PHONE};
                    String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
                    String[] selectionArgs = new String[]{phoneNo};

                    Cursor cursor = getContentResolver().query(DataContract.DataEntry.CONTENT_URI, projection, selection, selectionArgs, null);
                    if (!(cursor != null && cursor.getCount() > 0)) {

                        String phonewithCode = null;
                        if (phoneNo.charAt(0) != '+') {
                            String isocode = sharedPreferences.getString(getString(R.string.ISONUMBER), getString(R.string.defaultISOCodeNumber));
                            phonewithCode = isocode + phoneNo;
                            String selection2 = DataContract.DataEntry.COLUMN_PHONE + " =? ";
                            String[] selectionArgs2 = new String[]{phonewithCode};

                            Cursor cursor2 = getContentResolver().query(DataContract.DataEntry.CONTENT_URI, projection, selection2, selectionArgs2, null);
                            if (!(cursor2 != null && cursor2.getCount() > 0)) {
                                if (phoneNo.charAt(0) == '+') {
                                    items.add(phoneNo);
                                } else {
                                    String isocode2 = sharedPreferences.getString(getString(R.string.ISONUMBER), getString(R.string.defaultISOCodeNumber));
                                    items.add(isocode2 + phoneNo);
                                }
                            }
                            if (cursor2 != null) {
                                cursor2.close();
                            }
                        } else {
                            if (phoneNo.charAt(0) == '+') {
                                items.add(phoneNo);
                            } else {
                                String isocode3 = sharedPreferences.getString(getString(R.string.ISONUMBER), getString(R.string.defaultISOCodeNumber));
                                items.add(isocode3 + phoneNo);
                            }
                        }

                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }

            }
            pCur.close();


            new LovelyChoiceDialog(this, R.style.TintTheme)
                    .setTopColorRes(R.color.dialogColour)
                    .setTitle(name)
                    .setIcon(R.drawable.ic_contact_phone_white_24dp)
                    .setItemsMultiChoice(items, new LovelyChoiceDialog.OnItemsSelectedListener<String>() {
                        @Override
                        public void onItemsSelected(List<Integer> positions, List<String> items) {
                            if (items.isEmpty()) {
                                Toast.makeText(getBaseContext(), R.string.noContactSelected, Toast.LENGTH_SHORT).show();
                            } else {
                                boolean state = CheckNetworkConnection.checkNetwork(ContactActivity.this);
                                if (state) {
                                    updateDatabase(items, name);
                                } else {
                                    Toast.makeText(ContactActivity.this, getString(R.string.noInternetMessage), Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    })
                    .setConfirmButtonText(R.string.confirm)
                    .show();


        } else {
            Toast.makeText(getBaseContext(), R.string.noPhoneNumberPresent, Toast.LENGTH_SHORT).show();
            cur.close();
        }
    }

    private void updateDatabase(List<String> items, String name) {

        final String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));
        final String current_user_name = sharedPreferences.getString(getString(R.string.username), getString(R.string.error));
        if (current_user_number.equals(R.string.error) || current_user_name.equals(getString(R.string.error))) {
            Toast.makeText(ContactActivity.this, getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {
            final SweetAlertDialog loadingDialog;
            loadingDialog = new SweetAlertDialog(ContactActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            loadingDialog.getProgressHelper().setBarColor(Color.parseColor("#8a1ca6"));
            loadingDialog.setTitleText(getString(R.string.contactDialogString));
            loadingDialog.setCancelable(false);

            Iterator iterator = items.iterator();
            loadingDialog.show();
            while (iterator.hasNext()) {
                final String sent_phone_number = iterator.next().toString();
                DatabaseReference rootRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.sent));
                String key = mFirebaseReference.push().getKey();
                Map<String, Object> value = new HashMap<>();
                value.put(getString(R.string.name), name);
                value.put(getString(R.string.number), sent_phone_number);
                value.put(getString(R.string.status), getString(R.string.invited));
                rootRef.child(key).setValue(value);

                Map<String, Object> value2 = new HashMap<>();
                value2.put(getString(R.string.name), current_user_name);
                value2.put(getString(R.string.number), current_user_number);

                mFirebaseReference.child(getString(R.string.invitation)).child(sent_phone_number).child(key).setValue(value2);

                ContentValues values = new ContentValues();
                values.put(DataContract.DataEntry.COLUMN_NAME, name);
                values.put(DataContract.DataEntry.COLUMN_PHONE, sent_phone_number);
                values.put(DataContract.DataEntry.COLUMN_STATUS, getString(R.string.zero));
                values.put(DataContract.DataEntry.COLUMN_STATUS_INVITATION, getString(R.string.invited));

                Uri uri = getContentResolver().insert(DataContract.DataEntry.CONTENT_URI, values);

            }
            loadingDialog.dismissWithAnimation();
        }

    }


    private void findPosContactGroup(ArrayList<ContactNameClass> Contact) {
        int i = -1;
        int j = 0;
        contactPosArray = new int[27];
        for (ContactNameClass contact : Contact) {
            if (contact.getId().length() > 5 && contact.getId().substring(0, 4).equals("CONT")) {
                contactPosArray[++i] = j;
            }
            if (i == 25)
                break;
            j++;
        }

    }

    private String getID(int no) {
        return getString(R.string.CID) + Integer.toString(no + 1);
    }

    private String getName(int no) {
        return Character.toString((char) (65 + no));
    }

}
