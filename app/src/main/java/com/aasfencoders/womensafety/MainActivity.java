package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.util.data.PhoneNumberUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    ProgressBar progressBar;
    TextView textView;

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

    public boolean showNetworkDialog(Context mContext) {
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
                checkConnection();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress);
        textView  = findViewById(R.id.loadingUser);

        ANONYMOUS = getString(R.string.ANONYMOUS);
        sharedPreferences = MainActivity.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseReference = mFirebaseDatabase.getReference();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mUserPhoneNumber = user.getPhoneNumber();
                    sharedPreferences.edit().putString(getString(R.string.number),mUserPhoneNumber).apply();
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
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            showForm();
        } else {
            showNetworkDialog(MainActivity.this);
        }
    }

    private void showForm(){
        new LovelyTextInputDialog(this, R.style.TintTheme)
                .setTopColorRes(R.color.dialogColour)
                .setTitle(R.string.text_input_title)
                .setMessage(R.string.text_input_message)
                .setIcon(R.drawable.ic_person_outline_white_36dp)
                .setInputFilter(R.string.text_input_error_message, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        return !text.isEmpty();
                    }
                })
                .setConfirmButton(R.string.upload, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        mFirebaseReference.child(getString(R.string.users)).child(mUserPhoneNumber).child(getString(R.string.profile)).child(getString(R.string.name)).setValue(text.trim());
                        sharedPreferences.edit().putInt(getString(R.string.firstform),1).apply();
                        sharedPreferences.edit().putString(getString(R.string.username),text.trim()).apply();
                        Toast.makeText(MainActivity.this, "Data Stored in Server", Toast.LENGTH_SHORT).show();
                        onSignedInInitialize(mUserPhoneNumber);
                    }
                })
                .show();
    }
}
