package com.aasfencoders.womensafety.ui_fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.ForegroundService;

import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.ServiceDetector;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

// Track Me Fragment of the application, where we send location updates to others when we are in danger.
// For that, we first check for the permission, the GPS enabled, after everything is alright,
// we re-direct it to [ForegroundService.java], to upload location data in the background.
public class TrackMeFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap mMap;
    private Switch gpsSwitch;
    public SharedPreferences sharedPreferences;
    private LocationReceiver receiver;
    private LocationManager manager;
    boolean flag;

    public TrackMeFragment() {
        flag = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trackme, container, false);
        gpsSwitch = view.findViewById(R.id.gpsSwitch);
        ColorDrawable cd = new ColorDrawable(0xFFAD6400);
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(Color.parseColor("#804a00"));
            ((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(cd);
        }

        if (getContext() != null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        }

        receiver = new LocationReceiver();
        getContext().registerReceiver(receiver, new IntentFilter("GET_LOCATION_CUR"));

        // To check if location update service is running or not
        ServiceDetector serviceDetector = new ServiceDetector();
        if (!serviceDetector.isServiceRunning(getContext(), ForegroundService.class)) {
            gpsSwitch.setChecked(false);
        } else {
            gpsSwitch.setChecked(true);
        }

        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    // when switched is turned ON
                    if (getContext() != null) {
                        // first check network connection, if network detected, check for the permission.
                        // If enabled, check for the GPS Connection.
                        // Else, request for the location permission.
                        boolean state = CheckNetworkConnection.checkNetwork(getContext());
                        if (state) {
                            manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                checkGPS();
                            }
                        } else {
                            // network dialog is showed up when their is no network connectivity
                            NetworkDialog.showNetworkDialog(getContext());
                            gpsSwitch.setChecked(false);
                        }
                    }
                } else {
                    // when SWITCH is turned off
                    // close notification dialog
                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    Objects.requireNonNull(getContext()).sendBroadcast(it);
                    // stop the background service, i.e, stop sending location data
                    Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
                    getContext().stopService(serviceIntent);
                }
            }
        });

        return view;
    }

    // Requesting for the permission to be enabled by the user before fetching location data
    // Once granted, make them check GPS connection
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (getContext() != null) {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        checkGPS();
                    } else {
                        gpsSwitch.setChecked(false);
                        Toast.makeText(getContext(), getString(R.string.location_permission), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.location_permission), Toast.LENGTH_SHORT).show();
                gpsSwitch.setChecked(false);
            }
        }


    }

    // We check the GPS connection, if enabled re-direct to start the Foreground Service.
    // If not, start an intent and re-direct the user to settings asking them to open location settings.
    private void checkGPS() {
        if (getContext() != null) {
            manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsSwitch.setChecked(false);
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 1);
            } else {
                startService();
            }
        }

    }

    // After getting back from the Location setting page, it is rechecked for the GPS state.
    // If enabled, re-direct to start the Foreground Service.
    // Else, show an error toast.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsSwitch.setChecked(true);
                startService();
            } else {
                Toast.makeText(getContext(), getString(R.string.gps_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // called to set the map view on this attached fragment.
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

    // Called when the map view is ready and then we initialize it with the current context.
    public void onMapReady(GoogleMap googleMap) {
        if (getContext() != null) {
            MapsInitializer.initialize(getContext());
        }
        mMap = googleMap;
    }

    // start the foreground service to send the location data
    private void startService() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        if (getContext() != null) {
            ContextCompat.startForegroundService(getContext(), serviceIntent);
        }
    }

    // Get the location data from the [ForegroundService.class], and use it to set the marker on the Map.
    class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("GET_LOCATION_CUR")) {
                String Lat = intent.getStringExtra("lat_DATA");
                String Lng = intent.getStringExtra("lng_DATA");
                mMap.clear();
                float zoomLevel;
                LatLng userLocation = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));
                if (userLocation != null) {
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                    if (flag) {
                        flag = false;
                        zoomLevel = 17;
                    } else {
                        zoomLevel = mMap.getCameraPosition().zoom;
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel));
                }

            }
        }

    }

    @Override
    public void onStop() {
        if (mapView != null) {
            mapView.onPause();
            mapView.onStop();
            mapView.onDestroy();
            mapView = null;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.onPause();
            mapView.onStop();
            mapView.onDestroy();
            mapView = null;
        }
        super.onDestroyView();
    }

}
