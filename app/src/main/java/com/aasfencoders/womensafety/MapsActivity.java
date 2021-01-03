package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import com.aasfencoders.womensafety.data.DataContract;
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

// Maps activity to display the received location data from the matched connections.
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
        int key = intent.getIntExtra("to_map", -1);
        TextView fragment_title = findViewById(R.id.fragment_title);
        // Two situation calls up this [MapsActivity.java]
        // Key = 0 : Tracking user current location
        // Key = 1 : Displaying last location sent by the user
        switch (key) {
            case 0:
                fragment_title.setText(getString(R.string.updated_location));
                break;
            case 1:
                fragment_title.setText(getString(R.string.saved_location));
                break;
        }

        // initializing the view objects
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

    // Query the database to find the user locations.
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

    // Whenever location data gets updated into the database, this function is called up.
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mMap.clear();
        cursor.moveToFirst();

        // Declare each column Index of that data cursor
        int nameColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
        int numberColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_PHONE);
        int latColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_CURRENT_LAT);
        int longColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_CURRENT_LONG);
        int stampColIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_STAMP);

        // Get the Values present in those column
        String name = cursor.getString(nameColIndex);
        String number = cursor.getString(numberColIndex);
        String Lat = cursor.getString(latColIndex);
        String Long = cursor.getString(longColIndex);
        String stamp = cursor.getString(stampColIndex);

        // format the timestamp data
        Date dateObject = new Date(java.lang.Long.parseLong(stamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY, MMM, dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String date = dateFormat.format(dateObject);
        String time = timeFormat.format(dateObject);

        // set the view with the data
        nameView.setText(name);
        numberView.setText(number);
        stampTime.setText(time);
        stampDate.setText(date);

        // Now set the location marker on the fetched latitude and longitude with the correct zoom level
        LatLng userLocation = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Long));
        if (userLocation != null) {
            mMap.addMarker(new MarkerOptions().position(userLocation).title(name + " Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            // This flag is to set the zoom level to 17 the first time, for next time it takes its current zoom level
            if (flag) {
                flag = false;
                zoomLevel = 17;
            } else {
                zoomLevel = mMap.getCameraPosition().zoom;
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel));
        }

        // Parse the latitude and longitude to get the approximate address of the user
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(Double.parseDouble(Lat), Double.parseDouble(Long), 1);
            if (addressList != null && addressList.size() > 0) {
                String address = "";
                Address completeAddress = addressList.get(0);
                address = completeAddress.getFeatureName() + "," + completeAddress.getLocality() + "," + completeAddress.getAdminArea() + "," + completeAddress.getPostalCode() + "," + completeAddress.getCountryName();
                addresstext.setText(address);
            } else {
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
