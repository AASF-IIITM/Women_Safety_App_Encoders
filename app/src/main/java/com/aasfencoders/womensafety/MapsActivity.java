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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.aasfencoders.womensafety.data.DataContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap mMap;
    private Uri mCurrentDataUri;

    private TextView nameView;
    private TextView numberView;

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
                DataContract.DataEntry.COLUMN_CURRENT_LONG};

        return new CursorLoader(MapsActivity.this, mCurrentDataUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        int nameColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
        int numberColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_PHONE);
        int latColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_CURRENT_LAT);
        int longColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_CURRENT_LONG);

        String name = cursor.getString(nameColIndex);
        String number = cursor.getString(numberColIndex);
        String Lat = cursor.getString(latColIndex);
        String Long = cursor.getString(longColIndex);

        nameView.setText(name);
        numberView.setText(number);

        LatLng userLocation = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Long));
        mMap.addMarker(new MarkerOptions().position(userLocation).title(name + " Updated Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
