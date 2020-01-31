package com.aasfencoders.womensafety.ui_fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.Class.DataModel;
import com.aasfencoders.womensafety.Class.RootModel;
import com.aasfencoders.womensafety.ExampleService;
import com.aasfencoders.womensafety.MainActivity;
import com.aasfencoders.womensafety.MapsActivity;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.api.ApiClient;
import com.aasfencoders.womensafety.api.ApiInterface;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.matchedConnection;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Callback;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class TrackMeFragment extends Fragment implements OnMapReadyCallback {


    private MapView mapView;
    private GoogleMap mMap;
    private Switch gpsSwitch;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startService();
                } else {
                    gpsSwitch.setChecked(false);
                }
            } else {
                gpsSwitch.setChecked(false);
            }
        }


    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trackme, container, false);
        gpsSwitch = view.findViewById(R.id.gpsSwitch);
        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    if (getContext() != null) {
                        boolean state = CheckNetworkConnection.checkNetwork(getContext());
                        if (state) {

                            final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);


                            if (Build.VERSION.SDK_INT < 23) {
                                // sendSMS();
                                startService();
                            } else {
                                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                } else {

                                    // sendSMS();
                                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(intent);
                                    }
                                    startService();
                                }
                            }


                        } else {
                            NetworkDialog.showNetworkDialog(getContext());
                            gpsSwitch.setChecked(false);
                        }
                    }
                } else {
                    startService();
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

    private void startService(){
        Intent serviceIntent = new Intent(getContext(), ExampleService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }

}
