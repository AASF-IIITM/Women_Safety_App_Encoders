package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.view.WindowManager;

import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    private String mUserPhoneNumber;
    public static String ANONYMOUS;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    SharedPreferences sharedPreferences;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFirebaseReference;


    private void onSignedInInitialize(String username) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber NumberProto = phoneUtil.parse(username, "IN");
            int countrycode = NumberProto.getCountryCode();
            sharedPreferences.edit().putString(getString(R.string.ISONUMBER), "+" + countrycode).apply();
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        intent.putExtra(getString(R.string.phone), username);
        startActivity(intent);
        this.finish();
    }

    private void onSignedOutCleanup() {
        mUserPhoneNumber = ANONYMOUS;
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            startCheck();
        }
    }

    private void startCheck() {

        if (sharedPreferences.getString(getString(R.string.SIMSET), getString(R.string.NO)).equals(getString(R.string.NO))) {
            checkSIM();
        } else {
            int firstTime = sharedPreferences.getInt(getString(R.string.firstform), 0);
            if (firstTime == 0) {
                checkConnection();
            } else {
                onSignedInInitialize(mUserPhoneNumber);
            }
        }
    }

    public boolean showNetworkDialog(Context mContext) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle(Html.fromHtml("<h6><font color='#465ba6'>You are offline!</font></h6>"));
        builder.setMessage(Html.fromHtml("Connect your phone to internet/WiFi connection and then press on RETRY button."));
        builder.setIcon(R.drawable.ic_warning_pink_24dp);
        builder.setNegativeButton(Html.fromHtml("<h7><font color='#465ba6'>RETRY</font></h7>"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                checkConnection();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ANONYMOUS = getString(R.string.ANONYMOUS);
        sharedPreferences = MainActivity.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseReference = mFirebaseDatabase.getReference();
        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) || (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)) {
            String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(MainActivity.this, permission, 1);
        }

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUserPhoneNumber = user.getPhoneNumber();
                    sharedPreferences.edit().putString(getString(R.string.userNumber), mUserPhoneNumber).apply();
                    startCheck();

                } else {
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void checkConnection() {
        boolean state = CheckNetworkConnection.checkNetwork(MainActivity.this);
        if (state) {
            showForm();
        } else {
            showNetworkDialog(MainActivity.this);
        }
    }

    private void showForm() {
        new LovelyTextInputDialog(this, R.style.TintTheme)
                .setTopColorRes(R.color.dialogColour)
                .setTitle(R.string.text_input_title)
                .setMessage(R.string.text_input_message)
                .setCancelable(false)
                .setIcon(R.drawable.ic_person_outline_white_36dp)
                .setInputFilter(R.string.text_input_error_message, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        return !text.isEmpty();
                    }
                })
                .setConfirmButton(R.string.upload, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(final String text) {
                        mFirebaseReference.child(getString(R.string.users)).child(mUserPhoneNumber).child(getString(R.string.profile)).child(getString(R.string.name)).setValue(text.trim());
                        sharedPreferences.edit().putInt(getString(R.string.firstform), 1).apply();
                        sharedPreferences.edit().putString(getString(R.string.username), text.trim()).apply();
                        int length = mUserPhoneNumber.length();
                        FirebaseMessaging.getInstance().subscribeToTopic(mUserPhoneNumber.substring(1, length));
                        onSignedInInitialize(mUserPhoneNumber);
                    }
                })
                .show();
    }


    private void checkSIM() {
        String[] items_sim = {
                getString(R.string.SIM1),
                getString(R.string.SIM2),
                getString(R.string.SIMNO)
        };

        new LovelyChoiceDialog(MainActivity.this)
                .setTopColorRes(R.color.dialogColour)
                .setTitle(R.string.sms_title)
                .setIcon(R.drawable.ic_textsms_black_24dp)
                .setMessage(R.string.sms_message)
                .setCancelable(false)
                .setItems(items_sim, new LovelyChoiceDialog.OnItemSelectedListener<String>() {
                    @Override
                    public void onItemSelected(int position, String item) {
                        sharedPreferences.edit().putString(getString(R.string.SIM), item).apply();
                        sharedPreferences.edit().putString(getString(R.string.SIMSET), getString(R.string.YES)).apply();
                        int firstTime = sharedPreferences.getInt(getString(R.string.firstform), 0);
                        if (firstTime == 0) {
                            checkConnection();
                        } else {
                            onSignedInInitialize(mUserPhoneNumber);
                        }

                    }
                })
                .show();
    }

}
