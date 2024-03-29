package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.adapter.ReceiveAdapter;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// This activity displays the Received Connections.
// Also we have a refresh button, to refresh the list.
public class receivedConnection extends AppCompatActivity {

    View view;
    ListView listView;
    SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;

    private View no_network;
    private View progress;
    private Button reload;
    private ArrayList<ReceiveClass> receivedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_connection);
        getSupportActionBar().setTitle(getString(R.string.receivedHeading));

        // Initialize the Firebase Database Reference and the Shared Preference
        sharedPreferences = receivedConnection.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        // Initializing the views and the adapters.
        view = (View) findViewById(R.id.empty_received_view);
        listView = (ListView) findViewById(R.id.listOfReceivedConnections);
        no_network = findViewById(R.id.no_internet_view);
        progress = findViewById(R.id.progress_view);

        // Check connection, if detected, fetch details from the Firebase.
        checkConnection();

        // Reload button to refresh the list.
        // First, we need to check Connection.
        // After that, fetch details from the firebase.
        FloatingActionButton reload = (FloatingActionButton) findViewById(R.id.fab_reload_received);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });


    }

    // Checking connection.
    private void checkConnection() {
        boolean state = CheckNetworkConnection.checkNetwork(receivedConnection.this);
        if (state) {
            // if connection detected, fetch the received contacts.
            fetchReceivedContacts();
            no_network.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
        } else {
            no_network.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
            view.setVisibility(View.GONE);
        }
    }

    // Fetch the received connections from the Firebase
    private void fetchReceivedContacts() {
        String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        if (current_user_number.equals(R.string.error)) {
            Toast.makeText(receivedConnection.this, getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {

            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.invitation)).child(current_user_number);
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    receivedList = new ArrayList<ReceiveClass>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ReceiveClass callClassObj = ds.getValue(ReceiveClass.class);
                        receivedList.add(callClassObj);
                    }
                    // If there are received connections, display them on the list adapter.
                    if (receivedList.size() > 0) {
                        ReceiveAdapter receiveAdapter = new ReceiveAdapter(receivedConnection.this, receivedList);
                        listView.setAdapter(receiveAdapter);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progress.setVisibility(View.GONE);
                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);
        }
    }
}
