package com.aasfencoders.womensafety.ui_fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.MainActivity;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.ShowPolice;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class ExtrasFragment extends Fragment {

    private RadioGroup radioGroup;
    private SharedPreferences sharedPreferences;
    private Button showPolice;
    private LocationManager locationManager;
    private MediaPlayer mediaPlayer;
    private Button mPause, mPlay;

    private Spinner numberCodes;
    private String isoCodeNumber;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (getContext() != null) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        checkGPS();
                    }
                }
            }
        }
    }

    private void getPermission() {

        boolean state = false;
        if (getContext() != null) {
            state = CheckNetworkConnection.checkNetwork(getContext());
        }
        if (state) {
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT < 23) {
                startShowPolice();
            } else {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    checkGPS();
                }

            }

        } else {
            NetworkDialog.showNetworkDialog(getContext());
        }

    }

    private void checkGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,1);
        }
        else {
            startShowPolice();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startShowPolice();
            }
        }
    }

    private void startShowPolice() {
        Intent intent = new Intent(getContext(), ShowPolice.class);
        startActivity(intent);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_extra, container, false);
        radioGroup = view.findViewById(R.id.sim_radio_group);
        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        }

        String checked = sharedPreferences.getString(getString(R.string.SIM), getString(R.string.SIMNO));

        isoCodeNumber = getString(R.string.defaultISOCodeNumber);


        numberCodes = (Spinner) view.findViewById(R.id.number_codes2);
        ArrayAdapter<String> numberISO = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, getContext().getResources().getStringArray(R.array.CountryCodes));
        numberCodes.setAdapter(numberISO);

        String pos = sharedPreferences.getString(getString(R.string.ISOPOSE), getString(R.string.NAVITEM0));

        numberCodes.setSelection(Integer.parseInt(pos));

        numberCodes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] splited = numberCodes.getSelectedItem().toString().split(" ");
                isoCodeNumber = splited[0];
                sharedPreferences.edit().putString(getString(R.string.ISONUMBER), isoCodeNumber).apply();
                sharedPreferences.edit().putString(getString(R.string.ISOPOSE), Integer.toString(i)).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        showPolice = view.findViewById(R.id.showPolice);

        showPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermission();
            }
        });

        if (checked.equals(getString(R.string.SIM1))) {
            radioGroup.check(R.id.sim1);
        } else if (checked.equals(getString(R.string.SIM2))) {
            radioGroup.check(R.id.sim2);
        } else {
            radioGroup.check(R.id.sim_No);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.sim1:
                        sharedPreferences.edit().putString(getString(R.string.SIM), getString(R.string.SIM1)).apply();
                        break;
                    case R.id.sim2:
                        sharedPreferences.edit().putString(getString(R.string.SIM), getString(R.string.SIM2)).apply();
                        break;
                    case R.id.sim_No:
                        sharedPreferences.edit().putString(getString(R.string.SIM), getString(R.string.SIMNO)).apply();
                        break;
                }
            }
        });
        mPause = view.findViewById(R.id.media_pause);
        mPlay = view.findViewById(R.id.media_play);

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.police_siren);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager;
                if(getContext() != null){
                    audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                    if(audioManager != null){
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0);
                    }
                }

                if(mediaPlayer == null)
                {
                    mediaPlayer = MediaPlayer.create(getContext(), R.raw.police_siren);
                }
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
        });
        mPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    mediaPlayer.pause();
                    releaseMediaPlayer();
                }
            }
        });
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
