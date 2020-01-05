package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.aasfencoders.womensafety.Class.InviteSentClass;
import com.aasfencoders.womensafety.adapter.InviteAdapter;
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

public class inviteConnection extends AppCompatActivity {

    View view;
    ListView listView;
    SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;

    private ArrayList<InviteSentClass> inviteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_connection);
        getSupportActionBar().setTitle(getString(R.string.inviteConnectionHeading));

        sharedPreferences = inviteConnection.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        view = (View) findViewById(R.id.empty_view);
        listView = (ListView) findViewById(R.id.listOfInvitedConnections);
        listView.setEmptyView(view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(inviteConnection.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton reload = (FloatingActionButton) findViewById(R.id.fab_reload);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean state = CheckNetworkConnection.checkNetwork(inviteConnection.this);
                if (state) {
                    fetchInvitedContacts();
                } else {
                    NetworkDialog.showNetworkDialog(inviteConnection.this);
                }
            }
        });
    }

    private void fetchInvitedContacts() {

        String current_user_number = sharedPreferences.getString(getString(R.string.userNumber), getString(R.string.error));

        if (current_user_number.equals(R.string.error)) {
            Toast.makeText(inviteConnection.this, getString(R.string.errormessage), Toast.LENGTH_SHORT).show();
        } else {
            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.users)).child(current_user_number).child(getString(R.string.sent));
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    inviteList = new ArrayList<InviteSentClass>();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        InviteSentClass callClassObj = ds.getValue(InviteSentClass.class);
                        inviteList.add(callClassObj);
                    }
                    Collections.reverse(inviteList);
                    InviteAdapter inviteAdapter = new InviteAdapter(inviteConnection.this, inviteList);
                    listView.setAdapter(inviteAdapter);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userNameRef.addListenerForSingleValueEvent(eventListener);

        }


    }
}
