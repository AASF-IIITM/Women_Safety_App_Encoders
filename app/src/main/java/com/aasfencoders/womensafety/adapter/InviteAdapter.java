package com.aasfencoders.womensafety.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aasfencoders.womensafety.Class.InviteSentClass;
import com.aasfencoders.womensafety.R;
import com.aasfencoders.womensafety.inviteConnection;

import java.util.ArrayList;

public class InviteAdapter extends ArrayAdapter<InviteSentClass> {
    private Context mContext;


    public InviteAdapter(@NonNull Context context, ArrayList<InviteSentClass> inviteList) {
        super(context, 0, inviteList);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.single_invite_contact_item, parent, false);

        }

        InviteSentClass currentCall = getItem(position);

        TextView name = listItemView.findViewById(R.id.person_name);
        TextView number = listItemView.findViewById(R.id.person_number);
        TextView status = listItemView.findViewById(R.id.person_status);

        name.setText(currentCall.getName());
        number.setText(currentCall.getNumber());
        status.setText(currentCall.getStatus());

        return listItemView;
    }
}
