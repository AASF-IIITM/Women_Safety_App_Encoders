package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aasfencoders.womensafety.api.PoliceApiClient;
import com.aasfencoders.womensafety.api.PoliceApiInterface;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;

public class ShowPolice extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView police_station_count;
    TextView police_station_title;
    TextView police_station_fetch;
    ProgressBar progressBar;


    private void startLocationUpdate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Log.i("############", "4");
                retrofit(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                locationManager.removeUpdates(locationListener);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }
    }

    private void retrofit(String Latitude, String Longitude) {
        String url = "https://maps.googleapis.com/maps/api/place/search/" + "json?location=" + Latitude + "," + Longitude + "&rankby=distance&types=police&sensor=false&key=AIzaSyDzbVaqexiRvDpSt3t9oO2kwEu34Qbm3QI";
        PoliceApiInterface apiService = PoliceApiClient.getClient().create(PoliceApiInterface.class);
        retrofit2.Call<JsonObject> responseBodyCall = apiService.fetchCount(url);


        responseBodyCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                Log.i("############", "6");
                if (response.body() != null && response.code() == 200) {

                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().toString());
                        String item = jsonObject.getString(getString(R.string.results));

                        JSONArray arr;
                        arr = new JSONArray(item);

                        mMap.clear();
                        int i;
                        for (i = 0; i < arr.length(); i++) {
                            JSONObject content = arr.getJSONObject(i);
                            String name = content.get(getString(R.string.name3)).toString();

                            String vicinity = content.get(getString(R.string.vicinity)).toString();

                            String geometry = content.get(getString(R.string.geometry)).toString();

                            JSONObject jsonObject1 = new JSONObject(geometry);
                            String location = jsonObject1.get(getString(R.string.location)).toString();

                            JSONObject jsonObject2 = new JSONObject(location);

                            String lat = jsonObject2.get(getString(R.string.lat)).toString();
                            String lng = jsonObject2.get(getString(R.string.lng)).toString();

                            LatLng markerPoliceStation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                            mMap.addMarker(new MarkerOptions().position(markerPoliceStation).title(name).snippet(vicinity));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPoliceStation, 12));
                        }
                        police_station_title.setVisibility(View.VISIBLE);
                        police_station_count.setVisibility(View.VISIBLE);
                        police_station_fetch.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.GONE);

                        police_station_count.setText(Integer.toString(i) + " " + getString(R.string.police_station));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i("############", "7");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_police);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        police_station_count = findViewById(R.id.police_station_count);
        police_station_title = findViewById(R.id.police_station_title);
        police_station_fetch = findViewById(R.id.police_station_fetching);
        progressBar = findViewById(R.id.progressBar_police);
        police_station_title.setVisibility(View.INVISIBLE);
        police_station_count.setVisibility(View.INVISIBLE);
        police_station_fetch.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        startLocationUpdate();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
