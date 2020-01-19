package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.Collections;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class receivedConnection extends AppCompatActivity {

    View view;
    ListView listView;
    SharedPreferences sharedPreferences;
    private DatabaseReference mFirebaseReference;
    private SweetAlertDialog loadingDialog;

    private ArrayList<ReceiveClass> receivedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_connection);

        getSupportActionBar().setTitle(getString(R.string.receivedHeading));

        sharedPreferences = receivedConnection.this.getSharedPreferences(getString(R.string.package_name), Context.MODE_PRIVATE);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();

        view = (View) findViewById(R.id.empty_received_view);
        listView = (ListView) findViewById(R.id.listOfReceivedConnections);
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
            loadingDialog = new SweetAlertDialog(receivedConnection.this, SweetAlertDialog.PROGRESS_TYPE);
            loadingDialog.getProgressHelper().setBarColor(Color.parseColor("#8a1ca6"));
            loadingDialog.setTitleText(getString(R.string.receivedDialogString));
            loadingDialog.setCancelable(false);
            loadingDialog.show();

            DatabaseReference userNameRef = mFirebaseReference.child(getString(R.string.invitation)).child(current_user_number);
            ValueEventListener eventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    receivedList = new ArrayList<ReceiveClass>();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        ReceiveClass callClassObj = ds.getValue(ReceiveClass.class);
                        receivedList.add(callClassObj);
                    }

                    loadingDialog.dismissWithAnimation();

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
