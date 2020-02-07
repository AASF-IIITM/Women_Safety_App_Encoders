package com.aasfencoders.womensafety.ui_fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.BottomNavigationActivity;
import com.aasfencoders.womensafety.ExampleService;

import com.aasfencoders.womensafety.NotificationCancelReceiver;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.ServiceDetector;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class TrackMeFragment extends Fragment implements OnMapReadyCallback {


    private MapView mapView;
    private GoogleMap mMap;
    private Switch gpsSwitch;
    public SharedPreferences sharedPreferences;
    private LocationReceiver receiver;
    private LocationManager manager ;
    boolean flag;

    public TrackMeFragment() {
        flag = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    checkGPS();
                } else {
                    gpsSwitch.setChecked(false);
                    Toast.makeText(getContext(),getString(R.string.location_permission),Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(),getString(R.string.location_permission),Toast.LENGTH_SHORT).show();
                gpsSwitch.setChecked(false);
            }
        }


    }

    private void checkGPS() {
        manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsSwitch.setChecked(false);
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,1);
        }
        else {
            startService();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gpsSwitch.setChecked(true);
                startService();
            }
            else {
                Toast.makeText(getContext(),getString(R.string.gps_permission),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trackme, container, false);
        gpsSwitch = view.findViewById(R.id.gpsSwitch);
        ColorDrawable cd = new ColorDrawable(0xFFAD6400);
        if(getActivity() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.parseColor("#804a00"));
            ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(cd);
        }

        if (getContext() != null) {

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        }

        receiver = new LocationReceiver();
        getContext().registerReceiver(receiver, new IntentFilter("GET_LOCATION_CUR"));

        ServiceDetector serviceDetector = new ServiceDetector();
        if (!serviceDetector.isServiceRunning(getContext(), ExampleService.class)) {
            gpsSwitch.setChecked(false);
        } else {
            gpsSwitch.setChecked(true);
        }

        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (getContext() != null) {
                        boolean state = CheckNetworkConnection.checkNetwork(getContext());
                        if (state) {

                            manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                            if (Build.VERSION.SDK_INT < 23) {
                                // sendSMS();
                                startService();
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
                            gpsSwitch.setChecked(false);
                        }
                    }
                } else {
                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    getContext().sendBroadcast(it);
                    Intent serviceIntent = new Intent(getContext(), ExampleService.class);
                    getContext().stopService(serviceIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        if (getContext() != null) {
            MapsInitializer.initialize(getContext());
        }
        mMap = googleMap;

    }

    private void startService() {
        Intent serviceIntent = new Intent(getContext(), ExampleService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }


    class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("GET_LOCATION_CUR")) {
                String Lat = intent.getStringExtra("lat_DATA");
                String Lng = intent.getStringExtra("lng_DATA");
                mMap.clear();
                float zoomLevel;
                LatLng userLocation = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));
                if(userLocation != null) {
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your current Location"));
                    if(flag==true)
                    {
                        flag = false;
                        zoomLevel = 17;
                    }
                    else
                    {
                        zoomLevel = mMap.getCameraPosition().zoom;
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel));
                }

            }
        }

    }

    @Override
    public void onStop() {
        if(mapView != null){
            mapView.onPause();
            mapView.onStop();
            mapView.onDestroy();
            mapView = null;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if(mapView != null){
            mapView.onPause();
            mapView.onStop();
            mapView.onDestroy();
            mapView = null;
        }
        super.onDestroyView();
    }
}
