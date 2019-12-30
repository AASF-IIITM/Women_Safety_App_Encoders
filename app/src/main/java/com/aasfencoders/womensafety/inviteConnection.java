package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class inviteConnection extends AppCompatActivity {

    View view;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_connection);
        getSupportActionBar().setTitle(getString(R.string.inviteConnectionHeading));

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
    }
}
