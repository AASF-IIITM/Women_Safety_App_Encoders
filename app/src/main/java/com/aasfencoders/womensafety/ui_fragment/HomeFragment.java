package com.aasfencoders.womensafety.ui_fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.inviteConnection;
import com.aasfencoders.womensafety.matchedConnection;
import com.aasfencoders.womensafety.receivedConnection;

public class HomeFragment extends Fragment {

    private Button inviteNewConnection;
    private Button receivedConnectionButton;
    private Button showConnection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        inviteNewConnection = view.findViewById(R.id.inviteConnection);
        showConnection = view.findViewById(R.id.showConnection);
        receivedConnectionButton = view.findViewById(R.id.receiveConnection);

        // action triggered when the Invite New Connection button is pressed
        inviteNewConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), inviteConnection.class);
                startActivity(intent);
            }
        });

        // action triggered when the show Received Connection button is pressed
        receivedConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), receivedConnection.class);
                startActivity(intent);
            }
        });


        // action triggered when the show connection button is pressed
        showConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), matchedConnection.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
