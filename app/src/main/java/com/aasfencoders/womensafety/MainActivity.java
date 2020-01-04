package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.util.data.PhoneNumberUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 1;

    private String mUserPhoneNumber;
    public static String ANONYMOUS;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    SharedPreferences sharedPreferences;

    private void onSignedInInitialize(String username) {
        Intent intent = new Intent(this,BottomNavigationActivity.class);
        intent.putExtra(getString(R.string.phone),username);
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
                Toast.makeText(this, getString(R.string.signIn), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.signOut), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ANONYMOUS = getString(R.string.ANONYMOUS);
        sharedPreferences = MainActivity.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    TelephonyManager tm = (TelephonyManager)MainActivity.this.getSystemService(MainActivity.this.TELEPHONY_SERVICE);
                    String countryCodeValue = tm.getNetworkCountryIso();
                    mUserPhoneNumber = "+" + PhoneNumberUtils.getCountryCode(countryCodeValue) + user.getPhoneNumber();
                    onSignedInInitialize(mUserPhoneNumber);

                    int firstTime = sharedPreferences.getInt(getString(R.string.firstform), 0);
                    if (firstTime == 0) {
                        checkConnection();
                    }else{
                        onSignedInInitialize(mUserPhoneNumber);
                    }


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

    private void checkConnection(){
        boolean state = CheckNetworkConnection.checkNetwork(MainActivity.this);
        if (state) {
            showForm();
        } else {
            if(NetworkDialog.showNetworkDialog(MainActivity.this)){
                checkConnection();
            }
        }
    }

    private void showForm(){
        
    }
}
