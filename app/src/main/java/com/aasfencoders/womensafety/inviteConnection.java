package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.InviteSentClass;
import com.aasfencoders.womensafety.adapter.InviteAdapter;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class inviteConnection extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    View view;
    ListView listView;
    SharedPreferences sharedPreferences;

    DatabaseReference mFirebaseReference;

    private InviteAdapter mCursorAdapter;

    private void contact() {
        Intent intent = new Intent(inviteConnection.this, ContactActivity.class);
        startActivity(intent);
    }

    private void checkContactPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            contact();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    contact();
                } else {
                    Toast.makeText(this, getString(R.string.permissionDenied), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.permissionDenied), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_connection);
        getSupportActionBar().setTitle(getString(R.string.inviteConnectionHeading));

        mFirebaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = inviteConnection.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);

        view = (View) findViewById(R.id.empty_invite_view);
        listView = (ListView) findViewById(R.id.listOfInvitedConnections);

        mCursorAdapter = new InviteAdapter(this, null);
        listView.setAdapter(mCursorAdapter);
        listView.setEmptyView(view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkContactPermission();
            }
        });

        checkConnection();

        getSupportLoaderManager().initLoader(1, null, inviteConnection.this);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_PHONE,
                DataContract.DataEntry.COLUMN_STATUS_INVITATION};

        String selection = DataContract.DataEntry.COLUMN_STATUS_INVITATION + "=?" + " OR " + DataContract.DataEntry.COLUMN_STATUS_INVITATION + "=?";
        String[] selectionArgs = new String[]{getString(R.string.invited), getString(R.string.rejected)};

        return new CursorLoader(this, DataContract.DataEntry.CONTENT_URI, projection, selection, selectionArgs, DataContract.DataEntry._ID + " DESC");

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void checkConnection() {
        boolean state = CheckNetworkConnection.checkNetwork(inviteConnection.this);
        if (state) {
            fetchRejectedContacts();
        }
    }

    private void fetchRejectedContacts() {

        String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.sent));
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    InviteSentClass callClassObj = ds.getValue(InviteSentClass.class);

                    if (callClassObj.getStatus().equals(getString(R.string.rejected))) {
                        ContentValues values = new ContentValues();
                        values.put(DataContract.DataEntry.COLUMN_STATUS_INVITATION, getString(R.string.rejected));
                        values.put(DataContract.DataEntry.COLUMN_STATUS, getString(R.string.zero));

                        String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
                        String[] selectionArgs = new String[]{callClassObj.getNumber()};

                        Integer rowsAffected = getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values, selection, selectionArgs);
                        ds.getRef().removeValue();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        userNameRef.addListenerForSingleValueEvent(eventListener);


    }
}
