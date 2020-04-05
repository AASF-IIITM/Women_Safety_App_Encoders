package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.aasfencoders.womensafety.data.DataContract;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.security.AccessController.doPrivileged;
import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap mMap;
    private Uri mCurrentDataUri;

    private TextView nameView;
    private TextView numberView;
    private TextView stampDate;
    private TextView stampTime;
    private TextView addresstext;
    float zoomLevel;
    boolean flag;

    public MapsActivity() {
        flag = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        mCurrentDataUri = intent.getData();
        int key = intent.getIntExtra("to_map",-1);
        TextView fragment_title = findViewById(R.id.fragment_title);
        switch (key) {
            case 0:fragment_title.setText(getString(R.string.updated_location));
            break;
            case 1:fragment_title.setText(getString(R.string.saved_location));
            break;
        }

        nameView = findViewById(R.id.nameTrackOther);
        numberView = findViewById(R.id.numberTrackOther);
        stampDate = findViewById(R.id.stampDateTrackOther);
        stampTime = findViewById(R.id.stampTimeTrackOther);
        addresstext = findViewById(R.id.geocoder_track_other);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getSupportLoaderManager().initLoader(1, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_PHONE,
                DataContract.DataEntry.COLUMN_CURRENT_LAT,
                DataContract.DataEntry.COLUMN_CURRENT_LONG,
                DataContract.DataEntry.COLUMN_STAMP};

        return new CursorLoader(MapsActivity.this, mCurrentDataUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mMap.clear();
        cursor.moveToFirst();

        int nameColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
        int numberColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_PHONE);
        int latColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_CURRENT_LAT);
        int longColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_CURRENT_LONG);
        int stampColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_STAMP);

        String name = cursor.getString(nameColIndex);
        String number = cursor.getString(numberColIndex);
        String Lat = cursor.getString(latColIndex);
        String Long = cursor.getString(longColIndex);
        String stamp = cursor.getString(stampColIndex);

        Date dateObject = new Date(java.lang.Long.parseLong(stamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY, MMM, dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String date = dateFormat.format(dateObject);
        String time = timeFormat.format(dateObject);

        nameView.setText(name);
        numberView.setText(number);
        stampTime.setText(time);
        stampDate.setText(date);


        LatLng userLocation = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Long));
        if(userLocation != null) {
            mMap.addMarker(new MarkerOptions().position(userLocation).title(name + " Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            if(flag)
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


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(Double.parseDouble(Lat), Double.parseDouble(Long), 1);
            if (addressList != null && addressList.size() > 0) {
                String address = "";
                Address completeAddress = addressList.get(0);
                address = completeAddress.getFeatureName() + "," + completeAddress.getLocality() + "," + completeAddress.getAdminArea() + "," + completeAddress.getPostalCode() + "," + completeAddress.getCountryName();
                addresstext.setText(address);
            }
            else {
                addresstext.setText(getString(R.string.noAddress));
            }
        } catch (IOException e) {
            e.printStackTrace();
            addresstext.setText(getString(R.string.noAddress));

        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

}
