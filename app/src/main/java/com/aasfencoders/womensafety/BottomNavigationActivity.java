package com.aasfencoders.womensafety;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.aasfencoders.womensafety.ui_fragment.ExtrasFragment;
import com.aasfencoders.womensafety.ui_fragment.HomeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackMeFragment;
import com.aasfencoders.womensafety.ui_fragment.TrackOthersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class BottomNavigationActivity extends AppCompatActivity {


    Fragment selectedFragment;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        BottomNavigation bottomNavigation = (BottomNavigation) findViewById(R.id.nav_view);
        bottomNavigation.setMenuItemSelectionListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int i, int i1, boolean b) {

                switch (i) {
                    case R.id.navigation_home:
                        selectedFragment = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        break;
                    case R.id.navigation_trackOther:
                        selectedFragment = new TrackOthersFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        break;
                    case R.id.navigation_trackMe:
                        selectedFragment = new TrackMeFragment();
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
        sharedPreferences.edit().putString(getString(R.string.NAVITEM), getString(R.string.NAVITEM0)).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.edit().putString(getString(R.string.NAVITEM), getString(R.string.NAVITEM0)).apply();
    }

}
