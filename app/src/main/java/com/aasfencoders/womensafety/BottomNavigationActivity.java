package com.aasfencoders.womensafety;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.adapter.ReceiveAdapter;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.ui_fragment.ExtrasFragment;
import com.aasfencoders.womensafety.ui_fragment.HomeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackMeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackOthersFragment;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.Collections;

import cn.pedant.SweetAlert.SweetAlertDialog;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class BottomNavigationActivity extends AppCompatActivity {


    Fragment selectedFragment;
    private SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;

    private void checkConnection() {
        boolean state = CheckNetworkConnection.checkNetwork(BottomNavigationActivity.this);
        if (state) {
            fetchReceivedContacts();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        BottomNavigation bottomNavigation = (BottomNavigation) findViewById(R.id.nav_view);
        bottomNavigation.setMenuItemSelectionListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int i, int i1, boolean b) {

                switch (i) {
                    case R.id.navigation_home:
                        selectedFragment = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        break;
                    case R.id.navigation_trackMe:
                        selectedFragment = new TrackMeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        break;
                    case R.id.navigation_trackOther:
                        selectedFragment = new TrackOthersFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        break;
                    case R.id.navigation_extra:
                        selectedFragment = new ExtrasFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        break;
                }
            }

            @Override
            public void onMenuItemReselect(int i, int i1, boolean b) {

            }
        });


        sharedPreferences = BottomNavigationActivity.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        String firstInstall = sharedPreferences.getString(getString(R.string.firstInstall), "");
        if (firstInstall.equals("")) {
            checkConnection();
        }


    }

    private void fetchReceivedContacts() {
        String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        if (current_user_number.equals(R.string.error)) {
            Toast.makeText(BottomNavigationActivity.this, getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {

            final SweetAlertDialog pDialog = new SweetAlertDialog(BottomNavigationActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Matched contacts");
            pDialog.setCancelable(false);
            pDialog.show();

            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.matchedall));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ReceiveClass callClassObj = ds.getValue(ReceiveClass.class);
                        if (callClassObj != null) {
                            String name = callClassObj.getName();
                            String number = callClassObj.getNumber();

                            String nameOfContact = null;
                            if (ContextCompat.checkSelfPermission(BottomNavigationActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                nameOfContact = getContactName(BottomNavigationActivity.this, number);

                                if (nameOfContact == null) {
                                    String phone = number;
                                    String code = sharedPreferences.getString(getString(R.string.ISONUMBER), getString(R.string.defaultISOCodeNumber));
                                    String phonewithCode = phone.replace(code, "");
                                    String nameOfContactWithoutCOde = getContactName(BottomNavigationActivity.this, phonewithCode);

                                    if (nameOfContactWithoutCOde != null) {
                                        nameOfContact = nameOfContactWithoutCOde;
                                    }
                                }
                            }

                            if (nameOfContact == null) {
                                nameOfContact = name;
                            }
                            ContentValues values = new ContentValues();
                            values.put(DataContract.DataEntry.COLUMN_NAME, nameOfContact);
                            values.put(DataContract.DataEntry.COLUMN_PHONE, number);
                            values.put(DataContract.DataEntry.COLUMN_STATUS, getString(R.string.zero));
                            values.put(DataContract.DataEntry.COLUMN_STATUS_INVITATION, getString(R.string.matched));
                            getContentResolver().insert(DataContract.DataEntry.CONTENT_URI, values);
                        }

                    }
                    sharedPreferences.edit().putString(getString(R.string.firstInstall), getString(R.string.done)).apply();
                    pDialog.dismissWithAnimation();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);

        }
    }

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

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.edit().putString(getString(R.string.NAVITEM), getString(R.string.NAVITEM0)).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putString(getString(R.string.NAVITEM), getString(R.string.NAVITEM0)).apply();
    }

    @Override
    public void onBackPressed() {
        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.red_btn_bg_color)
                .setButtonsColorRes(R.color.colorPrimaryDark)
                .setIcon(R.drawable.ic_arrow_back_black_24dp)
                .setTitle(R.string.backMessage)
                .setPositiveButton(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BottomNavigationActivity.this.finishAndRemoveTask();
                    }
                })
                .setNegativeButton(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show();

    }

}
