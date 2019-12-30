package com.aasfencoders.womensafety.ui_fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aasfencoders.womensafety.R;

public class HomeFragment extends Fragment {

    private Button inviteNewConnection;
    private Button showConnection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        inviteNewConnection = view.findViewById(R.id.inviteConnection);
        showConnection = view.findViewById(R.id.showConnection);

        // action triggered when the Invite New Connection button is tpressed
        inviteNewConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        // action triggered when the show connection button is pressed
        showConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;
    }
}
