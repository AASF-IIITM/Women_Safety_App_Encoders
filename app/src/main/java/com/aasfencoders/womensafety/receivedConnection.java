package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.InviteSentClass;
import com.aasfencoders.womensafety.Class.ReceiveClass;
import com.aasfencoders.womensafety.adapter.InviteAdapter;
import com.aasfencoders.womensafety.adapter.ReceiveAdapter;
import com.aasfencoders.womensafety.utilities.CheckNetworkConnection;
import com.aasfencoders.womensafety.utilities.NetworkDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class receivedConnection extends AppCompatActivity {

    View view;
    ListView listView;
    SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;

    private ArrayList<ReceiveClass> receivedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_connection);

        sharedPreferences = receivedConnection.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        view = (View) findViewById(R.id.empty_view);
        listView = (ListView) findViewById(R.id.listOfInvitedConnections);
        listView.setEmptyView(view);

        boolean state = CheckNetworkConnection.checkNetwork(receivedConnection.this);
        if (state) {
            fetchReceivedContacts();
        } else {
            NetworkDialog.showNetworkDialog(receivedConnection.this);
        }
    }

    private void fetchReceivedContacts(){
        String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        if (current_user_number.equals(R.string.error)) {
            Toast.makeText(receivedConnection.this, getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.received));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    receivedList = new ArrayList<ReceiveClass>();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ReceiveClass callClassObj = ds.getValue(ReceiveClass.class);
                        receivedList.add(callClassObj);
                    }
                    Collections.reverse(receivedList);
                    ReceiveAdapter receiveAdapter = new ReceiveAdapter(receivedConnection.this, receivedList);
                    listView.setAdapter(receiveAdapter);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);

        }
    }
}
