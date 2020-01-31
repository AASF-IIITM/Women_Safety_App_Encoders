package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.aasfencoders.womensafety.ui_fragment.ExtrasFragment;
import com.aasfencoders.womensafety.ui_fragment.HomeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackMeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackOthersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

public class BottomNavigationActivity extends AppCompatActivity {


    Fragment selectedFragment;
    private SharedPreferences sharedPreferences;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    sharedPreferences.edit().putString(getString(R.string.NAVITEM),getString(R.string.NAVITEM0)).apply();
                    selectedFragment = new HomeFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    break;
                case R.id.navigation_trackOther:
                    sharedPreferences.edit().putString(getString(R.string.NAVITEM),getString(R.string.NAVITEM0)).apply();
                    selectedFragment = new TrackOthersFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    break;
                case R.id.navigation_trackMe:
                    if(sharedPreferences.getString(getString(R.string.NAVITEM) ,getString(R.string.NAVITEM0)).equals(getString(R.string.NAVITEM0))){
                        selectedFragment = new TrackMeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        sharedPreferences.edit().putString(getString(R.string.NAVITEM),getString(R.string.NAVITEM1)).apply();
                    }
                    break;
                case R.id.navigation_extra:
                    selectedFragment = new ExtrasFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    sharedPreferences.edit().putString(getString(R.string.NAVITEM),getString(R.string.NAVITEM0)).apply();
                    break;
            }

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        sharedPreferences = BottomNavigationActivity.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        if (sharedPreferences.getString(getString(R.string.SIMSET), getString(R.string.NO)).equals(getString(R.string.NO))) {
            checkSIM();
        }

    }

    private void checkSIM() {
        String[] items_sim = {
                getString(R.string.SIM1),
                getString(R.string.SIM2),
                getString(R.string.SIMNO)
        };

        new LovelyChoiceDialog(BottomNavigationActivity.this)
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
                    }
                })
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.edit().putString(getString(R.string.NAVITEM),getString(R.string.NAVITEM0)).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putString(getString(R.string.NAVITEM),getString(R.string.NAVITEM0)).apply();
    }

}
