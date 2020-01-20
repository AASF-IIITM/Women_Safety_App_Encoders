package com.aasfencoders.womensafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.MatchedClass;
import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.adapter.MatchedCursorAdapter;
import com.aasfencoders.womensafety.adapter.ReceiveAdapter;
import com.aasfencoders.womensafety.data.DataContract;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class matchedConnection extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    View view;
    ListView listView;
    SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;

    private View progress;

    private void checkConnection() {
        boolean state = CheckNetworkConnection.checkNetwork(matchedConnection.this);
        if (state) {
            fetchMatchedContacts();
            progress.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.INVISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    MatchedCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched_connection);

        getSupportActionBar().setTitle(getString(R.string.matchedConnectionHeading));

        sharedPreferences = matchedConnection.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        view = (View) findViewById(R.id.empty_matched_view);
        listView = (ListView) findViewById(R.id.listOfInvitedConnections);
        listView.setEmptyView(view);

        mCursorAdapter = new MatchedCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        progress = findViewById(R.id.progress_view);
        progress.setVisibility(View.INVISIBLE);

        getSupportLoaderManager().initLoader(1, null, matchedConnection.this);

        FloatingActionButton reload = (FloatingActionButton) findViewById(R.id.fab_reload_matched);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
                listView.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void fetchMatchedContacts() {

        String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        if (current_user_number.equals(R.string.error)) {
            Toast.makeText(matchedConnection.this, getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {

            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.matched));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        MatchedClass callClassObj = ds.getValue(MatchedClass.class);

                        ContentValues values = new ContentValues();
                        values.put(DataContract.DataEntry.COLUMN_STATUS_INVITATION, getString(R.string.matched));
                        values.put(DataContract.DataEntry.COLUMN_STATUS, getString(R.string.zero));

                        String selection = DataContract.DataEntry.COLUMN_PHONE + " =? ";
                        String[] selectionArgs = new String[]{callClassObj.getNumber()};

                        Integer rowsAffected = getContentResolver().update(DataContract.DataEntry.CONTENT_URI, values, selection, selectionArgs);

                        ds.getRef().removeValue();


                    }
                    progress.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    getSupportLoaderManager().initLoader(1, null, matchedConnection.this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progress.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);

        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_PHONE};


        String selection = DataContract.DataEntry.COLUMN_STATUS_INVITATION + " =? ";
        String[] selectionArgs = new String[]{getString(R.string.matched)};
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
}
