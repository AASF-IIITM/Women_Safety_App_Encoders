package com.aasfencoders.womensafety;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.aasfencoders.womensafety.api.PoliceApiClient;
import com.aasfencoders.womensafety.api.PoliceApiInterface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_police);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PoliceApiInterface apiService = PoliceApiClient.getClient().create(PoliceApiInterface.class);
        retrofit2.Call<JsonObject> responseBodyCall = apiService.fetchCount();

        responseBodyCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<JsonObject> call, retrofit2.Response<JsonObject> response) {

                if (response.body() != null && response.code() == 200) {

                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().toString());
                        String item = jsonObject.getString(getString(R.string.results));

                        JSONArray arr;
                        arr = new JSONArray(item);

                        mMap.clear();

                        for(int i =0; i < arr.length() ; i++){
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
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPoliceStation, 12));
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
