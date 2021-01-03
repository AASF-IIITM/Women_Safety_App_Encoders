package com.aasfencoders.womensafety;

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
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.ui_fragment.ExtrasFragment;
import com.aasfencoders.womensafety.ui_fragment.HomeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackMeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackOthersFragment;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import cn.pedant.SweetAlert.SweetAlertDialog;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

// This activity is used as the background screen over which all the major fragments are placed,
// i.e, Home Fragment, Track Me Fragment, Track Other Fragment, Extra Fragment
public class BottomNavigationActivity extends AppCompatActivity {
    Fragment selectedFragment;
    private SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        // Setting up fragments of the bottom navigation menu.
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

        // Also after first time installation, it fetches for any pre-matched contacts previously.
        String firstInstall = sharedPreferences.getString(getString(R.string.firstInstall), "");
        if (firstInstall.equals("")) {
            checkConnection();
        }
    }

    // connection is checked, and if yes, contacts are fetched.
    private void checkConnection() {
        boolean state = CheckNetworkConnection.checkNetwork(BottomNavigationActivity.this);
        if (state) {
            fetchReceivedContacts();
        }
    }

    // contacts fetching are done through here.
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

            // Data is checked from the reference [ Users -> NUMBER -> matchedall ]
            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.matchedall));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // On data match, traverse through all the child, and then insert it into local database.
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ReceiveClass callClassObj = ds.getValue(ReceiveClass.class);
                        if (callClassObj != null) {
                            String name = callClassObj.getName();
                            String number = callClassObj.getNumber();

                            String nameOfContact = null;
                            // Search for the local contact name in the smartphone if permission is enabled.
                            // Check in the device twice, once with phone code and another time without phone code.
                            if (ContextCompat.checkSelfPermission(BottomNavigationActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                nameOfContact = getContactName(BottomNavigationActivity.this, number);

                                if (nameOfContact == null) {
                                    String phone = number;
                                    String code = sharedPreferences.getString(getString(R.string.ISONUMBER), getString(R.string.defaultISOCodeNumber));
                                    String phoneWithCode = phone.replace(code, "");
                                    String nameOfContactWithoutCode = getContactName(BottomNavigationActivity.this, phoneWithCode);

                                    if (nameOfContactWithoutCode != null) {
                                        nameOfContact = nameOfContactWithoutCode;
                                    }
                                }
                            }

                            if (nameOfContact == null) {
                                nameOfContact = name;
                            }

                            // insert the values into local database
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

    // Contact name is searched for the fetched contact number if exist
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

    // Exit Application confirmation Dialog box
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

    // Creating the top-right menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigationtoolbar_menu, menu);
        return true;
    }

    // Item selection of the top-right menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.info) {
            showInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Showing the Info dialog, i.e, the credits of the application
    private void showInfoDialog() {
        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.red_btn_bg_color)
                .setButtonsColorRes(R.color.colorPrimaryDark)
                .setIcon(R.drawable.ic_face_white_72dp)
                .setTitle(R.string.infotitle)
                .setMessage(R.string.infomessage)
                .setPositiveButton(R.string.okay, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show();
    }

}
